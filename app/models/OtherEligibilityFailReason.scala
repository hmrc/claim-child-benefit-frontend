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

package models

sealed trait OtherEligibilityFailReason

object OtherEligibilityFailReason extends Enumerable.Implicits {

  case object ApplicantWorkedAbroad extends WithName("applicantWorkedAbroadInLast3Months") with OtherEligibilityFailReason
  case object ApplicantReceivedBenefitsAbroad extends WithName("applicantReceivedBenefitsAbroadInLast3Months") with OtherEligibilityFailReason
  case object PartnerWorkedAbroad extends WithName("partnerWorkedAbroadInLast3Months") with OtherEligibilityFailReason
  case object PartnerReceivedBenefitsAbroad extends WithName("partnerReceivedBenefitsAbroadInLast3Months") with OtherEligibilityFailReason
  case object ChildRecentlyLivedElsewhere extends WithName("childRecentlyLivedWithSomeoneElse") with OtherEligibilityFailReason
  case object BankAccountInsightsRisk extends WithName("bankAccountInsightsIndicatesPossibleRisk") with OtherEligibilityFailReason
  case object ChildPossiblyRecentlyUnderLocalAuthorityCare extends WithName("childPossiblyRecentlyUnderLocalAuthorityCare") with OtherEligibilityFailReason

  val values: Seq[OtherEligibilityFailReason] = Seq(
    ApplicantWorkedAbroad,
    ApplicantReceivedBenefitsAbroad,
    PartnerWorkedAbroad,
    PartnerReceivedBenefitsAbroad,
    ChildRecentlyLivedElsewhere,
    BankAccountInsightsRisk,
    ChildPossiblyRecentlyUnderLocalAuthorityCare
  )

  implicit val enumerable: Enumerable[OtherEligibilityFailReason] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
