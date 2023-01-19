import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "7.12.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % "0.74.0",
    "com.auth0"               % "java-jwt"                    % "4.2.2"
  )

  val akkaVersion = "2.6.20"

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "7.12.0",
    "org.scalatest"           %% "scalatest"                  % "3.2.12",
    "org.scalatestplus"       %% "scalatestplus-mockito"      % "1.0.0-M2",
    "org.mockito"             %  "mockito-core"               % "4.5.1",
    "com.github.tomakehurst"  %  "wiremock-standalone"        % "2.27.2",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.62.2",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "com.typesafe.akka"       %% "akka-testkit"               % akkaVersion,
    "com.typesafe.akka"       %% "akka-actor-typed"           % akkaVersion,
    "com.typesafe.akka"       %% "akka-slf4j"                 % akkaVersion,
    "com.typesafe.akka"       %% "akka-serialization-jackson" % akkaVersion,
    "com.typesafe.akka"       %% "akka-protobuf-v3"           % akkaVersion,
    "com.typesafe.akka"       %% "akka-stream"                % akkaVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % "0.74.0"
  ).map(_ % "test, it")
}
