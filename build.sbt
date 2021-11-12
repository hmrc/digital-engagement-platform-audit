import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

lazy val IntegrationTest = config("it") extend(Test)

val appName = "digital-engagement-platform-audit"

val silencerVersion = "1.7.7"

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedPackages :="""uk\.gov\.hmrc\.BuildInfo;.*\.Routes;.*\.RoutesPrefix;.*\.ErrorTemplate;.*\.ErrorHandler;.*\.TestOnlyTemplate;.*\.TestOnlyView;.*\.Reverse[^.]*""",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.12.13",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    PlayKeys.playDefaultPort         := 9190,
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(scoverageSettings)
  .settings(resolvers += Resolver.jcenterRepo)

lazy val itSettings =
  Defaults.itSettings ++ Seq(
    unmanagedSourceDirectories := Seq(
      baseDirectory.value / "it"
    ),
    unmanagedResourceDirectories := Seq(
      baseDirectory.value / "test" / "resources"
    )
  )
