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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TaskListService
import views.html.TaskListView

class TaskListControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockTaskListService = mock[TaskListService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockTaskListService)
    super.beforeEach()
  }

  "Task List Controller" - {

    "must return OK and the correct view for a GET" - {

      "when user answers can be found" in {

        val application =
          applicationBuilder(Some(emptyUserAnswers))
            .overrides(bind[TaskListService].toInstance(mockTaskListService))
            .build()

        when(mockTaskListService.sections(any())).thenReturn(Nil)

        running(application) {
          val request = FakeRequest(GET, routes.TaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[TaskListView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(Nil)(request, messages(application)).toString
        }
      }

      "when user answers can't be found and the user is signed in" in {

        val application =
          applicationBuilder(None, userIsAuthenticated = true)
            .overrides(bind[TaskListService].toInstance(mockTaskListService))
            .build()

        when(mockTaskListService.sections(any())).thenReturn(Nil)

        running(application) {
          val request = FakeRequest(GET, routes.TaskListController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      "when user answers can't be found and the user is not signed in" in {

        val application =
          applicationBuilder(None, userIsAuthenticated = false)
            .overrides(bind[TaskListService].toInstance(mockTaskListService))
            .build()

        when(mockTaskListService.sections(any())).thenReturn(Nil)

        running(application) {
          val request = FakeRequest(GET, routes.TaskListController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }
  }
}
