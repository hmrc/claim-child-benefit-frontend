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

package controllers.child

import base.SpecBase
import controllers.{routes => baseRoutes}
import forms.child.DateChildStartedLivingWithApplicantFormProvider
import generators.ModelGenerators
import models.{AdultName, ChildName, DesignatoryDetails, Done}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.applicant.{ApplicantNamePage, DesignatoryNamePage}
import pages.child.{ChildNamePage, DateChildStartedLivingWithApplicantPage}
import pages.{EmptyWaypoints, child}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import uk.gov.hmrc.domain.Nino
import views.html.child.DateChildStartedLivingWithApplicantView

import java.time.LocalDate
import scala.concurrent.Future

class DateChildStartedLivingWithApplicantControllerSpec extends SpecBase with MockitoSugar with ModelGenerators {

  private val childName = ChildName("first", None, "last")
  private val adultName = AdultName(None, "first", None, "last")
  private val baseAnswers =
    emptyUserAnswers
      .set(ChildNamePage(index), childName)
      .success
      .value
      .set(ApplicantNamePage, adultName)
      .success
      .value

  val formProvider = new DateChildStartedLivingWithApplicantFormProvider(clockAtFixedInstant)
  private val form = formProvider(childName)
  private val waypoints = EmptyWaypoints

  val validAnswer = LocalDate.now(clockAtFixedInstant).minusDays(1)

  lazy val dateChildStartedLivingWithApplicantRoute =
    routes.DateChildStartedLivingWithApplicantController.onPageLoad(waypoints, index).url

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateChildStartedLivingWithApplicantRoute)

  def postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateChildStartedLivingWithApplicantRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "DateChildStartedLivingWithApplicant Controller" - {

    "must return OK and the correct view for a GET" - {

      "when the user is unauthenticated" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        running(application) {
          val result = route(application, getRequest).value

          val view = application.injector.instanceOf[DateChildStartedLivingWithApplicantView]

          status(result) `mustEqual` OK
          contentAsString(result) `mustEqual` view(form, waypoints, index, childName, adultName)(
            getRequest,
            messages(application)
          ).toString
        }
      }

      "when the user is authenticated" - {

        val nino = arbitrary[Nino].sample.value.nino
        val npsName = AdultName(None, "original first", None, "original last")
        val designatoryDetails = DesignatoryDetails(Some(npsName), None, None, None, LocalDate.now)

        "and has given a new designatory name" in {

          val newName = AdultName(None, "new first", None, "new last")
          val answers =
            emptyUserAnswers
              .copy(nino = Some(nino), designatoryDetails = Some(designatoryDetails))
              .set(ChildNamePage(index), childName)
              .success
              .value
              .set(DesignatoryNamePage, newName)
              .success
              .value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val result = route(application, getRequest).value

            val view = application.injector.instanceOf[DateChildStartedLivingWithApplicantView]

            status(result) `mustEqual` OK
            contentAsString(result) `mustEqual` view(form, waypoints, index, childName, newName)(
              getRequest,
              messages(application)
            ).toString
          }
        }

        "and has not given a new designatory name" in {

          val answers =
            emptyUserAnswers
              .copy(nino = Some(nino), designatoryDetails = Some(designatoryDetails))
              .set(ChildNamePage(index), childName)
              .success
              .value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val result = route(application, getRequest).value

            val view = application.injector.instanceOf[DateChildStartedLivingWithApplicantView]

            status(result) `mustEqual` OK
            contentAsString(result) `mustEqual` view(form, waypoints, index, childName, npsName)(
              getRequest,
              messages(application)
            ).toString
          }
        }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(DateChildStartedLivingWithApplicantPage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[DateChildStartedLivingWithApplicantView]

        val result = route(application, getRequest).value

        status(result) `mustEqual` OK
        contentAsString(result) `mustEqual` view(form.fill(validAnswer), waypoints, index, childName, adultName)(
          getRequest,
          messages(application)
        ).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) `thenReturn` Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val result = route(application, postRequest).value
        val expectedAnswers =
          baseAnswers.set(child.DateChildStartedLivingWithApplicantPage(index), validAnswer).success.value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` child
          .DateChildStartedLivingWithApplicantPage(index)
          .navigate(waypoints, emptyUserAnswers, expectedAnswers)
          .url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, dateChildStartedLivingWithApplicantRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DateChildStartedLivingWithApplicantView]

        val result = route(application, request).value

        status(result) `mustEqual` BAD_REQUEST
        contentAsString(result) `mustEqual` view(boundForm, waypoints, index, childName, adultName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
