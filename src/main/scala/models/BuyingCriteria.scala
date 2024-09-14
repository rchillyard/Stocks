package models

import java.time.ZonedDateTime

trait BuyingCriteria {
  val met:Boolean
  val description:String
}

final case class EMABuyingCriteria(prevEMA:BigDecimal, currEma:BigDecimal) extends BuyingCriteria
{
  override val met: Boolean = currEma > prevEMA
  override val description: String = s"Current EMA $currEma is greater than the previous EMA $prevEMA."
}

final case class MACDBuyingCriteria(currMACD:BigDecimal) extends BuyingCriteria
{
  override val met: Boolean = currMACD > 0
  override val description: String = s"MACD $currMACD is greater 0."
}

final case class RSIBuyingCriteria(currRSI:BigDecimal) extends BuyingCriteria
{
  override val met: Boolean = currRSI < 30
  override val description: String = s"RSI $currRSI is less than 30."
}

final case class ADXBuyingCriteria(currADX:BigDecimal) extends BuyingCriteria
{
  override val met: Boolean = currADX > 25
  override val description: String = s"ADX $currADX is greater than 25."
}

final case class StochasticOscillatorBuyingCriteria(StochK:BigDecimal, StochD:BigDecimal) extends BuyingCriteria
{
  override val met: Boolean = StochK > StochD
  override val description: String = s"Stochastic Oscillator K $StochK is greater than Stochastic Oscillator D $StochD."
}

final case class WilliamsRBuyingCriteria(currWilliamsR:BigDecimal) extends BuyingCriteria
{
  override val met: Boolean = currWilliamsR < -80
  override val description: String = s"Current WilliamsR $currWilliamsR is less than -80."
}

final case class ATRBuyingCriteria(prevATR:BigDecimal, currATR:BigDecimal) extends BuyingCriteria
{
  override val met: Boolean = currATR > prevATR
  override val description: String = s"Current ATR $currATR is greater than the previous ATR $prevATR."
}

final case class CCIBuyingCriteria(currCCI:BigDecimal) extends BuyingCriteria
{
  override val met: Boolean = currCCI > 100
  override val description: String = s"Current CCI $currCCI is greater than 100."
}

final case class VolumeBuyingCriteria(prevVolume:BigDecimal, currVolume:BigDecimal) extends BuyingCriteria
{
  override val met: Boolean = currVolume > prevVolume
  override val description: String = s"Current volume $currVolume is greater than the previous volume $prevVolume."
}

final case class BollingerBandBuyingCriteria(price:BigDecimal, lowerBand:BigDecimal) extends BuyingCriteria
{
  override val met: Boolean = price <= lowerBand
  override val description: String = s"Current price $price is less than the lower Bollinger Band $lowerBand."
}

final case class BuyingRecommendation(ticker:String, price:BigDecimal, timestamp:ZonedDateTime, criteria:Seq[BuyingCriteria]) {
  override def toString: String = {
    val criteriaDescriptions = criteria.map(_.description).mkString(" ")
    s"Buying Recommendation - Ticker: $ticker, Price: $price, Timestamp: $timestamp, Criteria: $criteriaDescriptions"
  }
}

