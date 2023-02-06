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

case class BuildingSociety(id: String, name: String)

object BuildingSociety {

  implicit lazy val format: OFormat[BuildingSociety] = Json.format

  val allBuildingSocieties: Seq[BuildingSociety] = Seq(
    BuildingSociety("1", "Abbey / Santander"),
    BuildingSociety("2", "Alliance & Leicester"),
    BuildingSociety("3", "Barnsley"),
    BuildingSociety("4", "Bath Investment"),
    BuildingSociety("5", "Beverley"),
    BuildingSociety("6", "Birmingham Midshires"),
    BuildingSociety("7", "Bradford & Bingley"),
    BuildingSociety("8", "Bristol And West (BN Code)"),
    BuildingSociety("9", "Bristol And West (BZ Code)"),
    BuildingSociety("10", "Britannia (BR Code)"),
    BuildingSociety("11", "Britannia (BW Code)"),
    BuildingSociety("12", "Buckinghamshire"),
    BuildingSociety("13", "Cambridge"),
    BuildingSociety("14", "Catholic"),
    BuildingSociety("15", "Century"),
    BuildingSociety("16", "Chelsea"),
    BuildingSociety("17", "Cheltenham And Gloucester"),
    BuildingSociety("18", "Chesham"),
    BuildingSociety("19", "Cheshire"),
    BuildingSociety("20", "Chorley and District"),
    BuildingSociety("21", "Clay Cross Benefit"),
    BuildingSociety("22", "Coventry"),
    BuildingSociety("23", "Cumberland Merging With West Cumbria"),
    BuildingSociety("24", "Darlington"),
    BuildingSociety("25", "Derbyshire"),
    BuildingSociety("26", "Dudley"),
    BuildingSociety("27", "Dunfermline"),
    BuildingSociety("28", "Earl Shilton"),
    BuildingSociety("29", "Ecology"),
    BuildingSociety("30", "First Active"),
    BuildingSociety("31", "Furness"),
    BuildingSociety("32", "Hanley Economic"),
    BuildingSociety("33", "Harpenden"),
    BuildingSociety("34", "Hinkley And Rugby"),
    BuildingSociety("35", "Holmesdale"),
    BuildingSociety("36", "Ilkeston Permanent"),
    BuildingSociety("37", "Ipswich"),
    BuildingSociety("38", "Irish Permanent"),
    BuildingSociety("39", "Kent Reliance"),
    BuildingSociety("40", "Lambeth"),
    BuildingSociety("41", "Leek United"),
    BuildingSociety("42", "Loughborough"),
    BuildingSociety("43", "Manchester"),
    BuildingSociety("44", "Mansfield"),
    BuildingSociety("45", "Market Harborough"),
    BuildingSociety("46", "Marsden"),
    BuildingSociety("47", "Melton Mowbray"),
    BuildingSociety("48", "Mercantile / Leeds And Holbeck"),
    BuildingSociety("49", "Monmouthshire"),
    BuildingSociety("50", "National Counties"),
    BuildingSociety("51", "National Savings Direct Saver"),
    BuildingSociety("52", "National Savings Investment Account"),
    BuildingSociety("53", "Nationwide"),
    BuildingSociety("54", "Newbury"),
    BuildingSociety("55", "Newcastle"),
    BuildingSociety("56", "Northern Rock / Virgin Money"),
    BuildingSociety("57", "Norwich And Peterborough"),
    BuildingSociety("58", "Nottingham"),
    BuildingSociety("59", "Nottingham Imperial"),
    BuildingSociety("60", "Penrith"),
    BuildingSociety("61", "Portman / Greenwich"),
    BuildingSociety("62", "Principality"),
    BuildingSociety("63", "Progressive"),
    BuildingSociety("64", "Saffron And Walden Herts And Essex"),
    BuildingSociety("65", "Scottish"),
    BuildingSociety("66", "Shepshed"),
    BuildingSociety("67", "Skipton / Scarborough"),
    BuildingSociety("68", "Stafford Railway"),
    BuildingSociety("69", "Staffordshire"),
    BuildingSociety("70", "Standard"),
    BuildingSociety("71", "Standard Life Bank"),
    BuildingSociety("72", "Stroud And Swindon"),
    BuildingSociety("73", "Swansea"),
    BuildingSociety("74", "Teachers"),
    BuildingSociety("75", "Tipton and Coseley"),
    BuildingSociety("76", "Universal"),
    BuildingSociety("77", "Vernon"),
    BuildingSociety("78", "Victoria Mutual"),
    BuildingSociety("79", "West Bromwich"),
    BuildingSociety("80", "Woolwich[SA1]  PLC"),
    BuildingSociety("81", "Yorkshire / Gainsborough"),
    BuildingSociety("82", "First National"),
    BuildingSociety("83", "Gateway"),
    BuildingSociety("84", "Halifax"),
    BuildingSociety("85", "National And Provincial"),
    BuildingSociety("86", "HMG Cash Payment"),
    BuildingSociety("87", "Simple Payment Service[SA2]")
  )

  def selectItems(implicit messages: Messages): Seq[SelectItem] =
    SelectItem(value = None, text = messages("buildingSociety.selectBuildingSociety")) +:
      allBuildingSocieties.map {
        nationality =>
          SelectItemViewModel(
            value = nationality.id,
            text = nationality.name
          )
      }
}
