package io.github.lhohan.sbtcommons

import io.github.davidgregory084.TpolecatPlugin
import sbt._
import sbt.Keys._
import scalafix.sbt.ScalafixPlugin.autoImport._
import wartremover.WartRemover.autoImport._

object CommonsPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  import Dependencies._
  override def projectSettings: Seq[Setting[_]] = {
    commonProjectSettings ++
      libraryDependencySettings ++
      TpolecatPlugin.projectSettings ++
      scalafixSettings ++
      wartremoverSettings ++
      reportingSettings
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
    libraryDependencies += scalaTags,
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

  lazy val wartremoverSettings: Seq[Setting[_]] =
    Seq(
      wartremoverWarnings ++= Warts.allBut(
        Wart.Any,
        Wart.Nothing,
        Wart.Serializable,
        Wart.JavaSerializable,
        Wart.NonUnitStatements
      )
    )

  lazy val reportingSettings: Seq[Setting[_]] =
    Seq(
      extraLoggers := {
        import org.apache.logging.log4j.core.LogEvent;
        import org.apache.logging.log4j.core.appender.AbstractAppender
        import org.apache.logging.log4j.message.{Message,ObjectMessage}

        import sbt.internal.util.StringEvent

        def loggerNameForKey( key : sbt.Def.ScopedKey[_] ) = s"""reverse.${key.scope.task.toOption.getOrElse("<unknown>")}"""

        class ReverseConsoleAppender( key : ScopedKey[_] ) extends AbstractAppender (
          loggerNameForKey( key ), // name : String
          null,                    // filter : org.apache.logging.log4j.core.Filter
          null,                    // layout : org.apache.logging.log4j.core.Layout[ _ <: Serializable]
          false                    // ignoreExceptions : Boolean
        ) {

          this.start() // the log4j2 Appender must be started, or it will fail with an Exception

          override def append( event : LogEvent ) : Unit = {
            val output = {
              def forUnexpected( message : Message ) = s"[${this.getName()}] Unexpected: ${message.getFormattedMessage()}"
              event.getMessage() match {
                case om : ObjectMessage => { // what we expect
                  om.getParameter() match {
                    case se : StringEvent => s"[${this.getName()} - ${se.level}] ${se.message.reverse}"
                    case other            => forUnexpected( om )
                  }
                }
                case unexpected : Message => forUnexpected( unexpected )
              }
            }
            System.out.synchronized { // sbt adopts a convention of acquiring System.out's monitor printing to the console
              println( output )
            }
          }
        }

        val currentFunction = extraLoggers.value
        (key: ScopedKey[_]) => {
          new ReverseConsoleAppender(key) +: currentFunction(key)
        }
      }
    )
}

object Dependencies {
  lazy val catsCore   = "org.typelevel" %% "cats-core"          % "2.0.0"
  lazy val csv        = "com.nrinaudo"  %% "kantan.csv"         % "0.6.1"
  lazy val csvGeneric = "com.nrinaudo"  %% "kantan.csv-generic" % "0.6.1"
  lazy val scalaTags = "com.lihaoyi" %% "scalatags" % "0.8.2"
  lazy val scopt = "com.github.scopt" %% "scopt" % "4.0.0-RC2"
  lazy val uPickle    = "com.lihaoyi"   %% "upickle"            % "1.2.2"
  lazy val scalaTest  = "org.scalatest" %% "scalatest"          % "3.2.2"
}
