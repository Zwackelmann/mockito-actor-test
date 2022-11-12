import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "mockito-test",
    libraryDependencies += scalaTest,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14",
    libraryDependencies += "org.mockito" %% "mockito-scala" % "1.17.12",
    libraryDependencies += "org.mockito" %% "mockito-scala-scalatest" % "1.17.12",
    libraryDependencies += "com.typesafe.akka"  %% "akka-actor" % "2.7.0",
    libraryDependencies += "com.typesafe.akka"  %% "akka-actor-typed" % "2.7.0",
    Compile / resourceDirectory := baseDirectory.value / "conf",
    Compile / scalaSource := baseDirectory.value / "app"
  )

