import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % "9.11.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % "2.6.0",
    "io.jsonwebtoken"         % "jjwt-api"                    % "0.11.5",
    "commons-codec"           %  "commons-codec"              % "1.15",
    "com.github.jwt-scala"    %% "jwt-core"                   % "9.1.2"
  )

  val PekkoVersion = "1.0.2"
  val PekkoHttpVersion = "1.0.1"

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % "9.11.0",
    "org.scalatest"           %% "scalatest"                  % "3.2.12",
    "org.scalatestplus"       %% "scalatestplus-mockito"      % "1.0.0-M2",
    "org.mockito"             %  "mockito-core"               % "4.5.1",
    "org.apache.pekko"        %% "pekko-stream-testkit"       % PekkoVersion,
    "org.apache.pekko"        %% "pekko-http-testkit"         % PekkoHttpVersion,
    "org.apache.pekko"        %% "pekko-actor-typed"          % PekkoVersion,
    "org.apache.pekko"        %% "pekko-serialization-jackson"% PekkoVersion,
    "org.apache.pekko"        %% "pekko-stream"               % PekkoVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % "2.6.0"
  ).map(_ % "test, it")
}
