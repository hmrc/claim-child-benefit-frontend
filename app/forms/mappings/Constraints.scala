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

package forms.mappings

import models.Index
import play.api.data.validation.{Constraint, Invalid, Valid}

import java.time.LocalDate
import scala.util.matching.Regex

trait Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, maximum)
        }
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String, args: Any*)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum && input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, Seq(minimum, maximum) ++ args: _*)
        }
    }

  protected def regexp(regex: Regex, errorKey: String, args: Any*): Constraint[String] =
    Constraint {
      case str if str.matches(regex.toString) =>
        Valid
      case _ =>
        Invalid(errorKey, regex.toString +: args: _*)
    }

  protected def maxLength(maximum: Int, errorKey: String, args: Any*): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum +: args: _*)
    }

  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def nonEmptySet(errorKey: String, args: Any*): Constraint[Set[_]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey, args: _*)
    }

  protected def noMutuallyExclusiveAnswers[A](set1: Set[A], set2: Set[A], errorKey: String, args: Any*): Constraint[Set[A]] =
    Constraint {
      case set if set.intersect(set1).nonEmpty && set.intersect(set2).nonEmpty =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def notADuplicate[A](index: Index, existingAnswers: Seq[A], errorKey: String, args: Any*): Constraint[A] = {

    val indexedAnswers = existingAnswers.zipWithIndex
    val filteredAnswers = indexedAnswers.filter(_._2 != index.position)

    Constraint {
      case answer if filteredAnswers.map(_._1) contains answer =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }
  }
}
