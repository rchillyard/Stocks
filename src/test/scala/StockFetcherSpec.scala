import actors.StockFetcher
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.util.Timeout
import messages._
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.ZonedDateTime
import scala.concurrent.duration._

class StockFetcherSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {


  override implicit val timeout: Timeout = Timeout(3.seconds)

  "StockFetcher" should {
    "successfully fetch stock quotes" in {
      val stockFetcher = spawn(StockFetcher())
      val testProbe = createTestProbe[FetcherMessage]()

      val quoteRequest = QuoteRequest("AAPL", ZonedDateTime.now.minusDays(10), ZonedDateTime.now)
      stockFetcher ! quoteRequest


    }

    "handle failures in fetching stock data" in {
      val stockFetcher = spawn(StockFetcher())
      val testProbe = createTestProbe[FetcherMessage]()

      val quoteRequest = QuoteRequest("INVALID", ZonedDateTime.now.minusDays(10), ZonedDateTime.now)
      stockFetcher ! quoteRequest


    }
  }
}

