package actors

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.alpakka.csv.scaladsl.CsvFormatting
import akka.stream.scaladsl.{FileIO, Source}
import messages.{Write, WriterMessage}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}
import scala.language.postfixOps
import scala.util.Using

object CsvWriter {

  def apply(name: String): Behavior[WriterMessage] = Behaviors setup  { context =>


    val path = Paths.get(s"data/$name.csv")//file path can be an environment variable
    Files.createFile(path)

    val headers = Seq("symbol", "timestamp", "currentPrice", "rd", "min", "max", "avg", "macd", "rsi", "bollingerBands", "ema", "adx", "stochK", "stochD", "volume", "williamsR", "atr", "cci")
    Using(Files.newBufferedWriter(path, StandardCharsets.UTF_8)){ writer =>
      writer.write(headers.mkString(","))
      writer.newLine()
    }

    val sink = FileIO.toPath(Paths.get(s"data/$name.csv"), Set(StandardOpenOption.APPEND))

    Behaviors receiveMessage {
      case Write(symbol, indicators) =>
        implicit val system: ActorSystem[Nothing] = context.system
        Source.single(symbol +: indicators.productIterator.toSeq.map(_ toString))
          .via(CsvFormatting.format())
          .runWith(sink)
        Behaviors.same
    }

  }

}
