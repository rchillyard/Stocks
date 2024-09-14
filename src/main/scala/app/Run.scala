package app

import actors.Guardian
import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import messages.Request

import java.time.{ZoneId, ZonedDateTime}
import scala.io.Source
import scala.util.Using

object Run extends App {

  val from: ZonedDateTime = ZonedDateTime.of(2024, 3, 28, 0, 0, 0, 0, ZoneId.systemDefault())
  Using(Source.fromResource("symbols.txt")){source =>
    val symbols:Seq[String] = source.getLines().toSeq.take(150)
    ActorSystem(Guardian(from, symbols, 30), "guardian", ConfigFactory.load()) ! Request
  }



}
