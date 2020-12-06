package io.github.lhohan.sbtcommons

import java.nio.charset.StandardCharsets

import io.github.davidgregory084.TpolecatPlugin
import java.nio.file._
import sbt._
import sbt.Keys._
import scalafix.sbt.ScalafixPlugin.autoImport._
import wartremover.WartRemover.autoImport._
import scala.collection.mutable.ListBuffer
import scala.sys.process.ProcessLogger
import sys.process._

object CommonsPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  import Dependencies._
  override def projectSettings: Seq[Setting[_]] = {
    commonProjectSettings ++
      libraryDependencySettings ++
      TpolecatPlugin.projectSettings ++
      scalafixSettings ++
      wartremoverSettings
  }

  lazy val checkCompilerReport    = taskKey[Unit]("Prints static code analysis report from compiler")
  lazy val checkCompilerThreshold = taskKey[Int]("Set threshold to fail on if above this number")

  import CompilerReporting._
  lazy val commonProjectSettings = Seq(
    scalaVersion := "2.13.3",
    organization := "io.github.lhohan",
    organizationName := "Hans L'Hoest",
    checkCompilerThreshold := Int.MaxValue,
    checkCompilerReport := {
      val VerboseOutput = true // TODO ??? make setting
      val projectName   = name.value

      def readViolations(): Seq[CheckViolation] = {
        val scalaSourcePath = Paths.get((Compile / scalaSource).value.toString)
        val regex           = raw"\[(.+)\] (.+):(\d+):(\d+): (.+)".r
        println("Running static code analysis report. This may take a while ...")
        val violations = new ListBuffer[CheckViolation]()
        "sbt clean compile" ! ProcessLogger( // TODO: this will not work for sub projects
          { outputStr =>
            if (VerboseOutput) {
              println(outputStr)
            }
            outputStr match {
              case regex(sev, file, line, pos, rule) =>
                val filePath = Paths.get(file)
                val f = {
                  if (filePath.startsWith(scalaSourcePath)) scalaSourcePath.relativize(filePath)
                  else filePath
                }.toString
                violations += CheckViolation(
                  f,
                  Severity(sev),
                  rule,
                  filePath,
                  line = line.toInt,
                  position = pos.toInt
                )
              case _ => // OK not a compiler warning
            }
          },
          sys.error(_)
        )
        violations.toSeq
      }

      def process(violations: Seq[CheckViolation]): Unit = {
        import scalatags.Text.all._

        val reportFileHtml   = (baseDirectory.value / "target" / "compiler-report.html").toPath
        val fileToViolations = violations.groupBy(_.file)
        val filesOverviewRows =
          tr(th("File")(textAlign := "left"), th("Warnings")) +:
            fileToViolations.mapValues(_.size).toList.sortBy(_._1).map { case (file, count) =>
              tr(td(file), td(count)(textAlign := "center"))
            }

        val reportHeading  = h1("Compiler report")
        val projectTitle   = b(id := "project-name", "Project: $projectName")
        val summaryHeading = h2(id := "summary", "Summary")
        val summaryOverview =
          table(tr(th("Files"), th("Warnings")), tr(td(filesOverviewRows.size - 1), td(violations.size)))
        val filesHeading = h2(id := "files", "Files")
        val rulesHeading = h2(id := "rules", "Rules")
        val rulesOverView = table(
          tr(th("Rule")(textAlign := "left"), th("Warnings")) +:
            violations.groupBy(_.rule).mapValues(_.size).toList.sortBy(_._1).map {
              case (rule, count) =>
                tr(td(rule), td(count)(textAlign := "center"))
            }
        )
        val filesOverview  = table(filesOverviewRows)
        val detailsHeading = h2(id := "details", "Details")
        val detailsOverview = fileToViolations.toList.sortBy(_._1).map { case (file, violations) =>
          div(
            h3(file)(id := file.replace('/', '.')),
            table(
              tr(th("Rule")(textAlign := "left"), th("line"))
                +: violations.sortBy(_.line).map { violation =>
                  tr(td(violation.rule), td(violation.line))
                }
            )
          )
        }

        val page = html(
          body(
            reportHeading,
            projectTitle,
            summaryHeading,
            summaryOverview,
            filesHeading,
            filesOverview,
            rulesHeading,
            rulesOverView,
            detailsHeading,
            detailsOverview
          )
        )
        Files.write(reportFileHtml, page.toString().getBytes(StandardCharsets.UTF_8))
        println(s"Compile report written to ${reportFileHtml.toString}")
        val threshold = checkCompilerThreshold.value
        if (violations.size > threshold) {
          throw new MessageOnlyException(
            s"Check compiler threshold exceed, threshold is ${threshold}, number of violations: ${violations.size}"
          )
        }
      }

      val violations = readViolations()
      process(violations)

      ()
    }
  )

  lazy val libraryDependencySettings = Seq(
    libraryDependencies += scalaTest % Test,
    libraryDependencies += catsCore,
    libraryDependencies += csv,
    libraryDependencies += csvGeneric,
    libraryDependencies += scalaTags,
    libraryDependencies += scopt,
    libraryDependencies += uPickle
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

}

object Dependencies {
  lazy val catsCore   = "org.typelevel"    %% "cats-core"          % "2.0.0"
  lazy val csv        = "com.nrinaudo"     %% "kantan.csv"         % "0.6.1"
  lazy val csvGeneric = "com.nrinaudo"     %% "kantan.csv-generic" % "0.6.1"
  lazy val scalaTags  = "com.lihaoyi"      %% "scalatags"          % "0.8.2"
  lazy val scopt      = "com.github.scopt" %% "scopt"              % "4.0.0-RC2"
  lazy val uPickle    = "com.lihaoyi"      %% "upickle"            % "1.2.2"
  lazy val scalaTest  = "org.scalatest"    %% "scalatest"          % "3.2.2"
}

object CompilerReporting {
  sealed trait Severity
  case object Info    extends Severity
  case object Warning extends Severity
  case object Error   extends Severity
  object Severity {
    def apply(s: String): Severity = s.toLowerCase match {
      case "info"  => Info
      case "warn"  => Warning
      case "error" => Error
      case _       => Info
    }
  }
  case class CheckViolation(
      file: String,
      severity: Severity,
      rule: String,
      filePath: Any,
      line: Int,
      position: Int
  )

}
