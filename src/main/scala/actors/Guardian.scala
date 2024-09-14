package actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import messages.{GuardianMessage, QuoteRequest, Request}

import java.time.ZonedDateTime
import scala.concurrent.duration.DurationInt


object Guardian {
  def apply(from: ZonedDateTime, symbols: Seq[String], delay:Int): Behavior[GuardianMessage] = Behaviors setup { context =>

    lazy val fetcher = context.spawn(StockFetcher(), "stock-fetcher")

    Behaviors.withTimers{scheduler =>
      scheduler.startTimerWithFixedDelay(Request, delay.second)
      Behaviors receiveMessage {
        case Request =>
          val to = ZonedDateTime.now()
          for(symbol <- symbols)
            fetcher ! QuoteRequest(symbol, from, to)
          Behaviors.same
      }
    }



  }

}
