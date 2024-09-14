package models

final case class Quote(timestamp: Long,
                        open:  BigDecimal,
                        low: BigDecimal,
                        volume: BigDecimal,
                        high: BigDecimal,
                        close: BigDecimal,
                        adjclose: BigDecimal)