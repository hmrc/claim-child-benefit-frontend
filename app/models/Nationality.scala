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

final case class Nationality(name: String, group: NationalityGroup, key: String) {
  def message(implicit messages: Messages): String = messages(key)
}

object Nationality {

  import NationalityGroup._

  implicit lazy val format: OFormat[Nationality] = Json.format

  val allNationalities: Seq[Nationality] = Seq(
    Nationality("Afghan", NonEea, "nationality.afghan"),
    Nationality("Albanian", NonEea, "nationality.albanian"),
    Nationality("Algerian", NonEea, "nationality.algerian"),
    Nationality("American", NonEea, "nationality.american"),
    Nationality("Andorran", NonEea, "nationality.andorran"),
    Nationality("Angolan", NonEea, "nationality.angolan"),
    Nationality("Anguillan", NonEea, "nationality.anguillan"),
    Nationality("Argentine", NonEea, "nationality.argentine"),
    Nationality("Armenian", NonEea, "nationality.armenian"),
    Nationality("Australian", NonEea, "nationality.australian"),
    Nationality("Austrian", Eea, "nationality.austrian"),
    Nationality("Azerbaijani", NonEea, "nationality.azerbaijani"),
    Nationality("Bahamian", NonEea, "nationality.bahamian"),
    Nationality("Bahraini", NonEea, "nationality.bahraini"),
    Nationality("Bangladeshi", NonEea, "nationality.bangladeshi"),
    Nationality("Barbadian", NonEea, "nationality.barbadian"),
    Nationality("Belarusian", NonEea, "nationality.belarusian"),
    Nationality("Belgian", Eea, "nationality.belgian"),
    Nationality("Belizean", NonEea, "nationality.belizean"),
    Nationality("Beninese", NonEea, "nationality.beninese"),
    Nationality("Bermudian", NonEea, "nationality.bermudian"),
    Nationality("Bhutanese", NonEea, "nationality.bhutanese"),
    Nationality("Bolivian", NonEea, "nationality.bolivian"),
    Nationality("Botswanan", NonEea, "nationality.botswanan"),
    Nationality("Brazilian", NonEea, "nationality.brazilian"),
    Nationality("British", UkCta, "nationality.british"),
    Nationality("British Virgin Islander", NonEea, "nationality.britishVirginIslander"),
    Nationality("Bruneian", NonEea, "nationality.bruneian"),
    Nationality("Bulgarian", Eea, "nationality.bulgarian"),
    Nationality("Burkinan", NonEea, "nationality.burkinan"),
    Nationality("Burmese", NonEea, "nationality.burmese"),
    Nationality("Burundian", NonEea, "nationality.burundian"),
    Nationality("Cambodian", NonEea, "nationality.cambodian"),
    Nationality("Cameroonian", NonEea, "nationality.cameroonian"),
    Nationality("Canadian", NonEea, "nationality.canadian"),
    Nationality("Cape Verdean", NonEea, "nationality.capeVerdean"),
    Nationality("Cayman Islander", NonEea, "nationality.caymanIslander"),
    Nationality("Central African", NonEea, "nationality.centralAfrican"),
    Nationality("Chadian", NonEea, "nationality.chadian"),
    Nationality("Chilean", NonEea, "nationality.chilean"),
    Nationality("Chinese", NonEea, "nationality.chinese"),
    Nationality("Citizen of Antigua and Barbuda", NonEea, "nationality.citizenofAntiguaAndBarbuda"),
    Nationality("Citizen of Bosnia and Herzegovina", NonEea, "nationality.citizenOfBosniaAndHerzegovina"),
    Nationality("Citizen of Guinea-Bissau", NonEea, "nationality.citizenOfGuineaBissau"),
    Nationality("Citizen of Kiribati", NonEea, "nationality.citizenOfKiribati"),
    Nationality("Citizen of Seychelles", NonEea, "nationality.citizenOfSeychelles"),
    Nationality("Citizen of the Dominican Republic", NonEea, "nationality.citizenOfTheDominicanRepublic"),
    Nationality("Citizen of Vanuatu", NonEea, "nationality.citizenOfVanuatu"),
    Nationality("Colombian", NonEea, "nationality.colombian"),
    Nationality("Comoran", NonEea, "nationality.comoran"),
    Nationality("Congolese (Congo)", NonEea, "nationality.congolese"),
    Nationality("Congolese (DRC)", NonEea, "nationality.congoleseDRC"),
    Nationality("Cook Islander", NonEea, "nationality.cookIslander"),
    Nationality("Costa Rican", NonEea, "nationality.costaRican"),
    Nationality("Croatian", Eea, "nationality.croatian"),
    Nationality("Cuban", NonEea, "nationality.cuban"),
    Nationality("Cymraes", UkCta, "nationality.cymraes"),
    Nationality("Cymro", UkCta, "nationality.cymro"),
    Nationality("Cypriot", Eea, "nationality.cypriot"),
    Nationality("Czech", Eea, "nationality.czech"),
    Nationality("Danish", Eea, "nationality.danish"),
    Nationality("Djiboutian", NonEea, "nationality.djiboutian"),
    Nationality("Dominican", NonEea, "nationality.dominican"),
    Nationality("Dutch", Eea, "nationality.dutch"),
    Nationality("East Timorese", NonEea, "nationality.eastTimorese"),
    Nationality("Ecuadorean", NonEea, "nationality.ecuadorean"),
    Nationality("Egyptian", NonEea, "nationality.egyptian"),
    Nationality("Emirati", NonEea, "nationality.emirati"),
    Nationality("English", UkCta, "nationality.english"),
    Nationality("Equatorial Guinean", NonEea, "nationality.equatorialGuinean"),
    Nationality("Eritrean", NonEea, "nationality.eritrean"),
    Nationality("Estonian", Eea, "nationality.estonian"),
    Nationality("Ethiopian", NonEea, "nationality.ethiopian"),
    Nationality("Faroese", NonEea, "nationality.faroese"),
    Nationality("Fijian", NonEea, "nationality.fijian"),
    Nationality("Filipino", NonEea, "nationality.filipino"),
    Nationality("Finnish", Eea, "nationality.finnish"),
    Nationality("French", Eea, "nationality.french"),
    Nationality("Gabonese", NonEea, "nationality.gabonese"),
    Nationality("Gambian", NonEea, "nationality.gambian"),
    Nationality("Georgian", NonEea, "nationality.georgian"),
    Nationality("German", Eea, "nationality.german"),
    Nationality("Ghanaian", NonEea, "nationality.ghanaian"),
    Nationality("Gibraltarian", NonEea, "nationality.gibraltarian"),
    Nationality("Greek", Eea, "nationality.greek"),
    Nationality("Greenlandic", NonEea, "nationality.greenlandic"),
    Nationality("Grenadian", NonEea, "nationality.grenadian"),
    Nationality("Guamanian", NonEea, "nationality.guamanian"),
    Nationality("Guatemalan", NonEea, "nationality.guatemalan"),
    Nationality("Guinean", NonEea, "nationality.guinean"),
    Nationality("Guyanese", NonEea, "nationality.guyanese"),
    Nationality("Haitian", NonEea, "nationality.haitian"),
    Nationality("Honduran", NonEea, "nationality.honduran"),
    Nationality("Hong Konger", NonEea, "nationality.hongKonger"),
    Nationality("Hungarian", Eea, "nationality.hungarian"),
    Nationality("Icelandic", Eea, "nationality.icelandic"),
    Nationality("Indian", NonEea, "nationality.indian"),
    Nationality("Indonesian", NonEea, "nationality.indonesian"),
    Nationality("Iranian", NonEea, "nationality.iranian"),
    Nationality("Iraqi", NonEea, "nationality.iraqi"),
    Nationality("Irish", UkCta, "nationality.irish"),
    Nationality("Israeli", NonEea, "nationality.israeli"),
    Nationality("Italian", Eea, "nationality.italian"),
    Nationality("Ivorian", NonEea, "nationality.ivorian"),
    Nationality("Jamaican", NonEea, "nationality.jamaican"),
    Nationality("Japanese", NonEea, "nationality.japanese"),
    Nationality("Jordanian", NonEea, "nationality.jordanian"),
    Nationality("Kazakh", NonEea, "nationality.kazakh"),
    Nationality("Kenyan", NonEea, "nationality.kenyan"),
    Nationality("Kittitian", NonEea, "nationality.kittitian"),
    Nationality("Kosovan", NonEea, "nationality.kosovan"),
    Nationality("Kuwaiti", NonEea, "nationality.kuwaiti"),
    Nationality("Kyrgyz", NonEea, "nationality.kyrgyz"),
    Nationality("Lao", NonEea, "nationality.lao"),
    Nationality("Latvian", Eea, "nationality.latvian"),
    Nationality("Lebanese", NonEea, "nationality.lebanese"),
    Nationality("Liberian", NonEea, "nationality.liberian"),
    Nationality("Libyan", NonEea, "nationality.libyan"),
    Nationality("Liechtenstein citizen", Eea, "nationality.liechtenstein"),
    Nationality("Lithuanian", Eea, "nationality.lithuanian"),
    Nationality("Luxembourger", Eea, "nationality.luxembourger"),
    Nationality("Macanese", NonEea, "nationality.macanese"),
    Nationality("Macedonian", NonEea, "nationality.macedonian"),
    Nationality("Malagasy", NonEea, "nationality.malagasy"),
    Nationality("Malawian", NonEea, "nationality.malawian"),
    Nationality("Malaysian", NonEea, "nationality.malaysian"),
    Nationality("Maldivian", NonEea, "nationality.maldivian"),
    Nationality("Malian", NonEea, "nationality.malian"),
    Nationality("Maltese", Eea, "nationality.maltese"),
    Nationality("Marshallese", NonEea, "nationality.marshallese"),
    Nationality("Martiniquais", NonEea, "nationality.martiniquais"),
    Nationality("Mauritanian", NonEea, "nationality.mauritanian"),
    Nationality("Mauritian", NonEea, "nationality.mauritian"),
    Nationality("Mexican", NonEea, "nationality.mexican"),
    Nationality("Micronesian", NonEea, "nationality.micronesian"),
    Nationality("Moldovan", NonEea, "nationality.moldovan"),
    Nationality("Monegasque", NonEea, "nationality.monegasque"),
    Nationality("Mongolian", NonEea, "nationality.mongolian"),
    Nationality("Montenegrin", NonEea, "nationality.montenegrin"),
    Nationality("Montserratian", NonEea, "nationality.montserratian"),
    Nationality("Moroccan", NonEea, "nationality.moroccan"),
    Nationality("Mosotho", NonEea, "nationality.mosotho"),
    Nationality("Mozambican", NonEea, "nationality.mozambican"),
    Nationality("Namibian", NonEea, "nationality.namibian"),
    Nationality("Nauruan", NonEea, "nationality.nauruan"),
    Nationality("Nepalese", NonEea, "nationality.nepalese"),
    Nationality("New Zealander", NonEea, "nationality.newZealander"),
    Nationality("Nicaraguan", NonEea, "nationality.nicaraguan"),
    Nationality("Nigerian", NonEea, "nationality.nigerian"),
    Nationality("Nigerien", NonEea, "nationality.nigerien"),
    Nationality("Niuean", NonEea, "nationality.niuean"),
    Nationality("North Korean", NonEea, "nationality.northKorean"),
    Nationality("Northern Irish", UkCta, "nationality.northernIrish"),
    Nationality("Norwegian", Eea, "nationality.norwegian"),
    Nationality("Omani", NonEea, "nationality.omani"),
    Nationality("Pakistani", NonEea, "nationality.pakistani"),
    Nationality("Palauan", NonEea, "nationality.palauan"),
    Nationality("Palestinian", NonEea, "nationality.palestinian"),
    Nationality("Panamanian", NonEea, "nationality.panamanian"),
    Nationality("Papua New Guinean", NonEea, "nationality.papuaNewGuinean"),
    Nationality("Paraguayan", NonEea, "nationality.paraguayan"),
    Nationality("Peruvian", NonEea, "nationality.peruvian"),
    Nationality("Pitcairn Islander", NonEea, "nationality.pitcairnIslander"),
    Nationality("Polish", Eea, "nationality.polish"),
    Nationality("Portuguese", Eea, "nationality.portuguese"),
    Nationality("Prydeinig", UkCta, "nationality.prydeinig"),
    Nationality("Puerto Rican", NonEea, "nationality.puertoRican"),
    Nationality("Qatari", NonEea, "nationality.qatari"),
    Nationality("Romanian", Eea, "nationality.romanian"),
    Nationality("Russian", NonEea, "nationality.russian"),
    Nationality("Rwandan", NonEea, "nationality.rwandan"),
    Nationality("Salvadorean", NonEea, "nationality.salvadorean"),
    Nationality("Sammarinese", NonEea, "nationality.sammarinese"),
    Nationality("Samoan", NonEea, "nationality.samoan"),
    Nationality("Sao Tomean", NonEea, "nationality.saoTomean"),
    Nationality("Saudi Arabian", NonEea, "nationality.saudiArabian"),
    Nationality("Scottish", UkCta, "nationality.scottish"),
    Nationality("Senegalese", NonEea, "nationality.senegalese"),
    Nationality("Serbian", NonEea, "nationality.serbian"),
    Nationality("Sierra Leonean", NonEea, "nationality.sierraLeonean"),
    Nationality("Singaporean", NonEea, "nationality.singaporean"),
    Nationality("Slovak", Eea, "nationality.slovak"),
    Nationality("Slovenian", Eea, "nationality.slovenian"),
    Nationality("Solomon Islander", NonEea, "nationality.solomonIslander"),
    Nationality("Somali", NonEea, "nationality.somali"),
    Nationality("South African", NonEea, "nationality.southAfrican"),
    Nationality("South Korean", NonEea, "nationality.southKorean"),
    Nationality("South Sudanese", NonEea, "nationality.southSudanese"),
    Nationality("Spanish", Eea, "nationality.spanish"),
    Nationality("Sri Lankan", NonEea, "nationality.sriLankan"),
    Nationality("St Helenian", NonEea, "nationality.stHelenian"),
    Nationality("St Lucian", NonEea, "nationality.stLucian"),
    Nationality("Stateless", NonEea, "nationality.stateless"),
    Nationality("Sudanese", NonEea, "nationality.sudanese"),
    Nationality("Surinamese", NonEea, "nationality.surinamese"),
    Nationality("Swazi", NonEea, "nationality.swazi"),
    Nationality("Swedish", Eea, "nationality.swedish"),
    Nationality("Swiss", Eea, "nationality.swiss"),
    Nationality("Syrian", NonEea, "nationality.syrian"),
    Nationality("Taiwanese", NonEea, "nationality.taiwanese"),
    Nationality("Tajik", NonEea, "nationality.tajik"),
    Nationality("Tanzanian", NonEea, "nationality.tanzanian"),
    Nationality("Thai", NonEea, "nationality.thai"),
    Nationality("Togolese", NonEea, "nationality.togolese"),
    Nationality("Tongan", NonEea, "nationality.tongan"),
    Nationality("Trinidadian", NonEea, "nationality.trinidadian"),
    Nationality("Tristanian", NonEea, "nationality.tristanian"),
    Nationality("Tunisian", NonEea, "nationality.tunisian"),
    Nationality("Turkish", NonEea, "nationality.turkish"),
    Nationality("Turkmen", NonEea, "nationality.turkmen"),
    Nationality("Turks and Caicos Islander", NonEea, "nationality.turksAndCaicosIslander"),
    Nationality("Tuvaluan", NonEea, "nationality.tuvaluan"),
    Nationality("Ugandan", NonEea, "nationality.ugandan"),
    Nationality("Ukrainian", NonEea, "nationality.ukrainian"),
    Nationality("Uruguayan", NonEea, "nationality.uruguayan"),
    Nationality("Uzbek", NonEea, "nationality.uzbek"),
    Nationality("Vatican citizen", NonEea, "nationality.vaticanCitizen"),
    Nationality("Venezuelan", NonEea, "nationality.venezuelan"),
    Nationality("Vietnamese", NonEea, "nationality.vietnamese"),
    Nationality("Vincentian", NonEea, "nationality.vincentian"),
    Nationality("Wallisian", NonEea, "nationality.wallisian"),
    Nationality("Welsh", UkCta, "nationality.welsh"),
    Nationality("Yemeni", NonEea, "nationality.yemeni"),
    Nationality("Zambian", NonEea, "nationality.zambian"),
    Nationality("Zimbabwean", NonEea, "nationality.zimbabwean"),
  )

  def selectItems(implicit messages: Messages): Seq[SelectItem] =
    SelectItem(value = None, text = messages("nationality.selectNationality")) +:
      allNationalities.map {
        nationality =>
          SelectItemViewModel(
            value = nationality.name,
            text = messages(nationality.key)
          )
      }
}
