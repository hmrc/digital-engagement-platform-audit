import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.3.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % "5.3.0",
    "org.scalatest"           %% "scalatest"                % "3.2.5",
    "org.scalatestplus"       %% "scalatestplus-mockito"    % "1.0.0-M2",
    "org.mockito"             %  "mockito-core"             % "3.10.0",
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.27.2",
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.36.8",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"
  ).map(_ % "test, it")
}
