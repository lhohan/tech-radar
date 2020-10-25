package com.github.lhohan.techradar

import java.nio.file.Paths

import cats.data.Chain
import cats.data.Validated.Invalid
import com.github.lhohan.techradar.CsvToRadar.NoRadarBlips
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class CsvToRadarSpec extends AnyFlatSpec with Matchers {

  "The CSV to JSON conversion" should "report an error when no csv record is present" in {

    val csvOnlyHead = Source.fromString("name,ring,quadrant,moved,description\n")

    val result =
      CsvToRadar.convert(
        csvOnlyHead,
        Paths.get(System.getProperty("user.dir")).resolve("target"),
        getClass.getResource("/index_template.html")
      )

    result.isInvalid should be(true)
    val Invalid(Chain(error)) = result
    error shouldBe NoRadarBlips
  }
}
