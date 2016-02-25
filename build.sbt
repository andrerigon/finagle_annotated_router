import java.io.File

import spray.revolver.RevolverPlugin._

name := """finagle_annotated_router"""

fork in run := true

resolvers ++= Seq("Twitter Repo" at "http://maven.twttr.com/")

resolvers ++= Seq("Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")

resolvers += "Mesosphere Public Repository" at "http://downloads.mesosphere.io/maven"

scalaVersion := "2.11.6"

organization := "org"

val finagleVersion = "6.30.0"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.6",
  "com.twitter" %% "finagle-http" % finagleVersion,
  "org.slf4j" % "slf4j-log4j12" % "1.7.10"
)

// test

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "org.codehaus.groovy" % "groovy-all" % "2.4.3" % "test"
)

assemblyJarName in assembly := s"${name.value}.jar"

parallelExecution in Test := true

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 65

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true

addCommandAlias("test", "testQuick")

addCommandAlias("devrun", "~re-start")

addCommandAlias("cov", "; clean; coverage; test")

Revolver.settings

test in assembly := {}

releaseSettings

publishTo := Some(Resolver.file("Local repo", Path.userHome / ".m2" / "repository" asFile ))
