/*
 *  MUSIT is a museum database to archive natural and cultural history data.
 *  Copyright (C) 2016  MUSIT Norway, part of www.uio.no (University of Oslo)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License,
 *  or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
import CommonSettings._
import Dependencies._
import play.twirl.sbt.Import.TwirlKeys._
import ScoverageSbtPlugin.ScoverageKeys._

name := """musit"""

version := "0.1"

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding", "UTF-8"
)

val baseDockerSettings = Seq(
  maintainer in Docker := "Musit Norway <musit@musit.uio.no>",
  packageSummary in Docker := "A Microservice part of the middleware for Musit Norway",
  packageDescription in Docker := "A Microservice part of the middleware for MusitNorway",
  dockerExposedPorts in Docker := Seq(8080)
)

val scoverageSettings = Seq(
  coverageExcludedPackages := "<empty>;controllers.javascript;views.*;router",
  coverageExcludedFiles := "",
  coverageMinimum := 80,
  coverageFailOnMinimum := true
)

val noPublish = Seq(
  publish := {},
  publishLocal := {}
)

lazy val root = (
  project.in(file("."))
  settings(noPublish)
  aggregate(common, security, service_core, service_example)
)

// Base projects used as dependencies
lazy val common = (
  BaseProject("common")
  settings(noPublish)
  settings(libraryDependencies ++= playWithPersistenceDependencies)
  settings(scoverageSettings: _*)
)

lazy val security = (
  BaseProject("security")
  settings(noPublish)
  settings(libraryDependencies ++= playDependencies)
  settings(scoverageSettings: _*)
)

// Microservices with publish support
lazy val service_example = (
  PlayProject("service_example")
  settings(libraryDependencies ++= playWithPersistenceDependencies)
  settings(routesGenerator := InjectedRoutesGenerator)
  settings(scoverageSettings: _*)
  settings(baseDockerSettings ++ Seq(
    packageName in Docker := "musit_service_example"
  ))
) dependsOn(common)

lazy val service_core = (
  PlayProject("service_core")
    settings(libraryDependencies ++= playWithPersistenceDependencies)
    settings(routesGenerator := InjectedRoutesGenerator)
    settings(scoverageSettings: _*)
    settings(baseDockerSettings ++ Seq(
      packageName in Docker := "musit_service_core"
    ))
  ) dependsOn(common)

// Add other services here

// Extra tasks
// TODO: Fix codegen task to have external properties not in GIT
libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "2.1.0"
lazy val dbgen = taskKey[Seq[File]]("slick code generation")

dbgen := {
  val dbName = "olddb"
  val userName = "username"
  val password = "have to find some way of adding it a runtime"
  val url = s"jdbc:mysql://server:port/$dbName"
  val jdbcDriver = "com.mysql.jdbc.Driver"
  val slickDriver = "scala.slick.driver.MySQLDriver"
  val targetPackageName = "no.uio.musit.legacy.model"
  val outputDir = ((sourceManaged in Compile).value / dbName).getPath
  val fname = outputDir + s"/$targetPackageName/Tables.scala"
  println(s"\nauto-generating slick source for database schema at $url...")
  println(s"output source file file: file://$fname\n")
  (runner in Compile).value.run("scala.slick.codegen.SourceCodeGenerator", (dependencyClasspath in Compile).value.files, Array(slickDriver, jdbcDriver, url, outputDir, targetPackageName, userName, password), streams.value.log)
  Seq(file(fname))
}