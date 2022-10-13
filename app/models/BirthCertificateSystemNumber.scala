package models

import play.api.libs.json.{Format, Json}

final case class BirthCertificateSystemNumber(value: String) extends BirthReferenceNumber {

  override val brmsFormat: String = value
  override val display: String    = value
}

object BirthCertificateSystemNumber {

  implicit lazy val format: Format[BirthCertificateSystemNumber] = Json.format
}
