package messages

import akka.http.scaladsl.model.HttpResponse
import models.Quote

import java.time.ZonedDateTime

sealed trait FetcherMessage
final case class QuoteRequest(symbol:String, from:ZonedDateTime, to:ZonedDateTime) extends FetcherMessage
final case class Response(symbol:String, data:HttpResponse) extends FetcherMessage
final case class Parsed(symbol:String, parsed: Seq[Quote]) extends FetcherMessage
final case class FetchFailed(symbol:String, exception:Throwable) extends FetcherMessage
final case class ParseFailed(symbol:String, exception:Throwable) extends FetcherMessage