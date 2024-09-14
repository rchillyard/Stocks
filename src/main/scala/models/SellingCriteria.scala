package models

import java.time.ZonedDateTime

trait SellingCriteria {
  val met:Boolean
  val description:String
}

final case class EMASellingCriteria(prevEMA:BigDecimal, currEma:BigDecimal) extends SellingCriteria
{
  override val met: Boolean = currEma < prevEMA
  override val description: String = s"Current EMA $currEma is less than the previous EMA $prevEMA."
}

final case class MACDSellingCriteria(currMACD:BigDecimal) extends SellingCriteria
{
  override val met: Boolean = currMACD < 0
  override val description: String = s"MACD $currMACD is less than 0."
}

final case class RSISellingCriteria(currRSI:BigDecimal) extends SellingCriteria
{
  override val met: Boolean = currRSI > 70
  override val description: String = s"RSI $currRSI is greater than 70."
}

final case class ADXSellingCriteria(currADX:BigDecimal) extends SellingCriteria
{
  override val met: Boolean = currADX < 25
  override val description: String = s"ADX $currADX is less than 25."
}

final case class StochasticOscillatorSellingCriteria(StochK:BigDecimal, StochD:BigDecimal) extends SellingCriteria
{
  override val met: Boolean = StochK < StochD
  override val description: String = s"Stochastic Oscillator K $StochK is less than Stochastic Oscillator D $StochD."
}

final case class WilliamsRSellingCriteria(currWilliamsR:BigDecimal) extends SellingCriteria
{
  override val met: Boolean = currWilliamsR > -20
  override val description: String = s"Current WilliamsR $currWilliamsR is greater than -20."
}

final case class ATRSellingCriteria(prevATR:BigDecimal, currATR:BigDecimal) extends SellingCriteria
{
  override val met: Boolean = currATR > prevATR
  override val description: String = s"Current ATR $currATR is greater than the previous ATR $prevATR."
}

final case class CCISellingCriteria(currCCI:BigDecimal) extends SellingCriteria
{
  override val met: Boolean = currCCI < -100
  override val description: String = s"Current CCI $currCCI is less than -100."
}

final case class VolumeSellingCriteria(prevVolume:BigDecimal, currVolume:BigDecimal) extends SellingCriteria {
  override val met: Boolean = currVolume < prevVolume
  override val description: String = s"Current Volume $currVolume is less than the previous volume."
}

final case class BollingerBandSellingCriteria(price:BigDecimal, upperBand:BigDecimal) extends SellingCriteria {
  override val met: Boolean = price >= upperBand
  override val description: String = s"Current Price $price is greater than upper Bollinger Band $upperBand."
}


final case class SellingRecommendation(ticker:String, price:BigDecimal, timestamp:ZonedDateTime, criteria:Seq[SellingCriteria]){
  override def toString: String = {
    val criteriaDescriptions = criteria.map(_.description).mkString(" ")
    s"Selling Recommendation - Ticker: $ticker, Price: $price, Timestamp: $timestamp, Criteria: $criteriaDescriptions"
  }
}
