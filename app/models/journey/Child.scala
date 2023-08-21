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

package models.journey

import cats.data._
import cats.implicits._
import models.ApplicantRelationshipToChild.AdoptedChild
import models.ChildBirthRegistrationCountry._
import models.DocumentType.{AdoptionCertificate, BirthCertificate, TravelDocument}
import models._
import pages.child._
import play.api.i18n.Messages
import queries.{AllChildPreviousNames, AllChildSummaries, Query}

import java.time.LocalDate

final case class Child(
                        name: ChildName,
                        nameChangedByDeedPoll: Option[Boolean],
                        previousNames: List[ChildName],
                        biologicalSex: ChildBiologicalSex,
                        dateOfBirth: LocalDate,
                        countryOfRegistration: ChildBirthRegistrationCountry,
                        birthCertificateNumber: Option[BirthCertificateNumber],
                        birthCertificateDetailsMatched: BirthRegistrationMatchingResult,
                        relationshipToApplicant: ApplicantRelationshipToChild,
                        adoptingThroughLocalAuthority: Boolean,
                        previousClaimant: Option[PreviousClaimant],
                        guardian: Option[Guardian],
                        previousGuardian: Option[PreviousGuardian],
                        dateChildStartedLivingWithApplicant: Option[LocalDate]
                      ) {

  private val adoptionCertificate =
    if (relationshipToApplicant == AdoptedChild) Some(AdoptionCertificate) else None

  private val (birthCertificate, travelDocument) = countryOfRegistration match {
    case England | Scotland | Wales | NorthernIreland => (None, None)
    case _ if previousClaimant.isDefined              => (None, None)
    case _                                            => (Some(BirthCertificate), Some(TravelDocument))
  }

  val requiredDocuments: Seq[DocumentType] =
    Seq(birthCertificate, travelDocument, adoptionCertificate).flatten

  val possiblyLivedAbroadSeparately: Boolean =
    if (dateChildStartedLivingWithApplicant.exists(_.isAfter(LocalDate.now.minusMonths(3)))) {
      previousGuardian.exists { previousGuardian =>
        previousGuardian.address.forall {
          case _: InternationalAddress => true
          case _ => false
        }
      }
    } else {
      false
    }

  def possiblyRecentlyCaredForByLocalAuthority(implicit messages: Messages): Boolean =
    if (dateChildStartedLivingWithApplicant.exists(_.isAfter(LocalDate.now.minusMonths(3)))) {
      previousGuardian.exists(_.address.exists(_.possibleLocalAuthorityAddress))
    } else {
      false
    }
}

object Child {

  def buildChildren(answers: UserAnswers): IorNec[Query, NonEmptyList[Child]] =
    answers.getIor(AllChildSummaries).getOrElse(Nil).indices.toList.parTraverse { i =>
      buildChild(answers, Index(i))
    }.flatMap { children =>
      NonEmptyList.fromList(children).toRightIor(NonEmptyChain.one(AllChildSummaries))
    }

  private def buildChild(answers: UserAnswers, index: Index): IorNec[Query, Child] =
    (
      answers.getIor(ChildNamePage(index)),
      getNameChangedByDeedPoll(answers, index),
      getPreviousNames(answers, index),
      answers.getIor(ChildBiologicalSexPage(index)),
      answers.getIor(ChildDateOfBirthPage(index)),
      answers.getIor(ChildBirthRegistrationCountryPage(index)),
      getBirthCertificateNumber(answers, index),
      Ior.Right(BirthRegistrationMatchingResult.NotAttempted),
      answers.getIor(ApplicantRelationshipToChildPage(index)),
      answers.getIor(AdoptingThroughLocalAuthorityPage(index)),
      PreviousClaimant.build(answers, index),
      Guardian.build(answers, index),
      PreviousGuardian.build(answers, index),
      getDateChildStartedLivingWithApplicant(answers, index)
    ).parMapN(Child.apply)

  private def getNameChangedByDeedPoll(answers: UserAnswers, index: Index): IorNec[Query, Option[Boolean]] =
    answers.getIor(ChildHasPreviousNamePage(index)).flatMap {
      case true => answers.getIor(ChildNameChangedByDeedPollPage(index)).map(Some(_))
      case false => Ior.Right(None)
    }

  private def getPreviousNames(answers: UserAnswers, index: Index): IorNec[Query, List[ChildName]] =
    answers.getIor(ChildHasPreviousNamePage(index)).flatMap {
      case true => answers.getIor(AllChildPreviousNames(index))
      case false => Ior.Right(Nil)
    }

  private def getBirthCertificateNumber(answers: UserAnswers, index: Index): IorNec[Query, Option[BirthCertificateNumber]] =
    answers.getIor(ChildBirthRegistrationCountryPage(index)).flatMap {
      case England | Wales =>
        answers.getIor(BirthCertificateHasSystemNumberPage(index)).flatMap {
          case true =>
            answers.getIor(ChildBirthCertificateSystemNumberPage(index)).map(Some(_))
          case false =>
            Ior.Right(None)
        }

      case Scotland =>
        answers.getIor(ScottishBirthCertificateHasNumbersPage(index)).flatMap {
          case true =>
            answers.getIor(ChildScottishBirthCertificateDetailsPage(index)).map(x => Some(x))
          case false =>
            Ior.Right(None)
        }

      case NorthernIreland =>
        answers.getIor(BirthCertificateHasNorthernIrishNumberPage(index)).flatMap {
          case true =>
            answers.getIor(ChildNorthernIrishBirthCertificateNumberPage(index)).map(Some(_))
          case false =>
            Ior.Right(None)
        }

      case _ =>
        Ior.Right(None)
    }

  private def getDateChildStartedLivingWithApplicant(answers: UserAnswers, index: Index): IorNec[Query, Option[LocalDate]] =
    answers.getIor(ChildLivesWithApplicantPage(index)).flatMap {
      case true =>
        answers.getIor(ChildLivedWithAnyoneElsePage(index)).flatMap {
          case true =>
            answers.getIor(DateChildStartedLivingWithApplicantPage(index)).map(Some(_))

          case false =>
            Ior.Right(None)
        }

      case false =>
        Ior.Right(None)
    }
}
