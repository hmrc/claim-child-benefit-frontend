/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import base.SpecBase
import models.TaskListSectionChange
import models.TaskListSectionChange.{PartnerDetailsRemoved, PartnerDetailsRequired, PaymentDetailsRemoved}
import pages.{EmptyWaypoints, RelationshipStatusChangesTaskListPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html._

class RelationshipStatusChangesTaskListControllerSpec extends SpecBase {

  private val waypoints = EmptyWaypoints

  "RelationshipStatusChangesTaskList Controller" - {

    "must return OK and the correct view for a GET" - {

      "when the user's payment details have been removed" in {

        val answers = emptyUserAnswers.set(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PaymentDetailsRemoved)).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.RelationshipStatusChangesTaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RelationshipStatusChangesTaskListPaymentRemovedView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
        }
      }

      "when the user's partner details have been removed" in {

        val answers = emptyUserAnswers.set(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRemoved)).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.RelationshipStatusChangesTaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RelationshipStatusChangesTaskListPartnerRemovedView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
        }
      }

      "when the user's partner details are required" in {

        val answers = emptyUserAnswers.set(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRequired)).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.RelationshipStatusChangesTaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RelationshipStatusChangesTaskListPartnerRequiredView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
        }
      }

      "when the user's payment and partner details have been removed" in {

        val answers = emptyUserAnswers.set(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PaymentDetailsRemoved, PartnerDetailsRemoved)).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.RelationshipStatusChangesTaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RelationshipStatusChangesTaskListPaymentRemovedPartnerRemovedView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
        }
      }

      "when the user's payment details have been removed and partner details are required" in {

        val answers = emptyUserAnswers.set(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PaymentDetailsRemoved, PartnerDetailsRequired)).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.RelationshipStatusChangesTaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RelationshipStatusChangesTaskListPaymentRemovedPartnerRequiredView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
        }
      }
    }

    "must redirect to the next page for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.RelationshipStatusChangesTaskListController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RelationshipStatusChangesTaskListPage.navigate(waypoints, emptyUserAnswers, emptyUserAnswers).route.url
      }
    }
  }
}
