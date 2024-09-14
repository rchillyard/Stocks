package messages

import models.{PerformanceIndicators, Quote}

sealed trait ProcessorMessage

final case class Quotes(symbol:String, quotes:Seq[Quote]) extends ProcessorMessage

final case class Processed(symbol: String, indicators: PerformanceIndicators) extends ProcessorMessage

final case class Error(symbol:String, error: Throwable) extends ProcessorMessage