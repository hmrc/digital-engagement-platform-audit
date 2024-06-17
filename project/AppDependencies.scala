import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % "9.0.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % "2.0.0",
    "io.jsonwebtoken"         % "jjwt-api"                    % "0.11.5",
    "commons-codec"           %  "commons-codec"              % "1.15",
    "com.github.jwt-scala"    %% "jwt-core"                   % "9.1.2"
  )

  val akkaVersion = "2.6.21"

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % "9.0.0",
    "org.scalatest"           %% "scalatest"                  % "3.2.12",
    "org.scalatestplus"       %% "scalatestplus-mockito"      % "1.0.0-M2",
    "org.mockito"             %  "mockito-core"               % "4.5.1",
    "com.typesafe.akka"       %% "akka-testkit"               % akkaVersion,
    "com.typesafe.akka"       %% "akka-actor-typed"           % akkaVersion,
    "com.typesafe.akka"       %% "akka-slf4j"                 % akkaVersion,
    "com.typesafe.akka"       %% "akka-serialization-jackson" % akkaVersion,
    "com.typesafe.akka"       %% "akka-protobuf-v3"           % akkaVersion,
    "com.typesafe.akka"       %% "akka-stream"                % akkaVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % "2.0.0"
  ).map(_ % "test, it")
}
