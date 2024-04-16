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
import forms.payments.WantToBePaidFormProvider
import models.Done
import models.Income._
import models.RelationshipStatus._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.partner.RelationshipStatusPage
import pages.payments._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.payments._

import scala.concurrent.Future

class WantToBePaidControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new WantToBePaidFormProvider()
  private val form = formProvider()
  private val waypoints = EmptyWaypoints

  "WantToBePaid Controller" - {

    "must return OK and the correct view for a GET" - {

      "when the applicant is married or cohabiting" - {

        val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value
        val baseAnswers = emptyUserAnswers.set(RelationshipStatusPage, relationshipStatus).success.value

        "and their income is under LowerThreshold" - {

          "and their partner's income is under LowerThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BelowLowerThreshold).success.value
                .set(PartnerIncomePage, BelowLowerThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderLowerThresholdUnderLowerThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is between LowerThreshold and UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BelowLowerThreshold).success.value
                .set(PartnerIncomePage, BetweenThresholds).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderLowerThresholdUnderUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is over UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BelowLowerThreshold).success.value
                .set(PartnerIncomePage, AboveUpperThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderLowerThresholdOverUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
            }
          }
        }

        "and their income is between LowerThreshold and UpperThreshold" - {

          "and their partner's income is below UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BetweenThresholds).success.value
                .set(PartnerIncomePage, BelowLowerThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderUpperThresholdUnderLowerThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is between LowerThreshold and UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BetweenThresholds).success.value
                .set(PartnerIncomePage, BetweenThresholds).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderUpperThresholdUnderUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is over UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BetweenThresholds).success.value
                .set(PartnerIncomePage, AboveUpperThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderUpperThresholdOverUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
            }
          }
        }

        "and their income is over UpperThreshold" - {

          "and their partner's income is below LowerThreshold" in {

            val answers =
              baseAnswers
                .set(RelationshipStatusPage, relationshipStatus).success.value
                .set(ApplicantIncomePage, AboveUpperThreshold).success.value
                .set(PartnerIncomePage, BelowLowerThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleOverUpperThresholdUnderLowerThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is between LowerThreshold and UpperThreshold" in {

            val answers =
              baseAnswers
                .set(RelationshipStatusPage, relationshipStatus).success.value
                .set(ApplicantIncomePage, AboveUpperThreshold).success.value
                .set(PartnerIncomePage, BetweenThresholds).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleOverUpperThresholdUnderUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is over UpperThreshold" in {

            val answers =
              baseAnswers
                .set(RelationshipStatusPage, relationshipStatus).success.value
                .set(ApplicantIncomePage, AboveUpperThreshold).success.value
                .set(PartnerIncomePage, AboveUpperThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleOverUpperThresholdOverUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
            }
          }
        }
      }

      "when the applicant is single, separated, divorced or widowed" - {

        val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value
        val baseAnswers = emptyUserAnswers.set(RelationshipStatusPage, relationshipStatus).success.value

        "and their income is under LowerThreshold" in {

          val answers = baseAnswers.set(ApplicantIncomePage, BelowLowerThreshold).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleUnderLowerThresholdView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
          }
        }

        "and their income is between LowerThreshold and UpperThreshold" in {

          val answers = baseAnswers.set(ApplicantIncomePage, BetweenThresholds).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleUnderUpperThresholdView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
          }
        }

        "and their income is over UpperThreshold" in {

          val answers = baseAnswers.set(ApplicantIncomePage, AboveUpperThreshold).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleOverUpperThresholdView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
          }
        }
      }
    }

    "must return OK and the correct, pre-populated view for a GET" - {

      "when the applicant is married or cohabiting" - {

        val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value
        val baseAnswers = emptyUserAnswers.set(RelationshipStatusPage, relationshipStatus).success.value

        "and their income is below LowerThreshold" - {

          "and their partner's income is between LowerThreshold and UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BelowLowerThreshold).success.value
                .set(PartnerIncomePage, BetweenThresholds).success.value
                .set(WantToBePaidPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderLowerThresholdUnderUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is over UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BelowLowerThreshold).success.value
                .set(PartnerIncomePage, AboveUpperThreshold).success.value
                .set(WantToBePaidPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderLowerThresholdOverUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
            }
          }
        }

        "and their income is between LowerThreshold and UpperThreshold" - {

          "and their partner's income is below LowerThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BetweenThresholds).success.value
                .set(PartnerIncomePage, BelowLowerThreshold).success.value
                .set(WantToBePaidPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderUpperThresholdUnderLowerThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is between LowerThreshold and UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BetweenThresholds).success.value
                .set(PartnerIncomePage, BetweenThresholds).success.value
                .set(WantToBePaidPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderUpperThresholdUnderUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is above UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BetweenThresholds).success.value
                .set(PartnerIncomePage, AboveUpperThreshold).success.value
                .set(WantToBePaidPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderUpperThresholdOverUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
            }
          }
        }

        "and their income is over UpperThreshold" - {

          "and their partner's income is below LowerThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, AboveUpperThreshold).success.value
                .set(PartnerIncomePage, BelowLowerThreshold).success.value
                .set(WantToBePaidPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleOverUpperThresholdUnderLowerThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is between LowerThreshold and UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, AboveUpperThreshold).success.value
                .set(PartnerIncomePage, BetweenThresholds).success.value
                .set(WantToBePaidPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleOverUpperThresholdUnderUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is above UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, AboveUpperThreshold).success.value
                .set(PartnerIncomePage, AboveUpperThreshold).success.value
                .set(WantToBePaidPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleOverUpperThresholdOverUpperThresholdView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
            }
          }
        }
      }

      "when the applicant is single, separated, divorced or widowed" - {

        "and their income is between LowerThreshold and UpperThreshold" in {

          val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantIncomePage, BetweenThresholds).success.value
              .set(WantToBePaidPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleUnderUpperThresholdView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
          }
        }

        "and their income is over UpperThreshold" in {

          val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantIncomePage, AboveUpperThreshold).success.value
              .set(WantToBePaidPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleOverUpperThresholdView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
          }
        }
      }
    }

    "must save the answer and redirect to the next page for a POST of valid data" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =

          FakeRequest(POST, routes.WantToBePaidController.onSubmit().url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = emptyUserAnswers.set(WantToBePaidPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual WantToBePaidPage.navigate(waypoints, emptyUserAnswers, expectedAnswers).route.url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" - {

      "when the applicant is married or cohabiting" - {

        val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value
        val baseAnswers = emptyUserAnswers.set(RelationshipStatusPage, relationshipStatus).success.value

        "and their income is below LowerThreshold" - {

          "and their partner's income is between LowerThreshold and UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BelowLowerThreshold).success.value
                .set(PartnerIncomePage, BetweenThresholds).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))
              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderLowerThresholdUnderUpperThresholdView]

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is over UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BelowLowerThreshold).success.value
                .set(PartnerIncomePage, AboveUpperThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))
              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderLowerThresholdOverUpperThresholdView]

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
            }
          }
        }

        "and their income is between LowerThreshold and UpperThreshold" - {

          "and their partner's income is below LowerThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BetweenThresholds).success.value
                .set(PartnerIncomePage, BelowLowerThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))
              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderUpperThresholdUnderLowerThresholdView]

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is between LowerThreshold and UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BetweenThresholds).success.value
                .set(PartnerIncomePage, BetweenThresholds).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))
              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderUpperThresholdUnderUpperThresholdView]

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is over UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, BetweenThresholds).success.value
                .set(PartnerIncomePage, AboveUpperThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))
              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleUnderUpperThresholdOverUpperThresholdView]

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
            }
          }
        }

        "and their income is over UpperThreshold" - {

          "and their partner's income is below LowerThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, AboveUpperThreshold).success.value
                .set(PartnerIncomePage, BelowLowerThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))
              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleOverUpperThresholdUnderLowerThresholdView]

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is between LowerThreshold and UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, AboveUpperThreshold).success.value
                .set(PartnerIncomePage, BetweenThresholds).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))
              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleOverUpperThresholdUnderUpperThresholdView]

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
            }
          }

          "and their partner's income is over UpperThreshold" in {

            val answers =
              baseAnswers
                .set(ApplicantIncomePage, AboveUpperThreshold).success.value
                .set(PartnerIncomePage, AboveUpperThreshold).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))
              val result = route(application, request).value

              val view = application.injector.instanceOf[WantToBePaidCoupleOverUpperThresholdOverUpperThresholdView]

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
            }
          }
        }
      }

      "when the applicant is single, separated, divorced or widowed" - {

        val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value
        val baseAnswers = emptyUserAnswers.set(RelationshipStatusPage, relationshipStatus).success.value

        "and their income is between LowerThreshold and UpperThreshold" in {

          val answers = baseAnswers.set(ApplicantIncomePage, BetweenThresholds).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))
            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleUnderUpperThresholdView]

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
          }
        }

        "and their income is over UpperThreshold" in {

          val answers = baseAnswers.set(ApplicantIncomePage, AboveUpperThreshold).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))
            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleOverUpperThresholdView]

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
          }
        }
      }
    }
  }
}
