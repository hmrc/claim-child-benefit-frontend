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

package services

import connectors.BarsConnector
import models.{BankAccountDetails, InvalidJson, ReputationResponseEnum, ValidateBankDetailsResponseModel}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BarsServiceSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with OptionValues
    with ScalaFutures{

  ".validate" - {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    "must return the BARS response when the connector returns them" in {

      val barsResponse = ValidateBankDetailsResponseModel(
        accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
        nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.Yes,
        sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
        sortCodeSupportsDirectCredit = None
      )

      val mockConnector = mock[BarsConnector]
      when(mockConnector.validate(any())(any())) thenReturn Future.successful(Right(barsResponse))


      val app =
        new GuiceApplicationBuilder()
          .overrides(bind[BarsConnector].toInstance(mockConnector))
          .build()

      running(app) {

        val service = app.injector.instanceOf[BarsService]

        val bankDetails = BankAccountDetails("name on account", "bank name", "123456", "00123456", None)

        val result = service.validateBankDetails(bankDetails).futureValue

        result.value mustEqual barsResponse
      }
    }

    "must return None when the connector returns an error response" in {

      val mockConnector = mock[BarsConnector]
      when(mockConnector.validate(any())(any())) thenReturn Future.successful(Left(InvalidJson))


      val app =
        new GuiceApplicationBuilder()
          .overrides(bind[BarsConnector].toInstance(mockConnector))
          .build()

      running(app) {

        val service = app.injector.instanceOf[BarsService]

        val bankDetails = BankAccountDetails("name on account", "bank name", "123456", "00123456", None)

        val result = service.validateBankDetails(bankDetails).futureValue

        result must not be defined
      }
    }
  }
}
