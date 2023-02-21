package submissions

import generators.ModelGenerators
import models.domain._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Environment
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino

import java.time.{Clock, Instant, LocalDate, ZoneId}

class SubmitClaimSpec extends AnyFreeSpec with Matchers with ModelGenerators with OptionValues {

  private val fixedInstant: Instant = LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant
  private val clockAtFixedInstant: Clock = Clock.fixed(fixedInstant, ZoneId.systemDefault)
  private val app = new GuiceApplicationBuilder().overrides(bind[Clock].toInstance(clockAtFixedInstant)).build()
  private val environment = app.injector.instanceOf[Environment]
  private val validator = new CbsSchemaValidationService(environment)
  private val applicantNino = arbitrary[Nino].sample.value.nino
  private val partnerNino = arbitrary[Nino].sample.value.nino

  "a Claim must generate a JSON payload that passes schema validation" - {

    "for a payload with a UK applicant and E/W child" in {

      val claim = Claim(
        dateOfClaim = LocalDate.now,
        claimant = UkCtaClaimantAlwaysResident(nino = applicantNino, hmfAbroad = false, hicbcOptOut = true),
        partner = None,
        payment = None,
        children = List(
          Child(
            name = ChildName("first", None, "last"),
            gender = BiologicalSex.Female,
            dateOfBirth = LocalDate.now,
            birthRegistrationNumber = None,
            crn = None,
            countryOfRegistration = CountryOfRegistration.EnglandWales,
            dateOfBirthVerified = None,
            livingWithClaimant = true,
            claimantIsParent = true,
            adoptionStatus = false
          )
        ),
        otherEligibilityFailure = false
      )

      val json = Json.toJson(claim)
      validator.validateCbsClaim(json) mustBe true
    }

    "for a payload with a UK applicant who hasn't always lived abroad and S child" in {

      val claim = Claim(
        dateOfClaim = LocalDate.now,
        claimant = UkCtaClaimantNotAlwaysResident(nino = applicantNino, hmfAbroad = false, hicbcOptOut = true, last3MonthsInUK = true),
        partner = Some(Partner(partnerNino, "partner surname")),
        payment = Some(Payment(PaymentFrequency.Weekly, None)),
        children = List(
          Child(
            name = ChildName("first", None, "last"),
            gender = BiologicalSex.Unspecified,
            dateOfBirth = LocalDate.now,
            birthRegistrationNumber = Some("1234567890"),
            crn = None,
            countryOfRegistration = CountryOfRegistration.Scotland,
            dateOfBirthVerified = None,
            livingWithClaimant = true,
            claimantIsParent = true,
            adoptionStatus = false
          )
        ),
        otherEligibilityFailure = false
      )

      val json = Json.toJson(claim)
      validator.validateCbsClaim(json) mustBe true
    }

    "for a payload with a non-UK applicant who has always lived abroad and child registered abroad" in {

      val claim = Claim(
        dateOfClaim = LocalDate.now,
        claimant = NonUkCtaClaimantAlwaysResident(
          nino = applicantNino,
          hmfAbroad = false,
          hicbcOptOut = true,
          nationality = Nationality.Eea,
          rightToReside = false
        ),
        partner = None,
        payment = Some(Payment(
          PaymentFrequency.Weekly,
          Some(BankDetails(ClaimantAccountHolder, BankAccount("123456", "12345678")))
        )),
        children = List(
          Child(
            name = ChildName("first", None, "last"),
            gender = BiologicalSex.Male,
            dateOfBirth = LocalDate.now,
            birthRegistrationNumber = None,
            crn = None,
            countryOfRegistration = CountryOfRegistration.Abroad,
            dateOfBirthVerified = Some(false),
            livingWithClaimant = false,
            claimantIsParent = true,
            adoptionStatus = false
          )
        ),
        otherEligibilityFailure = false
      )

      val json = Json.toJson(claim)
      validator.validateCbsClaim(json) mustBe true
    }

    "for a payload with a non-UK applicant who hasn't always lived abroad and child registered abroad" in {

      val claim = Claim(
        dateOfClaim = LocalDate.now,
        claimant = NonUkCtaClaimantNotAlwaysResident(
          nino = applicantNino,
          hmfAbroad = false,
          hicbcOptOut = true,
          last3MonthsInUK = true,
          nationality = Nationality.NonEea,
          rightToReside = false
        ),
        partner = None,
        payment = Some(Payment(
          PaymentFrequency.Weekly,
          Some(BuildingSocietyDetails(ClaimantAccountHolder, BuildingSocietyAccount("1", "123/456")))
        )),
        children = List(
          Child(
            name = ChildName("first", None, "last"),
            gender = BiologicalSex.Male,
            dateOfBirth = LocalDate.now,
            birthRegistrationNumber = None,
            crn = None,
            countryOfRegistration = CountryOfRegistration.Abroad,
            dateOfBirthVerified = Some(false),
            livingWithClaimant = true,
            claimantIsParent = true,
            adoptionStatus = true
          )
        ),
        otherEligibilityFailure = true
      )

      val json = Json.toJson(claim)
      validator.validateCbsClaim(json) mustBe true
    }
  }
}
