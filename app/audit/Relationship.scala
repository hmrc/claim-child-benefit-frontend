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

package audit

import models.journey
import play.api.libs.json.{Json, Writes}

import java.time.LocalDate

final case class Relationship(status: String, since: Option[LocalDate], partner: Option[Partner])

object Relationship {

  implicit lazy val writes: Writes[Relationship] = Json.writes

  def build(relationship: journey.Relationship): Relationship =
    Relationship(
     status = relationship.status.toString,
     since = relationship.since,
     partner = relationship.partner.map(Partner.build)
   )
}
