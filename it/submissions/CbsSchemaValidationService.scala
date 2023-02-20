package submissions


import io.vertx.core.json.JsonObject
import io.vertx.json.schema.{Draft, JsonSchema, JsonSchemaOptions, Validator}
import play.api.Environment
import play.api.libs.json.{JsValue, Json}

import javax.inject.{Inject, Singleton}
import scala.io.Source
import scala.collection.JavaConverters._

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
    val isValid = result.getValid

    if (!isValid) {
      println("*"*100)
      println(Json.prettyPrint(json))
      println("*"*100)

      result.getErrors.asScala.foreach { error =>
        println(error)
      }

      println("*"*100)
    }

    isValid
  }
}

