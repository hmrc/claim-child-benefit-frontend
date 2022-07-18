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

package models

import generators.ModelGenerators
import models.IncludedDocuments._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

class IncludedDocumentsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "IncludedDocuments" - {

    "must serialise and deserialise standard documents to/from JSON" in {

      val standardDocuments: Gen[IncludedDocuments] = Gen.oneOf(BirthCertificate, Passport, TravelDocuments, AdoptionCertificate)

      forAll(standardDocuments) {
        document =>
          val json = Json.toJson(document)
          json.validate[IncludedDocuments] mustEqual JsSuccess(document)
      }
    }

    "must serialise and deserialise other documents to/from JSON" in {

      val standardDocuments = Set(BirthCertificate, Passport, TravelDocuments, AdoptionCertificate)

      forAll(arbitrary[String]) {
        value =>

          whenever (!standardDocuments.map(_.toString).contains(value)) {
            val json = Json.toJson(OtherDocument(value): IncludedDocuments)
            json.validate[IncludedDocuments] mustEqual JsSuccess(OtherDocument(value))
          }
      }
    }
  }
}
