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

import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem
import viewmodels.govuk.select._

sealed trait NationalityGroup {
  val order: Int
}

object NationalityGroup extends Enumerable.Implicits {

  case object UkCta extends WithName("UkCta") with NationalityGroup { override val order: Int = 3 }
  case object Eea extends WithName("Eea") with NationalityGroup { override val order: Int = 1 }
  case object NonEea extends WithName("NonEea") with NationalityGroup { override val order: Int = 2 }

  val values: Seq[NationalityGroup] = Seq(UkCta, Eea, NonEea)

  implicit val enumerable: Enumerable[NationalityGroup] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

object NationalityGroupOrdering extends Ordering[NationalityGroup] {
  override def compare(x: NationalityGroup, y: NationalityGroup): Int = x.order compare y.order
}

final case class Nationality(name: String, group: NationalityGroup)

object Nationality {

  import NationalityGroup._

  implicit lazy val format: OFormat[Nationality] = Json.format

  val allNationalities: Seq[Nationality] = Seq(
    Nationality("Afghan", NonEea),
    Nationality("Albanian", NonEea),
    Nationality("Algerian", NonEea),
    Nationality("American", NonEea),
    Nationality("Andorran", NonEea),
    Nationality("Angolan", NonEea),
    Nationality("Anguillan", NonEea),
    Nationality("Argentine", NonEea),
    Nationality("Armenian", NonEea),
    Nationality("Australian", NonEea),
    Nationality("Austrian", Eea),
    Nationality("Azerbaijani", NonEea),
    Nationality("Bahamian", NonEea),
    Nationality("Bahraini", NonEea),
    Nationality("Bangladeshi", NonEea),
    Nationality("Barbadian", NonEea),
    Nationality("Belarusian", NonEea),
    Nationality("Belgian", Eea),
    Nationality("Belizean", NonEea),
    Nationality("Beninese", NonEea),
    Nationality("Bermudian", NonEea),
    Nationality("Bhutanese", NonEea),
    Nationality("Bolivian", NonEea),
    Nationality("Botswanan", NonEea),
    Nationality("Brazilian", NonEea),
    Nationality("British", UkCta),
    Nationality("British Virgin Islander", NonEea),
    Nationality("Bruneian", NonEea),
    Nationality("Bulgarian", Eea),
    Nationality("Burkinan", NonEea),
    Nationality("Burmese", NonEea),
    Nationality("Burundian", NonEea),
    Nationality("Cambodian", NonEea),
    Nationality("Cameroonian", NonEea),
    Nationality("Canadian", NonEea),
    Nationality("Cape Verdean", NonEea),
    Nationality("Cayman Islander", NonEea),
    Nationality("Central African", NonEea),
    Nationality("Chadian", NonEea),
    Nationality("Chilean", NonEea),
    Nationality("Chinese", NonEea),
    Nationality("Citizen of Antigua and Barbuda", NonEea),
    Nationality("Citizen of Bosnia and Herzegovina", NonEea),
    Nationality("Citizen of Guinea-Bissau", NonEea),
    Nationality("Citizen of Kiribati", NonEea),
    Nationality("Citizen of Seychelles", NonEea),
    Nationality("Citizen of the Dominican Republic", NonEea),
    Nationality("Citizen of Vanuatu", NonEea),
    Nationality("Colombian", NonEea),
    Nationality("Comoran", NonEea),
    Nationality("Congolese (Congo)", NonEea),
    Nationality("Congolese (DRC)", NonEea),
    Nationality("Cook Islander", NonEea),
    Nationality("Costa Rican", NonEea),
    Nationality("Croatian", Eea),
    Nationality("Cuban", NonEea),
    Nationality("Cymraes", UkCta),
    Nationality("Cymro", UkCta),
    Nationality("Cypriot", Eea),
    Nationality("Czech", Eea),
    Nationality("Danish", Eea),
    Nationality("Djiboutian", NonEea),
    Nationality("Dominican", NonEea),
    Nationality("Dutch", Eea),
    Nationality("East Timorese", NonEea),
    Nationality("Ecuadorean", NonEea),
    Nationality("Egyptian", NonEea),
    Nationality("Emirati", NonEea),
    Nationality("English", UkCta),
    Nationality("Equatorial Guinean", NonEea),
    Nationality("Eritrean", NonEea),
    Nationality("Estonian", Eea),
    Nationality("Ethiopian", NonEea),
    Nationality("Faroese", NonEea),
    Nationality("Fijian", NonEea),
    Nationality("Filipino", NonEea),
    Nationality("Finnish", Eea),
    Nationality("French", Eea),
    Nationality("Gabonese", NonEea),
    Nationality("Gambian", NonEea),
    Nationality("Georgian", NonEea),
    Nationality("German", NonEea),
    Nationality("Ghanaian", NonEea),
    Nationality("Gibraltarian", NonEea),
    Nationality("Greek", Eea),
    Nationality("Greenlandic", NonEea),
    Nationality("Grenadian", NonEea),
    Nationality("Guamanian", NonEea),
    Nationality("Guatemalan", NonEea),
    Nationality("Guinean", NonEea),
    Nationality("Guyanese", NonEea),
    Nationality("Haitian", NonEea),
    Nationality("Honduran", NonEea),
    Nationality("Hong Konger", NonEea),
    Nationality("Hungarian", Eea),
    Nationality("Icelandic", Eea),
    Nationality("Indian", NonEea),
    Nationality("Indonesian", NonEea),
    Nationality("Iranian", NonEea),
    Nationality("Iraqi", NonEea),
    Nationality("Irish", UkCta),
    Nationality("Israeli", NonEea),
    Nationality("Italian", Eea),
    Nationality("Ivorian", NonEea),
    Nationality("Jamaican", NonEea),
    Nationality("Japanese", NonEea),
    Nationality("Jordanian", NonEea),
    Nationality("Kazakh", NonEea),
    Nationality("Kenyan", NonEea),
    Nationality("Kittitian", NonEea),
    Nationality("Kosovan", NonEea),
    Nationality("Kuwaiti", NonEea),
    Nationality("Kyrgyz", NonEea),
    Nationality("Lao", NonEea),
    Nationality("Latvian", Eea),
    Nationality("Lebanese", NonEea),
    Nationality("Liberian", NonEea),
    Nationality("Libyan", NonEea),
    Nationality("Liechtenstein citizen", Eea),
    Nationality("Lithuanian", Eea),
    Nationality("Luxembourger", Eea),
    Nationality("Macanese", NonEea),
    Nationality("Macedonian", NonEea),
    Nationality("Malagasy", NonEea),
    Nationality("Malawian", NonEea),
    Nationality("Malaysian", NonEea),
    Nationality("Maldivian", NonEea),
    Nationality("Malian", NonEea),
    Nationality("Maltese", Eea),
    Nationality("Marshallese", NonEea),
    Nationality("Martiniquais", NonEea),
    Nationality("Mauritanian", NonEea),
    Nationality("Mauritian", NonEea),
    Nationality("Mexican", NonEea),
    Nationality("Micronesian", NonEea),
    Nationality("Moldovan", NonEea),
    Nationality("Monegasque", NonEea),
    Nationality("Mongolian", NonEea),
    Nationality("Montenegrin", NonEea),
    Nationality("Montserratian", NonEea),
    Nationality("Moroccan", NonEea),
    Nationality("Mosotho", NonEea),
    Nationality("Mozambican", NonEea),
    Nationality("Namibian", NonEea),
    Nationality("Nauruan", NonEea),
    Nationality("Nepalese", NonEea),
    Nationality("New Zealander", NonEea),
    Nationality("Nicaraguan", NonEea),
    Nationality("Nigerian", NonEea),
    Nationality("Nigerien", NonEea),
    Nationality("Niuean", NonEea),
    Nationality("North Korean", NonEea),
    Nationality("Northern Irish", UkCta),
    Nationality("Norwegian", Eea),
    Nationality("Omani", NonEea),
    Nationality("Pakistani", NonEea),
    Nationality("Palauan", NonEea),
    Nationality("Palestinian", NonEea),
    Nationality("Panamanian", NonEea),
    Nationality("Papua New Guinean", NonEea),
    Nationality("Paraguayan", NonEea),
    Nationality("Peruvian", NonEea),
    Nationality("Pitcairn Islander", NonEea),
    Nationality("Polish", Eea),
    Nationality("Portuguese", Eea),
    Nationality("Prydeinig", NonEea),
    Nationality("Puerto Rican", NonEea),
    Nationality("Qatari", NonEea),
    Nationality("Romanian", Eea),
    Nationality("Russian", NonEea),
    Nationality("Rwandan", NonEea),
    Nationality("Salvadorean", NonEea),
    Nationality("Sammarinese", NonEea),
    Nationality("Samoan", NonEea),
    Nationality("Sao Tomean", NonEea),
    Nationality("Saudi Arabian", NonEea),
    Nationality("Scottish", UkCta),
    Nationality("Senegalese", NonEea),
    Nationality("Serbian", NonEea),
    Nationality("Sierra Leonean", NonEea),
    Nationality("Singaporean", NonEea),
    Nationality("Slovak", Eea),
    Nationality("Slovenian", Eea),
    Nationality("Solomon Islander", NonEea),
    Nationality("Somali", NonEea),
    Nationality("South African", NonEea),
    Nationality("South Korean", NonEea),
    Nationality("South Sudanese", NonEea),
    Nationality("Spanish", Eea),
    Nationality("Sri Lankan", NonEea),
    Nationality("St Helenian", NonEea),
    Nationality("St Lucian", NonEea),
    Nationality("Stateless", NonEea),
    Nationality("Sudanese", NonEea),
    Nationality("Surinamese", NonEea),
    Nationality("Swazi", NonEea),
    Nationality("Swedish", Eea),
    Nationality("Swiss", Eea),
    Nationality("Syrian", NonEea),
    Nationality("Taiwanese", NonEea),
    Nationality("Tajik", NonEea),
    Nationality("Tanzanian", NonEea),
    Nationality("Thai", NonEea),
    Nationality("Togolese", NonEea),
    Nationality("Tongan", NonEea),
    Nationality("Trinidadian", NonEea),
    Nationality("Tristanian", NonEea),
    Nationality("Tunisian", NonEea),
    Nationality("Turkish", NonEea),
    Nationality("Turkmen", NonEea),
    Nationality("Turks and Caicos Islander", NonEea),
    Nationality("Tuvaluan", NonEea),
    Nationality("Ugandan", NonEea),
    Nationality("Ukrainian", NonEea),
    Nationality("Uruguayan", NonEea),
    Nationality("Uzbek", NonEea),
    Nationality("Vatican citizen", NonEea),
    Nationality("Venezuelan", NonEea),
    Nationality("Vietnamese", NonEea),
    Nationality("Vincentian", NonEea),
    Nationality("Wallisian", NonEea),
    Nationality("Welsh", UkCta),
    Nationality("Yemeni", NonEea),
    Nationality("Zambian", NonEea),
    Nationality("Zimbabwean", NonEea),
  )

  def selectItems(implicit messages: Messages): Seq[SelectItem] =
    SelectItem(value = None, text = messages("nationality.selectNationality")) +:
      allNationalities.map {
        nationality =>
          SelectItemViewModel(
            value = nationality.name,
            text = nationality.name
          )
      }
}
