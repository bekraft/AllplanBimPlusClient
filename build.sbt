name := "bimplus-akka-repl"

organization := "bitub"
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.1"

val akkaVersion = "2.6.1"
val akkaHttp = "10.1.11"
val scalacheckVersion = "1.14.1"
val scalatestVersion = "3.1.1"

fork in run := true
cancelable in Global := true

libraryDependencies ++= Seq(
	"org.scala-lang" % "scala-reflect" % scalaVersion.value,

	"org.apache.poi" % "poi-ooxml" % "4.1.2",

	"com.typesafe.akka" %% "akka-actor" % akkaVersion,
	"com.typesafe.akka" %% "akka-http-core" % akkaHttp,
	"com.typesafe.akka" %% "akka-http" % akkaHttp,
	"com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
	"com.typesafe.akka" %% "akka-stream" % akkaVersion,
	"com.typesafe.akka" %% "akka-http-spray-json" % akkaHttp,

	"org.scalatest" %% "scalatest" % scalatestVersion % Test,
	"org.scalacheck" %% "scalacheck" % scalacheckVersion % Test
)

initialCommands in console := """|
								|import scala.concurrent.ExecutionContext.Implicits._
								|import scala.concurrent.{Await,Future}
								|import scala.concurrent.duration._
								|import bimplus._
								|import bimplus.dto._
								|import bimplus.DtoResult._
								|import bimplus.Api._
								|import bimplus.dto.marshalling.JsonMarshaller._
								|""".stripMargin
