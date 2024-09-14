lazy val StocksVersion = "0.1.1-SNAPSHOT"

lazy val ScalaVersion = "2.13.14"
lazy val akkaHttpVersion = "10.6.3"
lazy val akkaVersion    = "2.9.5"
lazy val alpakkaVersion = "8.0.0"
lazy val ta4jVersion = "0.16"
lazy val logbackVersion = "1.5.8"
lazy val scalatestVersion = "3.2.19"
lazy val Akka = "com.typesafe.akka"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      version := StocksVersion,
      scalaVersion    := ScalaVersion
    )),
    name := "Real-Time-Stock-Tracking-Application",
    libraryDependencies ++= Seq(
      Akka %% "akka-http"                % akkaHttpVersion,
      Akka %% "akka-http-spray-json"     % akkaHttpVersion,
      Akka %% "akka-actor-typed"         % akkaVersion,
      Akka %% "akka-actor"               % akkaVersion,
      Akka %% "akka-stream"              % akkaVersion,
      Akka %% "akka-pki"                 % akkaVersion,
      "com.lightbend.akka" %% "akka-stream-alpakka-csv" % alpakkaVersion,
      "org.ta4j" % "ta4j-core" % ta4jVersion,
      "ch.qos.logback"    % "logback-classic"           % logbackVersion,

      Akka %% "akka-http-testkit"        % akkaHttpVersion % Test,
      Akka %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % scalatestVersion        % Test
    )
  )
