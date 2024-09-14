package actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.stream.scaladsl.{Sink, Source}
import messages.{LatestIndicators, LogError, PushRecommendations, RecommenderMessage}
import models._

import scala.collection.immutable.HashMap
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Recommender {

  def apply(): Behavior[RecommenderMessage] = {

    def behavior(prev: Map[String, PerformanceIndicators]): Behavior[RecommenderMessage] = Behaviors receive {
      case (context, LatestIndicators(curr)) =>
        implicit val system: ActorSystem[Nothing] = context.system
        val recommendations = getBuyingRecommendations(prev, curr) zip getSellingRecommendations(prev, curr)
        //        context.log.info("Calculating criteria")
        context.pipeToSelf(recommendations) {
          case Success((buyingRecommendations, sellingRecommendations)) => PushRecommendations(buyingRecommendations, sellingRecommendations)
          case Failure(exception) => LogError(exception)
        }
        behavior(curr)

      case (context, PushRecommendations(buyingRecommendations, sellingRecommendations)) =>
        context.log.info("buying recommendations:\n" + buyingRecommendations.take(10).mkString("\n"))
        context.log.info("selling recommendations:\n" + sellingRecommendations.take(10).mkString("\n"))
        Behaviors.same

      case (context, LogError(exception)) =>
        context.log.error(exception.getMessage)
        Behaviors.same
    }

    behavior(HashMap.empty)
  }

  private def getBuyingRecommendations(prev: Map[String, PerformanceIndicators], curr: Map[String, PerformanceIndicators])(implicit system: ActorSystem[Nothing]): Future[Seq[BuyingRecommendation]] = {
    implicit val executionContext: ExecutionContextExecutor = system.executionContext
    Source(curr.toList)
      .mapAsyncUnordered(500) //parallelism can be a configuration parameter
      { case (ticker, indicators) =>
        Future {
          if (prev.contains(ticker)) {
            val criteria: Seq[BuyingCriteria] = List(
              EMABuyingCriteria(prev(ticker).ema, indicators.ema),
              MACDBuyingCriteria(indicators.macd),
              RSIBuyingCriteria(indicators.rsi),
              BollingerBandBuyingCriteria(indicators.price, indicators.bollingerBands._1),
              ADXBuyingCriteria(indicators.adx),
              StochasticOscillatorBuyingCriteria(indicators.stochK, indicators.stochD),
              VolumeBuyingCriteria(prev(ticker).volume, indicators.volume),
              WilliamsRBuyingCriteria(indicators.williamsR),
              ATRBuyingCriteria(prev(ticker).atr, indicators.atr),
              CCIBuyingCriteria(indicators.cci)
            ).filter(_.met)
            (ticker, criteria)
          } else {
            (ticker, Nil)
          }
        }
      }
      .filter(_._2 != Nil)
      .runWith(Sink.seq)
      .map(seq => seq.sortWith(_._2.length > _._2.length))
      .map { seq =>
        seq.map { case (ticker, criteria) =>
          BuyingRecommendation(ticker, curr(ticker).price, curr(ticker).timestamp, criteria)
        }
      }
  }

  private def getSellingRecommendations(prev: Map[String, PerformanceIndicators], curr: Map[String, PerformanceIndicators])(implicit system: ActorSystem[Nothing]): Future[Seq[SellingRecommendation]] = {
    implicit val executionContext: ExecutionContextExecutor = system.executionContext
    Source(curr.toList)
      .mapAsyncUnordered(500) //parallelism can be a configuration parameter
      { case (ticker, indicators) =>
        Future {
          if (prev.contains(ticker)) {
            val criteria: Seq[SellingCriteria] = List(
              EMASellingCriteria(prev(ticker).ema, indicators.ema),
              MACDSellingCriteria(indicators.macd),
              RSISellingCriteria(indicators.rsi),
              BollingerBandSellingCriteria(indicators.price, indicators.bollingerBands._3),
              ADXSellingCriteria(indicators.adx),
              StochasticOscillatorSellingCriteria(indicators.stochK, indicators.stochD),
              VolumeSellingCriteria(prev(ticker).volume, indicators.volume),
              WilliamsRSellingCriteria(indicators.williamsR),
              ATRSellingCriteria(prev(ticker).atr, indicators.atr),
              CCISellingCriteria(indicators.cci)
            ).filter(_.met)
            (ticker, criteria)
          } else {
            (ticker, Nil)
          }
        }
      }
      .filter(_._2 != Nil)
      .runWith(Sink.seq)
      .map(seq => seq.sortWith(_._2.length > _._2.length))
      .map { seq =>
        seq.map { case (ticker, criteria) =>
          SellingRecommendation(ticker, curr(ticker).price, curr(ticker).timestamp, criteria)
        }
      }

  }
}
