package com.github.lhohan.techradar

import cats.implicits._

import scala.io.Source
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import java.nio.file.Files
import java.nio.file.Path

import cats.data.{NonEmptyChain, ValidatedNec}
import com.github.lhohan.techradar.CsvToRadar.{
  ConversionResult,
  ConversionWarning,
  CsvRecord,
  DecodingWarning,
  JsonEntity,
  NoRadarBlips
}

object CsvToRadar extends CsvToRadar {

  case class CsvRecord(
      name: String,
      ring: String,
      quadrant: String,
      moved: String,
      description: Option[String]
  )

  case class JsonEntity(
      label: String,
      ring: Int,
      quadrant: Int,
      moved: Int,
      link: String = "",
      active: Boolean = false
  )

  // Warnings are non-fatal: they still allow to produce a result.
  sealed trait ConversionWarning
  case class DecodingWarning(msg: String) extends ConversionWarning
  case class EncodingWarning()            extends ConversionWarning

  // Error are fatal: the block reporting of a result.
  sealed trait ConversionError
  case object NoRadarBlips extends ConversionError

  type ConversionResult[A] = ValidatedNec[ConversionError, A]
}

trait CsvToRadar {
  def convert(
      source: Source,
      targetDir: Path
  ): ConversionResult[(List[ConversionWarning], Path)] = {

    val (csvResult, csvWarnings) = parseAndValidate(source)
    val jsonResult               = csvResult.map(toJson)
    val markdownResult           = csvResult.map(toMarkdown)
    markdownResult.map { content =>
      val target = targetDir.resolve("index.md")
      Files.write(target, content.getBytes)
    }
    val htmlContentResult = jsonResult.map(resolveInTemplate)
    val pathResult = htmlContentResult.map { htmlContent =>
      val targetHtml = targetDir.resolve("index.html")
      Files.write(targetHtml, htmlContent.getBytes)
    }
    pathResult.map((csvWarnings, _))
  }

  private def parseAndValidate(
      source: Source
  ): (ConversionResult[NonEmptyChain[CsvRecord]], List[ConversionWarning]) = {
    val (csvRecords, warnings) = {
      val csvIterator = read(source).asCsvReader[CsvRecord](rfc.withHeader).toList
      csvIterator.foldLeft((List.empty[CsvRecord], List.empty[ConversionWarning])) {
        case ((csvs, warnings), x) =>
          x match {
            case Right(csv)    => (csvs :+ csv, warnings)
            case Left(warning) => (csvs, warnings :+ DecodingWarning(warning.getMessage))
          }
      }
    }
    NonEmptyChain.fromSeq(csvRecords) match {
      case None      => (NoRadarBlips.invalidNec, warnings)
      case Some(nec) => (nec.validNec, warnings)
    }
  }

  private def convert(csv: CsvRecord): JsonEntity = {
    val entityMoved = csv.moved match {
      case "up"   => 1
      case "down" => -1
      case "none" => 0
      case _      => 1
    }
    val entityQuadrant = csv.quadrant.toLowerCase match {
      case "languages and frameworks" => 0
      case "techniques"               => 1
      case "platforms"                => 2
      case "tools"                    => 3
    }
    val entityRing = csv.ring.toLowerCase match {
      case "adopt"  => 0
      case "trial"  => 1
      case "assess" => 2
      case "hold"   => 3
    }
    JsonEntity(csv.name, entityRing, entityQuadrant, entityMoved)
  }

  private def toJson(csvRecords: NonEmptyChain[CsvRecord]): String = {
    val output = csvRecords.map { record =>
      val entity = convert(record)
      ujson.Obj(
        "label"    -> ujson.Str(entity.label),
        "quadrant" -> ujson.Num(entity.quadrant),
        "ring"     -> ujson.Num(entity.ring),
        "moved"    -> ujson.Num(entity.moved),
        "link"     -> ujson.Str(entity.link),
        "active"   -> ujson.Bool(entity.active)
      )
    }.toList
    ujson.write(output)
  }

  private def toMarkdown(csvRecords: NonEmptyChain[CsvRecord]): String = {
    csvRecords.toList
      .sortBy(_.name)
      .map { record =>
        s"""## ${record.name} - ${record.ring}
           |
           |${record.quadrant}
           |
           |${record.description.getOrElse("")}
           |
           |""".stripMargin
      }
      .mkString("\n")
  }

  private def resolveInTemplate(entities: String): String = {
    val templateUrl: java.net.URL = getClass.getResource("/index_template.html")
    val template                  = read(Source.fromURL(templateUrl))
    template.replace("$ENTRIES$", entities)
  }

  private def read(source: Source): String = {
    source.getLines().toList.mkString("\n")
  }
}
