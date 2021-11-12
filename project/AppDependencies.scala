import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.16.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % "0.56.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % "5.16.0",
    "org.scalatest"           %% "scalatest"                % "3.2.10",
    "org.scalatestplus"       %% "scalatestplus-mockito"    % "1.0.0-M2",
    "org.mockito"             %  "mockito-core"             % "4.0.0",
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.27.2",
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.62.2",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0",
    "com.typesafe.akka"       %% "akka-stream-testkit"      % "2.6.14",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"  % "0.56.0"
  ).map(_ % "test, it")
}
