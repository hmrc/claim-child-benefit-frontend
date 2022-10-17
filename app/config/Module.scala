/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import controllers.actions._
import play.api.inject.Binding
import play.api.{Configuration, Environment}

import java.time.Clock

class Module extends play.api.inject.Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {

    val identifierActionBinding: Binding[_] = if (configuration.get[Boolean]("features.allow-authenticated-sessions")) {
      bind[IdentifierAction].to[OptionalAuthIdentifierAction].eagerly
    } else {
      bind[IdentifierAction].to[SessionIdentifierAction].eagerly
    }

    Seq(
      bind[DataRetrievalAction].to[DataRetrievalActionImpl].eagerly,
      bind[DataRequiredAction].to[DataRequiredActionImpl].eagerly,
      bind[Clock].toInstance(Clock.systemUTC()),
      bind[FeatureFlags].toSelf.eagerly,
      identifierActionBinding
    )
  }
}
