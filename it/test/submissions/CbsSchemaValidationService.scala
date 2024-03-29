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

package submissions

import io.vertx.core.json.JsonObject
import io.vertx.json.schema.{Draft, JsonSchema, JsonSchemaOptions, Validator}
import play.api.Environment
import play.api.libs.json.{JsValue, Json}

import javax.inject.{Inject, Singleton}
import scala.io.Source

@Singleton
class CbsSchemaValidationService @Inject() (
                                             environment: Environment
                                           ) {

  private val cbsClaimValidator = {
    val jsonString =
      Source.fromInputStream(environment.resourceAsStream("schemas/api-1984-schema-1-0-0.json").get).mkString
    val json = new JsonObject(jsonString)
    val schema = JsonSchema.of(json)
    Validator.create(schema, new JsonSchemaOptions().setDraft(Draft.DRAFT7).setBaseUri("http://tax.service.gov.uk"))
  }

  def validateCbsClaim(json: JsValue): Boolean = {

    val jsonObject = new JsonObject(Json.stringify(json))
    val result = cbsClaimValidator.validate(jsonObject)
    result.getValid
  }
}

