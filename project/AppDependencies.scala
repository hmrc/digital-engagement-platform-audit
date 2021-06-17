import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.3.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.3.0"             % Test,
    "org.scalatest"           %% "scalatest"                  % "3.2.5"             % Test,
    "org.scalatestplus"      %% "scalatestplus-mockito" % "1.0.0-M2" % Test,
    "org.mockito"             % "mockito-core"          % "3.10.0" % Test,
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"             % "test, it"
  )
}
