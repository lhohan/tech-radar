package com.github.lhohan.techradar

import java.net.URL

import cats.implicits._

import scala.io.Source
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import java.nio.file.Files
import java.nio.file.Path
import java.time.format.DateTimeFormatter
import java.time.LocalDate

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import com.github.lhohan.techradar.CsvToRadar.{
  ConversionResult,
  ConversionWarning,
  CsvRecordDecoded,
  CsvRecord,
  DecodingWarning,
  Down,
  FirstQuadrant,
  FirstRing,
  InvalidInput,
  JsonEntity,
  JsonResult,
  Name,
  NoRadarBlips,
  NotMoved,
  ReferenceTableResult,
  SecondQuadrant,
  SecondRing,
  ThirdQuadrant,
  ThirdRing,
  Up,
  ZerothQuadrant,
  ZerothRing
}

object CsvToRadar extends CsvToRadar {

  case class CsvRecordDecoded(
      name: String,
      ring: String,
      quadrant: String,
      moved: String,
      description: Option[String]
  )

  sealed trait Quadrant
  case object ZerothQuadrant extends Quadrant
  case object FirstQuadrant  extends Quadrant
  case object SecondQuadrant extends Quadrant
  case object ThirdQuadrant  extends Quadrant

  sealed trait Ring
  case object ZerothRing extends Ring
  case object FirstRing  extends Ring
  case object SecondRing extends Ring
  case object ThirdRing  extends Ring

  sealed trait Moved
  case object Up       extends Moved
  case object Down     extends Moved
  case object NotMoved extends Moved

  case class Name(value: String) extends AnyVal

  case class CsvRecord(
      name: Name,
      ring: Ring,
      quadrant: Quadrant,
      moved: Moved,
      description: Option[String]
  )

  case class JsonEntity(
      label: String,
      ring: Int,
      quadrant: Int,
      moved: Int,
      link: String = "",
      active: Boolean = true
  )

  case class JsonResult(value: String)           extends AnyVal
  case class ReferenceTableResult(value: String) extends AnyVal

  // Warnings are non-fatal: they still allow to produce a result.
  sealed trait ConversionWarning
  case class DecodingWarning(msg: String) extends ConversionWarning
  case class InvalidInput(msg: String)    extends ConversionWarning

  // Error are fatal: the block reporting of a result.
  sealed trait ConversionError
  case object NoRadarBlips extends ConversionError

  type ConversionResult[A] = ValidatedNec[ConversionError, A]
}

trait CsvToRadar {
  def convert(
      source: Source,
      targetDir: Path,
      htmlTemplate: URL
  ): ConversionResult[(List[ConversionWarning], Path)] = {

    val (csvResult, csvWarnings) = parseAndValidate(source)
    val jsonAndReferenceResult   = csvResult.map { csv => (toJson(csv), toReference(csv)) }
    val htmlContentResult = {
      val dataResolved = jsonAndReferenceResult.map(resolveInHtmlTemplate(_, htmlTemplate))
      val dateResolved = dataResolved.map(resolveDate)
      dateResolved
    }
    val pathResult = htmlContentResult.map { htmlContent =>
      // HTML needs to radar js and css file so first copy those
      def copyResourceToTarget(resourceName: String) = {
        val resource = getClass.getResource(s"/$resourceName")
        val source   = read(Source.fromURL(resource))
        val target   = targetDir.resolve(resourceName)
        Files.write(target, source.getBytes)
      }
      val resources = Seq("radar.css", "radar.js")
      resources.foreach(copyResourceToTarget)

      val targetHtml = targetDir.resolve("index.html")
      Files.write(targetHtml, htmlContent.getBytes)
    }
    pathResult.map((csvWarnings, _))
  }

  private def parseAndValidate(
      source: Source
  ): (ConversionResult[NonEmptyChain[CsvRecord]], List[ConversionWarning]) = {
    val (csvRecords, warnings) = {
      val csvIterator = read(source).asCsvReader[CsvRecordDecoded](rfc.withHeader).toList
      csvIterator.foldLeft((List.empty[CsvRecord], List.empty[ConversionWarning])) {
        case ((csvs, warnings), x) =>
          x match {
            case Right(csv) =>
              validate(csv) match {
                case Invalid(errorMsg) => (csvs, warnings :+ InvalidInput(errorMsg))
                case Valid(csv)        => (csvs :+ csv, warnings)
              }
            case Left(warning) => (csvs, warnings :+ DecodingWarning(warning.getMessage))
          }
      }
    }
    NonEmptyChain.fromSeq(csvRecords) match {
      case None      => (NoRadarBlips.invalidNec, warnings)
      case Some(nec) => (nec.validNec, warnings)
    }
  }

  private def validate(csv: CsvRecordDecoded): Validated[String, CsvRecord] = {
    val moved = csv.moved match {
      case "up"   => Up.valid
      case "down" => Down.valid
      case "none" => NotMoved.valid
      case unsupported: Any =>
        s"CSV record '${csv.name}' in invalid, 'moved' value not supported: '${unsupported}'".invalid
    }
    val quadrant = csv.quadrant.toLowerCase match {
      case "languages and frameworks" => ZerothQuadrant.valid
      case "techniques"               => FirstQuadrant.valid
      case "platforms"                => SecondQuadrant.valid
      case "tools"                    => ThirdQuadrant.valid
      case unsupported: Any =>
        s"CSV record '${csv.name}' in invalid, 'quadrant' value not supported: '${unsupported}'".invalid

    }
    val ring = csv.ring.toLowerCase match {
      case "adopt"  => ZerothRing.valid
      case "trial"  => FirstRing.valid
      case "assess" => SecondRing.valid
      case "hold"   => ThirdRing.valid
      case unsupported: Any =>
        s"CSV record '${csv.name}' in invalid, 'ring' value not supported: '${unsupported}'".invalid

    }

    val name = {
      val n = if (csv.name.forall(_.isWhitespace)) {
        s"CSV record '${csv.name}' in invalid, 'name' value should not be blank".invalid
      } else {
        val MaxNameLength = 22 // to fit in radar columns
        if (csv.name.length > MaxNameLength) {
          s"CSV record '${csv.name}' in invalid, 'name' value is too long, should be max $MaxNameLength is ${csv.name.length}".invalid
        } else {
          csv.name.valid
        }
      }
      n.map(Name)
    }

    (name, ring, quadrant, moved).mapN { (n, r, q, m) =>
      CsvRecord(n, r, q, m, csv.description)
    }
  }

  private def convert(csv: CsvRecord): JsonEntity = {
    val entityMoved = csv.moved match {
      case Up       => 1
      case Down     => -1
      case NotMoved => 0
    }
    val entityQuadrant = csv.quadrant match {
      case ZerothQuadrant => 0
      case FirstQuadrant  => 1
      case SecondQuadrant => 2
      case ThirdQuadrant  => 3
    }
    val entityRing = csv.ring match {
      case ZerothRing => 0
      case FirstRing  => 1
      case SecondRing => 2
      case ThirdRing  => 3
    }
    JsonEntity(csv.name.value, entityRing, entityQuadrant, entityMoved)
  }

  private def toJson(csvRecords: NonEmptyChain[CsvRecord]): JsonResult = {
    val output = csvRecords.map { record =>
      val entity = convert(record)
      ujson.Obj(
        "label"    -> ujson.Str(entity.label),
        "quadrant" -> ujson.Num(entity.quadrant),
        "ring"     -> ujson.Num(entity.ring),
        "moved"    -> ujson.Num(entity.moved),
        "link"     -> ujson.Str("#" + generateId(entity.label)),
        "active"   -> ujson.Bool(entity.active)
      )
    }.toList
    JsonResult(ujson.write(output))
  }

  private def toReference(csvRecords: NonEmptyChain[CsvRecord]): ReferenceTableResult = {
    import scalatags.Text.all._

    def htmlRecord(csv: CsvRecord) = {
      val idVal = generateId(csv.name.value)
      div(cls := "radar-record", id := idVal)(
        Seq(
          p(h3(s"${csv.name.value}")),
          p(csv.description.map(d => raw(d)).getOrElse(""))
        )
      )
    }

    val htmlRecords = csvRecords.toList
      .sortBy(_.name.value)
      .map(htmlRecord)

    val tabled = table(
      tr(td(h2("Tech Reference")), td("")),
      htmlRecords.grouped(2).toList.map {
        case List(c1, c2) => tr(td(c1), td(c2))
        case List(c1)     => tr(td(c1), td(""))
      }
    )

    ReferenceTableResult(
      p(raw("&nbsp;")).toString() +
        tabled.toString()
    )
  }

  private def generateId(s: String): String = {
    s.collect {
      case ' '                    => '-'
      case '-'                    => '-'
      case c if c.isLetterOrDigit => c
    }.toLowerCase
  }

  private def resolveInHtmlTemplate(
      results: (JsonResult, ReferenceTableResult),
      htmlTemplate: java.net.URL
  ): String = {
    val template = read(Source.fromURL(htmlTemplate))
    template
      .replace("$ENTRIES$", results._1.value)
      .replace("$REFERENCE_TABLE$", results._2.value)
  }

  private def resolveDate(template: String): String = {
    val date      = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM")
    val text      = formatter.format(date)
    template.replace("$TIME$", text)
  }

  private def read(source: Source): String = {
    source.getLines().toList.mkString("\n")
  }
}
