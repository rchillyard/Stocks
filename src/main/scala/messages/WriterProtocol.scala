package messages

import models.PerformanceIndicators

sealed trait WriterMessage

final case class Write(symbol: String, indicators: PerformanceIndicators) extends WriterMessage
