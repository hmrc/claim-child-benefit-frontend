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

import cats.data.{IorNec, NonEmptyChain}
import cats.implicits._
import play.api.libs.json._
import queries.{Derivable, Gettable, Query, Settable}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
                              id: String,
                              data: JsObject = Json.obj(),
                              nino: Option[String] = None,
                              designatoryDetails: Option[DesignatoryDetails] = None,
                              relationshipDetails: Option[RelationshipDetails] = None,
                              lastUpdated: Instant = Instant.now
                            ) {

  lazy val isAuthenticated: Boolean = nino.nonEmpty

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def getIor[A](page: Gettable[A])(implicit rds: Reads[A]): IorNec[Query, A] =
    get(page).toRightIor(NonEmptyChain.one(page))

  def get[A, B](derivable: Derivable[A, B])(implicit rds: Reads[A]): Option[B] =
    Reads.optionNoError(Reads.at(derivable.path))
      .reads(data)
      .getOrElse(None)
      .map(derivable.derive)

  def isDefined(gettable: Gettable[_]): Boolean =
    Reads.optionNoError(Reads.at[JsValue](gettable.path)).reads(data)
      .map(_.isDefined)
      .getOrElse(false)

  def notDefined(gettable: Gettable[_]): Boolean =
    !isDefined(gettable)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val originalAnswers = this

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy (data = d)
        page.cleanup(Some(value), originalAnswers, updatedAnswers)
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val originalAnswers = this

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy (data = d)
        page.cleanup(None, originalAnswers, updatedAnswers)
    }
  }
}

object UserAnswers {

  private val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[String] and
      (__ \ "data").read[JsObject] and
      (__ \ "lastUpdated").read[Instant]
    ) ((id, data, lastUpdated) => UserAnswers(id, data, None, None, None, lastUpdated))
  }

  private val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._
    
    (
      (__ \ "_id").write[String] and
      (__ \ "data").write[JsObject] and
      (__ \ "lastUpdated").write[Instant]
    ) (ua => (ua.id, ua.data, ua.lastUpdated))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)

  def encryptedFormat(implicit crypto: Encrypter with Decrypter): OFormat[UserAnswers] = {

    import play.api.libs.functional.syntax._

    implicit val sensitiveFormat: Format[SensitiveString] =
      JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

    val encryptedReads: Reads[UserAnswers] =
      (
        (__ \ "_id").read[String] and
        (__ \ "data").read[SensitiveString] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )((id, data, lastUpdated) => UserAnswers(id, Json.parse(data.decryptedValue).as[JsObject], None, None, None, lastUpdated))

    val encryptedWrites: OWrites[UserAnswers] =
      (
        (__ \ "_id").write[String] and
        (__ \ "data").write[SensitiveString] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      )(ua => (ua.id, SensitiveString(Json.stringify(ua.data)), ua.lastUpdated))

    OFormat(encryptedReads orElse reads, encryptedWrites)
  }
}
