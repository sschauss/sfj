ThisBuild / organization := "org.softlang"
ThisBuild / version := "0.0.1"
ThisBuild / javaOptions += "--illegal-access=permit"
ThisBuild / scalaVersion := "3.0.2"
ThisBuild / libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-api" % "2.14.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.14.1",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.14.1",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
)

lazy val core = project
  .settings(
    idePackagePrefix.withRank(KeyRanks.Invisible) := Some("org.softlang.sfj.core"),
    libraryDependencies ++= Seq(
      "org.ow2.asm" % "asm" % "9.2",
      "org.ow2.asm" % "asm-tree" % "9.2",
      "org.ow2.asm" % "asm-commons" % "9.2",
      "org.ow2.asm" % "asm-analysis" % "9.2",
      "org.ow2.asm" % "asm-util" % "9.2",
    ),
  )

lazy val maven = project
  .settings(
    idePackagePrefix.withRank(KeyRanks.Invisible) := Some("org.softlang.sfj.maven"),
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-csv" % "1.8",
      "org.apache.maven" % "maven-model-builder" % "3.8.1",
      "org.apache.maven" % "maven-resolver-provider" % "3.8.1",
      "org.apache.maven.indexer" % "indexer-core" % "6.0.0",
      "org.apache.maven.resolver" % "maven-resolver-connector-basic" % "1.7.2",
      "org.apache.maven.resolver" % "maven-resolver-transport-http" % "1.7.2",
      "org.apache.maven.resolver" % "maven-resolver-transport-file" % "1.7.2",
      "org.apache.maven.shared" % "maven-invoker" % "3.1.0",
      "org.apache.maven.wagon" % "wagon-http-lightweight" % "3.4.3",
      "org.eclipse.sisu" % "org.eclipse.sisu.plexus" % "0.3.5",
      "com.google.code.gson" % "gson" % "2.8.8",
    )
  )
  .dependsOn(core)
