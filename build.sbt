name := "bezrealitky"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= "net.ruippeixotog" %% "scala-scraper" % "2.1.0" ::
  "com.typesafe" % "config" % "1.3.2" ::
  "ch.qos.logback" % "logback-classic" % "1.2.3" ::
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0" ::
  Nil
