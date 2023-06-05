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

package models

import models.ChildBirthRegistrationCountry._
import play.api.libs.json.{Format, Json}

import java.time.LocalDate

final case class BirthRegistrationMatchingRequest(
                                                   birthReferenceNumber: Option[String],
                                                   firstName: String,
                                                   additionalNames: Option[String],
                                                   lastName: String,
                                                   dateOfBirth: LocalDate,
                                                   whereBirthRegistered: String
                                                 )

object BirthRegistrationMatchingRequest {

  implicit lazy val format: Format[BirthRegistrationMatchingRequest] = Json.format

  def apply(
             birthCertificateNumber: Option[BirthCertificateNumber],
             name: ChildName,
             dateOfBirth: LocalDate,
             countryOfRegistration: ChildBirthRegistrationCountry
           ): Option[BirthRegistrationMatchingRequest] = {
    countryOfRegistration match {
      case England | Scotland | Wales | NorthernIreland =>
        Some(BirthRegistrationMatchingRequest(
          birthReferenceNumber = birthCertificateNumber.map(_.brmsFormat),
          firstName            = name.firstName,
          additionalNames      = name.middleNames,
          lastName             = name.lastName,
          dateOfBirth          = dateOfBirth,
          whereBirthRegistered = countryOfRegistration match {
            case England         => "england"
            case Scotland        => "scotland"
            case Wales           => "wales"
            case NorthernIreland => "northern ireland"
            case _               => throw new RuntimeException()
          }
        ))

      case _ =>
        None
    }
  }
}