package io.github.lhohan.sbtcommons

import io.github.davidgregory084.TpolecatPlugin
import sbt._
import sbt.Keys._
import scalafix.sbt.ScalafixPlugin.autoImport._

object CommonsPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  import Dependencies._
  override def projectSettings: Seq[Setting[_]] = {
    commonProjectSettings ++
      libraryDependencySettings ++
      TpolecatPlugin.projectSettings ++
      scalafixSettings
  }

  lazy val commonProjectSettings = Seq(
    scalaVersion := "2.13.3",
    organization := "io.github.lhohan",
    organizationName := "Hans L'Hoest"
  )

  lazy val libraryDependencySettings = Seq(
    libraryDependencies += scalaTest % Test,
    libraryDependencies += catsCore,
    libraryDependencies += csv,
    libraryDependencies += csvGeneric,
    libraryDependencies += scopt,
    libraryDependencies += uPickle,
  )

  lazy val scalafixSettings: Seq[Setting[_]] =
    Seq(
      semanticdbEnabled := true,                        // enable SemanticDB
      semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
      scalacOptions += "-Wunused",                      // required by `RemoveUnused` rule
      scalacOptions += "-Yrangepos",                    // required by `RemoveUnused` rule
      scalacOptions -= "-Xfatal-warnings"               // Added by tpolecat plugin but will disallow scalafix to run
    )
}

object Dependencies {
  lazy val catsCore   = "org.typelevel" %% "cats-core"          % "2.0.0"
  lazy val csv        = "com.nrinaudo"  %% "kantan.csv"         % "0.6.1"
  lazy val csvGeneric = "com.nrinaudo"  %% "kantan.csv-generic" % "0.6.1"
  lazy val scopt = "com.github.scopt" %% "scopt" % "4.0.0-RC2"
  lazy val uPickle    = "com.lihaoyi"   %% "upickle"            % "1.2.2"
  lazy val scalaTest  = "org.scalatest" %% "scalatest"          % "3.2.2"
}
