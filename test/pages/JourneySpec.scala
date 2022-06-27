package pages

import models.UserAnswers
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class JourneySpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {


  "foo" - {

    // Ever Worked or Lived Abroad (answering yes) to Any Child lived with Others

    val answers =
      UserAnswers("id")
        .set(EverLivedOrWorkedAbroadPage, true).success.value
        .set(AnyChildLivedWithOthersPage, false).success.value

    val waypoints = EmptyWaypoints

    val x = EverLivedOrWorkedAbroadPage.navigate(waypoints, answers).next(answers)

    val expectedSteps = Seq(
      PageAndWaypoints(AnyChildLivedWithOthersPage, waypoints),
    )

  }
}
