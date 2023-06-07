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

package config

import controllers.actions._
import org.apache.fop.apps.FopFactory
import play.api.inject.Binding
import play.api.{Configuration, Environment}
import services.{NoOpSupplementaryDataService, SupplementaryDataService, SupplementaryDataServiceImpl}
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}

import java.time.Clock

class Module extends play.api.inject.Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {

    val authTokenInitialiserBindings: Seq[Binding[_]] =
      if (configuration.get[Boolean]("create-internal-auth-token-on-start")) {
        Seq(bind[InternalAuthTokenInitialiser].to[InternalAuthTokenInitialiserImpl].eagerly())
      } else {
        Seq(bind[InternalAuthTokenInitialiser].to[NoOpInternalAuthTokenInitialiser].eagerly())
      }

    val supplementaryDataServiceBinding: Binding[_] =
      if (configuration.get[Boolean]("features.dmsa-submission")) {
        bind[SupplementaryDataService].to[SupplementaryDataServiceImpl].eagerly()
      } else {
        bind[SupplementaryDataService].to[NoOpSupplementaryDataService].eagerly()
      }

    Seq(
      bind[DataRetrievalAction].to[DataRetrievalActionImpl].eagerly(),
      bind[DataRequiredAction].to[DataRequiredActionImpl].eagerly(),
      bind[Clock].toInstance(Clock.systemUTC()),
      bind[FeatureFlags].toSelf.eagerly(),
      bind[Encrypter with Decrypter].toProvider[CryptoProvider].eagerly(),
      bind[IdentifierAction].to[OptionalAuthIdentifierAction].eagerly(),
      supplementaryDataServiceBinding,
      bind[FopFactory].toProvider[FopFactoryProvider].eagerly(),
    ) ++ authTokenInitialiserBindings
  }
}
