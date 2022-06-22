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

package pages.child

import controllers.child.routes
import models.{ApplicantRelationshipToChild, Index}
import pages.behaviours.PageBehaviours
import pages.{EmptyWaypoints, child}

class ApplicantRelationshipToChildPageSpec extends PageBehaviours {

  "ApplicantRelationshipToChildPage" - {

    beRetrievable[ApplicantRelationshipToChild](ApplicantRelationshipToChildPage(index))

    beSettable[ApplicantRelationshipToChild](ApplicantRelationshipToChildPage(index))

    beRemovable[ApplicantRelationshipToChild](ApplicantRelationshipToChildPage(index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Anyone Claimed for Child Before with the same index" in {

          ApplicantRelationshipToChildPage(Index(0))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.AnyoneClaimedForChildBeforeController.onPageLoad(waypoints, Index(0)))

          ApplicantRelationshipToChildPage(Index(1))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.AnyoneClaimedForChildBeforeController.onPageLoad(waypoints, Index(1)))
        }
      }
    }
  }
}