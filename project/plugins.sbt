resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc"       %  "sbt-auto-build"        % "3.24.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-distributables"    % "2.5.0")
addSbtPlugin("com.typesafe.play" %  "sbt-plugin"            % "2.9.1")
addSbtPlugin("org.scoverage"     %  "sbt-scoverage"         % "2.0.9")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin" % "1.0.0" exclude("org.scala-lang.modules", "scala-xml_2.12"))
addSbtPlugin("com.timushev.sbt"  %  "sbt-updates"           % "0.5.0")
