package models

import play.api.libs.json.{Format, Json}

final case class BirthRegistrationMatchingResponseModel(matched: Boolean)

object BirthRegistrationMatchingResponseModel {

  implicit lazy val format: Format[BirthRegistrationMatchingResponseModel] = Json.format
}
