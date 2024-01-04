/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.payments

import base.SpecBase
import connectors.BankAccountInsightsConnector
import controllers.{routes => baseRoutes}
import forms.payments.{BankAccountDetailsFormModel, BankAccountDetailsFormProvider}
import models.{BankAccountDetails, BankAccountHolder, BankAccountInsightsResponseModel, Done, ReputationResponseEnum, UnexpectedException, VerifyBankDetailsResponseModel}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.payments.{BankAccountDetailsPage, BankAccountHolderPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.BankAccountInsightsResultQuery
import services.{BarsService, UserDataService}
import views.html.payments.BankAccountDetailsView

import scala.concurrent.Future

class BankAccountDetailsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val formProvider = new BankAccountDetailsFormProvider()
  private val form = formProvider()
  private val waypoints = EmptyWaypoints

  private lazy val bankAccountDetailsRoute = routes.BankAccountDetailsController.onPageLoad(waypoints).url

  private val baseAnswers = emptyUserAnswers.set(BankAccountHolderPage, BankAccountHolder.Applicant).success.value
  private val validAnswer = BankAccountDetails("first", "last", "123456", "00123456")
  private val userAnswers = baseAnswers.set(BankAccountDetailsPage, validAnswer).success.value

  private val mockBarsService = mock[BarsService]
  private val mockUserDataService = mock[UserDataService]
  private val mockBankAccountInsightsConnector = mock[BankAccountInsightsConnector]

  private val bankAccountInsightsResponse = BankAccountInsightsResponseModel("correlationId", 0, "reason")

  override def beforeEach(): Unit = {
    Mockito.reset(mockBarsService)
    Mockito.reset(mockUserDataService)
    Mockito.reset(mockBankAccountInsightsConnector)
    super.beforeEach()
  }

  "BankAccountDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, bankAccountDetailsRoute)

        val view = application.injector.instanceOf[BankAccountDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, None)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, bankAccountDetailsRoute)

        val view = application.injector.instanceOf[BankAccountDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(BankAccountDetailsFormModel(validAnswer, None)), waypoints, None)(request, messages(application)).toString
      }
    }

    "must save the answer, and the result of the bank account insights call, and redirect to the next page when valid data is submitted and the BARS response is successful" in {

      val happyBarsResponse = VerifyBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.No,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
        sortCodeSupportsDirectCredit = ReputationResponseEnum.Yes,
        accountExists = ReputationResponseEnum.Yes,
        nameMatches = ReputationResponseEnum.Yes
      )

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(Some(happyBarsResponse))
      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Right(bankAccountInsightsResponse))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService),
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"))

        val result = route(application, request).value
        val expectedAnswers =
          baseAnswers
            .set(BankAccountDetailsPage, validAnswer).success.value
            .set(BankAccountInsightsResultQuery, bankAccountInsightsResponse).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual BankAccountDetailsPage.navigate(waypoints, baseAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
        verify(mockBarsService, times(1)).verifyBankDetails(any())(any(), any())
        verify(mockBankAccountInsightsConnector, times(1)).check(any())(any())
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted and the BARS response is successful but the call to bank account insights fails" in {

      val happyBarsResponse = VerifyBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.No,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
        sortCodeSupportsDirectCredit = ReputationResponseEnum.Yes,
        accountExists = ReputationResponseEnum.Yes,
        nameMatches = ReputationResponseEnum.Yes
      )

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(Some(happyBarsResponse))
      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Left(UnexpectedException))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService),
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(BankAccountDetailsPage, validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual BankAccountDetailsPage.navigate(waypoints, baseAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
        verify(mockBarsService, times(1)).verifyBankDetails(any())(any(), any())
        verify(mockBankAccountInsightsConnector, times(1)).check(any())(any())
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted and we cannot get a good response from BARS" in {

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(None)
      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Right(bankAccountInsightsResponse))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService),
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"))

        val result = route(application, request).value
        val expectedAnswers =
          baseAnswers
            .set(BankAccountDetailsPage, validAnswer).success.value
            .set(BankAccountInsightsResultQuery, bankAccountInsightsResponse).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual BankAccountDetailsPage.navigate(waypoints, baseAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
        verify(mockBarsService, times(1)).verifyBankDetails(any())(any(), any())
        verify(mockBankAccountInsightsConnector, times(1)).check(any())(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[BankAccountDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, None)(request, messages(application)).toString
      }
    }

    "must return a Bad Request when the BARS response indicates a bad sort code" in {

      val invalidDetailsResponse = VerifyBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.Indeterminate,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.Indeterminate,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.No,
        sortCodeSupportsDirectCredit = ReputationResponseEnum.Yes,
        accountExists = ReputationResponseEnum.Indeterminate,
        nameMatches = ReputationResponseEnum.Indeterminate
      )

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(Some(invalidDetailsResponse))
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Right(bankAccountInsightsResponse))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return a Bad Request when the BARS response indicates a failed modulus check" in {

      val invalidDetailsResponse = VerifyBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.No,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.Indeterminate,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
        sortCodeSupportsDirectCredit = ReputationResponseEnum.Yes,
        accountExists = ReputationResponseEnum.Indeterminate,
        nameMatches = ReputationResponseEnum.Indeterminate
      )

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(Some(invalidDetailsResponse))
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Right(bankAccountInsightsResponse))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return a Bad Request when the BARS response indicates special details are required" in {

      val invalidDetailsResponse = VerifyBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.Yes,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
        sortCodeSupportsDirectCredit = ReputationResponseEnum.Yes,
        accountExists = ReputationResponseEnum.Yes,
        nameMatches = ReputationResponseEnum.Yes
      )

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(Some(invalidDetailsResponse))
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Right(bankAccountInsightsResponse))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return a Bad Request when the BARS response indicates the account does not exist" in {

      val invalidDetailsResponse = VerifyBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.No,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
        sortCodeSupportsDirectCredit = ReputationResponseEnum.Yes,
        accountExists = ReputationResponseEnum.No,
        nameMatches = ReputationResponseEnum.Yes
      )

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(Some(invalidDetailsResponse))
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Right(bankAccountInsightsResponse))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return a Bad Request when the BARS response indicates the account does not support Direct Credit" in {

      val invalidDetailsResponse = VerifyBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.No,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
        sortCodeSupportsDirectCredit = ReputationResponseEnum.No,
        accountExists = ReputationResponseEnum.Yes,
        nameMatches = ReputationResponseEnum.Yes
      )

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(Some(invalidDetailsResponse))
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Right(bankAccountInsightsResponse))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return a Bad Request when the BARS response indicates the name does not match, and the user has not already seen a soft error" in {

      val invalidDetailsResponse = VerifyBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.No,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
        sortCodeSupportsDirectCredit = ReputationResponseEnum.Yes,
        accountExists = ReputationResponseEnum.Yes,
        nameMatches = ReputationResponseEnum.No
      )

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(Some(invalidDetailsResponse))
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Right(bankAccountInsightsResponse))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return a Bad Request when the BARS response indicates the name does not match, and the user has previously seen a hard error" in {

      val invalidDetailsResponse = VerifyBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.No,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
        sortCodeSupportsDirectCredit = ReputationResponseEnum.Yes,
        accountExists = ReputationResponseEnum.Yes,
        nameMatches = ReputationResponseEnum.No
      )

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(Some(invalidDetailsResponse))
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Right(bankAccountInsightsResponse))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"), ("softError", "false"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must save the answer and redirect to the next page when the BARS response indicates the name does not match, and the user has already seen a soft error" in {

      val invalidDetailsResponse = VerifyBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.No,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
        sortCodeSupportsDirectCredit = ReputationResponseEnum.Yes,
        accountExists = ReputationResponseEnum.Yes,
        nameMatches = ReputationResponseEnum.No
      )

      when(mockBarsService.verifyBankDetails(any())(any(), any())) thenReturn Future.successful(Some(invalidDetailsResponse))
      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)
      when(mockBankAccountInsightsConnector.check(any())(any())) thenReturn Future.successful(Right(bankAccountInsightsResponse))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService),
            bind[BarsService].toInstance(mockBarsService),
            bind[BankAccountInsightsConnector].toInstance(mockBankAccountInsightsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"), ("softError", "true"))

        val result = route(application, request).value
        val expectedAnswers =
          baseAnswers
            .set(BankAccountDetailsPage, validAnswer).success.value
            .set(BankAccountInsightsResultQuery, bankAccountInsightsResponse).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual BankAccountDetailsPage.navigate(waypoints, baseAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, bankAccountDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, bankAccountDetailsRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"), ("accountNumber", "00123456"), ("sortCode", "123456"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
