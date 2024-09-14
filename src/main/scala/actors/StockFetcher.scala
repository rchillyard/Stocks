package actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.unmarshalling.Unmarshal
import formats.Formats._
import messages._
import models.Quote

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success}

object StockFetcher {

  def apply():Behavior[FetcherMessage] = Behaviors setup { context =>

    lazy val processor = context.spawn(StockProcessor(), "processor")
    lazy val client = Http(context.system)

    Behaviors receiveMessage  {
          case QuoteRequest(symbol, from, to) =>
            val url = s"https://query1.finance.yahoo.com/v8/finance/chart/$symbol?symbol=$symbol&period1=${from.toEpochSecond}&period2=${to.toEpochSecond}&interval=1d&events=div|split"
            val response = client.singleRequest(Get(url))
            context.pipeToSelf(response){
              case Success(response) => Response(symbol, response)
              case Failure(exception) => FetchFailed(symbol, exception)
            }
            Behaviors.same

          case Response(symbol, response) =>
//            context.log.info(s"Data received for $symbol")
            implicit val system: ActorSystem[Nothing] = context.system
            response.status match {
              case StatusCodes.OK =>
                val parsed = Unmarshal(response).to[Seq[Quote]]
                context.pipeToSelf(parsed){
                  case Success(quoteList) => Parsed(symbol, quoteList)
                  case Failure(error) => ParseFailed(symbol, error)
                }
              case StatusCodes.TooManyRequests =>
                context.log.error("The application is being rate-limited.")
                response.discardEntityBytes()

              case _ => response.discardEntityBytes()
            }
            Behaviors.same

          case Parsed(symbol, parsed) =>
            processor ! Quotes(symbol, parsed)
            Behaviors.same

          case FetchFailed(symbol, exception) =>
            context.log.error(s"Unable to fetch data for $symbol", exception)
            Behaviors.same

          case ParseFailed(symbol, exception) =>
            exception match {
              case DataNotFoundException(message) => context.log.error(s"Error for $symbol. $message")
              case _ => context.log.error(s"Unable to parse data for $symbol.", exception)
            }
            Behaviors.same


        }
  }
}
