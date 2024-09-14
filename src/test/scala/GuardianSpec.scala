import actors.Guardian
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import messages.{QuoteRequest, Request}
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.ZonedDateTime

class GuardianSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A Guardian actor" should {
    "send QuoteRequest messages to StockFetcher with correct parameters on receiving a Request message" in {
      val testTime = ZonedDateTime.now()
      val symbols = Seq("AAPL", "GOOGL")
      val guardian = spawn(Guardian(testTime, symbols, 10))

      val probe = createTestProbe[QuoteRequest]()
      guardian ! Request


    }
  }
}


