package actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import messages.{Latest, LatestIndicators, RecordKeeperMessage, SendLatest}
import models.PerformanceIndicators

import scala.collection.immutable.HashMap
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object RecordKeeper {

  def apply(): Behavior[RecordKeeperMessage] = Behaviors setup { context =>

    lazy val recommender = context.spawn(Recommender(), "recommender")

    Behaviors.withTimers { scheduler =>
      scheduler.startTimerWithFixedDelay(SendLatest,  20 second)//the delay can be a configuration parameter
      def behavior(map: Map[String, PerformanceIndicators]): Behavior[RecordKeeperMessage] = Behaviors receiveMessage {
        case Latest(symbol, indicators) =>
          behavior(map + (symbol -> indicators))
        case SendLatest =>
          recommender ! LatestIndicators(map)
          Behaviors.same
      }
      behavior(HashMap.empty)
    }






  }


}
