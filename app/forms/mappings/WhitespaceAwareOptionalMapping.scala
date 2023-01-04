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

import play.api.data.{FormError, Mapping}
import play.api.data.validation.Constraint

final case class WhitespaceAwareOptionalMapping[A](
                                                    wrapped: Mapping[A],
                                                    additionalConstraints: Seq[Constraint[Option[A]]] = Nil
                                                  ) extends Mapping[Option[A]] {

  override val key: String = wrapped.key

  override val mappings: Seq[Mapping[_]] = wrapped.mappings

  override def constraints: Seq[Constraint[Option[A]]] = additionalConstraints

  override def bind(data: Map[String, String]): Either[Seq[FormError], Option[A]] =
    data.get(key).filter(_.trim.nonEmpty).map { _ =>
      wrapped.bind(data).right.map(a => Some(a)).flatMap(applyConstraints)
    }.getOrElse(applyConstraints(None))

  override def unbind(value: Option[A]): Map[String, String] =
    value.map(wrapped.unbind).getOrElse(Map.empty)

  override def unbindAndValidate(value: Option[A]): (Map[String, String], Seq[FormError]) =
    value.map(wrapped.unbindAndValidate).getOrElse((Map.empty, Seq.empty))

  override def withPrefix(prefix: String): Mapping[Option[A]] =
    copy(wrapped.withPrefix(prefix))

  override def verifying(constraints: Constraint[Option[A]]*): Mapping[Option[A]] =
    copy(additionalConstraints = additionalConstraints ++ constraints)
}