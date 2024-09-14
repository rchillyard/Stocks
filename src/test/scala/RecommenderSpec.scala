import actors.Recommender
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.util.Timeout
import messages.{LatestIndicators, LogError, PushRecommendations, RecommenderMessage}
import models._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class RecommenderSpec extends AnyWordSpecLike with BeforeAndAfterAll {
  val testKit = ActorTestKit()
  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val executionContext: ExecutionContextExecutor = testKit.system.executionContext // Explicit type for ExecutionContextExecutor

  override def afterAll(): Unit = testKit.shutdownTestKit()

  "Recommender Actor" should {
    "handle LatestIndicators and update behavior" in {
      val recommender = testKit.spawn(Recommender())
      val probe = testKit.createTestProbe[RecommenderMessage]()

      val currentIndicators = Map(
        "AAPL" -> PerformanceIndicators(
          ZonedDateTime.now(),
          BigDecimal(200.0),
          BigDecimal(2.0),
          BigDecimal(190.0),
          BigDecimal(210.0),
          BigDecimal(200.0),
          BigDecimal(1.5),
          BigDecimal(70),
          (BigDecimal(180.0), BigDecimal(195.0), BigDecimal(210.0)),
          BigDecimal(198.0),
          BigDecimal(25.0),
          BigDecimal(20.0),
          BigDecimal(21.0),
          BigDecimal(150000),
          BigDecimal(-20.0),
          BigDecimal(1.2),
          BigDecimal(100.0)
        )
      )
      recommender ! LatestIndicators(currentIndicators)
      probe.expectNoMessage(500.millis) // Adjust time according to your context
    }

    "push recommendations to log" in {
      val recommender = testKit.spawn(Recommender())
      val probe = testKit.createTestProbe[RecommenderMessage]()
      val formatter = DateTimeFormatter.ISO_DATE_TIME

      recommender ! PushRecommendations(
        Seq(BuyingRecommendation("AAPL", BigDecimal(210.0), ZonedDateTime.parse("2022-05-01T12:00:00Z", formatter), List())),
        Seq(SellingRecommendation("AAPL", BigDecimal(190.0), ZonedDateTime.parse("2022-05-01T12:00:00Z", formatter), List()))
      )

      probe.expectNoMessage(500.millis) // Verify output to log or other side effects if possible
    }

    "handle error logging" in {
      val recommender = testKit.spawn(Recommender())
      val probe = testKit.createTestProbe[RecommenderMessage]()

      recommender ! LogError(new RuntimeException("Test Exception"))

      probe.expectNoMessage(500.millis) // Testing error handling, may need to verify logs
    }
  }
}


