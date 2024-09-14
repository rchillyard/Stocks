package actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import messages.{Error, Latest, Processed, ProcessorMessage, Quotes, Write}
import models.{PerformanceIndicators, Quote}
import org.ta4j.core.indicators._
import org.ta4j.core.indicators.adx.ADXIndicator
import org.ta4j.core.indicators.bollinger.{BollingerBandsLowerIndicator, BollingerBandsMiddleIndicator, BollingerBandsUpperIndicator}
import org.ta4j.core.indicators.helpers.{ClosePriceIndicator, VolumeIndicator}
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator
import org.ta4j.core.num.{DecimalNum, Num}
import org.ta4j.core.{BaseBarSeries, BaseBarSeriesBuilder, Indicator}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object StockProcessor {

  def apply(): Behavior[ProcessorMessage] = Behaviors setup  { context =>
    lazy val writer = context.spawn(CsvWriter(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))), "csv-writer")
    lazy val table = context.spawn(RecordKeeper(), "table")

    Behaviors receiveMessage {
      case Quotes(symbol, quotes) =>
        implicit val executionContext: ExecutionContext = context.executionContext
        context.pipeToSelf(getIndicators(quotes)){
          case Success(indicators) => Processed(symbol, indicators)
          case Failure(exception) => Error(symbol, exception)
        }
        Behaviors.same

      case Processed(symbol, indicators) =>
        writer ! Write(symbol, indicators)
        table ! Latest(symbol, indicators)
        Behaviors.same

      case Error(symbol, error) =>
        context.log.error(symbol + "\t" + error.getMessage)
        Behaviors.same
    }

  }

  private def getIndicators(quotes: Seq[Quote])(implicit executionContext:ExecutionContext): Future[PerformanceIndicators] = {

    val timestamp = Instant.ofEpochSecond(quotes.last.timestamp).atZone(ZoneId.systemDefault())
    val currentPrice = quotes.last.close
    val closeSeq = quotes.map(_.close)


    val series = toBarSeries(quotes)
    val closePriceIndicator = new ClosePriceIndicator(series)
    val RSI = new RSIIndicator(closePriceIndicator, series.getBarCount)
    val bbmI = new BollingerBandsMiddleIndicator(new EMAIndicator(closePriceIndicator, series.getBarCount))
    val MACD = new MACDIndicator(closePriceIndicator)
    val sd = new StandardDeviationIndicator(closePriceIndicator, series.getBarCount)
    val bblI = new BollingerBandsLowerIndicator(bbmI, sd)
    val bbuI = new BollingerBandsUpperIndicator(bbmI, sd)
    val emaI = new EMAIndicator(closePriceIndicator, series.getBarCount)
    val adxI = new ADXIndicator(series, series.getBarCount)
    val stochKI = new StochasticOscillatorKIndicator(series, series.getBarCount)
    val stochDI = new StochasticOscillatorDIndicator(stochKI)
    val volumeI = new VolumeIndicator(series)
    val williamsRI = new WilliamsRIndicator(series, series.getBarCount)
    val atrI = new ATRIndicator(series, series.getBarCount)
    val cciI = new CCIIndicator(series, series.getBarCount)

    val changeF = getChange(closeSeq)
    val minF = getMin(closeSeq)
    val maxF = getMax(closeSeq)
    val avgF = getAverage(closeSeq)
    val macdF = getValue(MACD, series.getEndIndex)
    val rsiF = getValue(RSI, series.getEndIndex)
    val bblF = getValue(bblI, series.getEndIndex)
    val bbmF = getValue(bbmI, series.getEndIndex)
    val bbuF = getValue(bbuI, series.getEndIndex)
    val emaF = getValue(emaI, series.getEndIndex)
    val adxF = getValue(adxI, series.getEndIndex)
    val stochKF = getValue(stochKI, series.getEndIndex)
    val stochDF = getValue(stochDI, series.getEndIndex)
    val volumeF = getValue(volumeI, series.getEndIndex)
    val williamsRF = getValue(williamsRI, series.getEndIndex)
    val atrF = getValue(atrI, series.getEndIndex)
    val cciF = getValue(cciI, series.getEndIndex)

    for {
      (_, rd) <- changeF
      min <- minF
      max <- maxF
      avg <- avgF
      macd <- macdF
      rsi <- rsiF
      bbl <- bblF
      bbm <- bbmF
      bbu <- bbuF
      ema <- emaF
      adx <- adxF
      stochK <- stochKF
      stochD <- stochDF
      volume <- volumeF
      williamsR <- williamsRF
      atr <- atrF
      cci <- cciF
    } yield PerformanceIndicators(timestamp, currentPrice, rd.doubleValue, min, max, avg, macd, rsi, (bbl, bbm, bbu), ema, adx, stochK, stochD, volume, williamsR, atr, cci)

  }

  private def toBarSeries(quotes: Seq[Quote]): BaseBarSeries = {
    val barSeriesBuilder = new BaseBarSeriesBuilder()
    val series = barSeriesBuilder.withName("current")
      .withNumTypeOf(DecimalNum.valueOf(_))
      .build()
    for (quote <- quotes)
      series.addBar(Instant.ofEpochSecond(quote.timestamp).atZone(ZoneId.systemDefault()), quote.open, quote.high, quote.low, quote.close, quote.volume)
    series
  }

  private def getChange(closeSeq: Seq[BigDecimal])(implicit executionContext:ExecutionContext): Future[(BigDecimal, BigDecimal)] = Future{
    val absoluteDifference = closeSeq.head-closeSeq.last
    val relativeDifference = absoluteDifference/(if (closeSeq.head == 0.0) 1.0 else closeSeq.head)
    (absoluteDifference, relativeDifference)
  }

  private def getMin(closeSeq: Seq[BigDecimal])(implicit executionContext: ExecutionContext): Future[BigDecimal] = Future{
    closeSeq.min
  }

  private def getMax(closeSeq: Seq[BigDecimal])(implicit executionContext: ExecutionContext): Future[BigDecimal] = Future{
    closeSeq.max
  }

  private def getAverage(closeSeq: Seq[BigDecimal])(implicit executionContext: ExecutionContext): Future[BigDecimal] = Future{
    closeSeq.sum/closeSeq.length
  }


  private def getValue(indicator: Indicator[Num], index:Int)(implicit executionContext: ExecutionContext): Future[BigDecimal] = Future{
    indicator.getValue(index) match {
      case n:DecimalNum => n.getDelegate
      case a:Num => a.doubleValue
    }
  }


}
