package com.github.lhohan.techradar

import java.io.File
import java.nio.file.Files

import buildinfo.BuildInfo
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import com.github.lhohan.techradar.CsvToRadar.DecodingWarning
import com.github.lhohan.techradar.CsvToRadar.InvalidInput
import com.github.lhohan.techradar.CsvToRadar.NoRadarBlips

import scala.io.Source

object Main extends CsvToRadar with App {

  val parser = new scopt.OptionParser[Config]("CSV to Radar") {
    head("CSV to Radar", BuildInfo.version)

    opt[File]('t', "target")
      .optional()
      .valueName("<target dir>")
      .action { (x, c) =>
        c.copy(targetDir = x.toPath)
      }
      .validate { f =>
        val path = f.toPath
        if (Files.notExists(path)) {
          val created = Files.createDirectories(path)
          println(s"Target dir auto-created: ${created.toAbsolutePath.toString}")
        }
        success
      }
      .text("""
          |Target dir, optional.
          |If not specified the radar files will be created in the run location of this command. Existing files will be overwritten.
          |""".stripMargin)

    opt[File]('s', "source")
      .required()
      .valueName("<csv file>")
      .action { (x, c) =>
        c.copy(sourceFile = x.toPath)
      }
      .validate { f =>
        if (Files.exists(f.toPath) && f.getName.endsWith(".csv")) {
          success
        } else {
          failure(
            s"Option --source : csv file does not exist or does not end with '.csv': ${f.toPath.toAbsolutePath.toString}"
          )
        }
      }
      .text("""
          |CSV file containing the radar entries.
          |""".stripMargin)

    opt[File]('p', "template")
      .optional()
      .valueName("<path to HTML template>")
      .action { (x, c) =>
        c.copy(htmlTemplate = x.toPath.toUri.toURL)
      }
      .validate { f =>
        val path = f.toPath
        if (Files.notExists(path)) {
          failure(
            s"Option --template: template file does not exist: ${f.toPath.toAbsolutePath.toString}"
          )
        } else {
          success
        }
      }
      .text("""
              |Path to Tech Radar template file.
              |If not template is specified a default one will be used.
              |An example template file that can customized is available here https://github.com/lhohan/tech-radar/blob/main/src/main/resources/index_template.html
              |""".stripMargin)

  }

  parser.parse(args, Config()) match {
    case Some(config) =>
      CsvToRadar.convert(
        Source.fromFile(config.sourceFile.toFile),
        config.targetDir,
        config.htmlTemplate
      ) match {
        case Invalid(e) =>
          e.toNonEmptyList.toList.foreach { case NoRadarBlips =>
            System.err.println("No radar blips present")
          }
        case Valid((warnings, path)) =>
          if (warnings.nonEmpty) {
            println("Warnings (radar blips not shown):")
            warnings.foreach {
              case InvalidInput(msg)    => println(s"  Invalid input      : $msg")
              case DecodingWarning(msg) => println(s"  Failed decoding CSV: $msg")
            }
            println()
          }
          println(s"Radar written to ${path.toRealPath()}")
      }
    case None => println("Please correct error(s) above")
  }

}
