package messages

import models.PerformanceIndicators

sealed trait RecordKeeperMessage

final case class Latest(symbol: String, indicators: PerformanceIndicators) extends RecordKeeperMessage

case object SendLatest extends RecordKeeperMessage
