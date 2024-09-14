package models

import java.time.ZonedDateTime

final case class PerformanceIndicators(timestamp: ZonedDateTime,
                                       price: BigDecimal,
                                       change: BigDecimal,
                                       min: BigDecimal,
                                       max: BigDecimal,
                                       average: BigDecimal,
                                       macd: BigDecimal,
                                       rsi: BigDecimal,
                                       bollingerBands: (BigDecimal, BigDecimal, BigDecimal), // (lower, middle, upper)
                                       ema: BigDecimal,
                                       adx: BigDecimal,
                                       stochK: BigDecimal,
                                       stochD: BigDecimal,
                                       volume: BigDecimal,
                                       williamsR: BigDecimal,
                                       atr: BigDecimal,
                                       cci: BigDecimal
                                      )