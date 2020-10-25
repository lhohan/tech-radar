package com.github.lhohan.techradar

import java.io.File
import java.nio.file.Files

import scala.io.Source

object Main extends CsvToRadar with App {

  val parser = new scopt.OptionParser[Config]("CSV to Radar") {
    head("CSV to Radar", "0.1.0")

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

  }

  parser.parse(args, Config()) match {
    case Some(config) =>
      CsvToRadar.convert(Source.fromFile(config.sourceFile.toFile), config.targetDir)
    case None => println("Please correct error(s) above")
  }

}
