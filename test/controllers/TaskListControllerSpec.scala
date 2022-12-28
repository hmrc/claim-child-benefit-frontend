/*
 * Copyright 2022 HM Revenue & Customs
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
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, TaskListPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TaskListService
import views.html.TaskListView

class TaskListControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private def mockTaskListService = mock[TaskListService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockTaskListService)
    super.beforeEach()
  }

  private def appBuilder(answers: Option[UserAnswers]): GuiceApplicationBuilder =
    applicationBuilder(answers).overrides(bind[TaskListService].toInstance(mockTaskListService))

  "Task List Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = appBuilder(Some(emptyUserAnswers)).build()

      when(mockTaskListService.sections(any())).thenReturn(Nil)

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Nil)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = appBuilder( None).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page for a POST" in {

      val application = appBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TaskListPage.navigate(EmptyWaypoints, emptyUserAnswers, emptyUserAnswers).url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = appBuilder(None).build()

      running(application) {
        val request = FakeRequest(POST, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
