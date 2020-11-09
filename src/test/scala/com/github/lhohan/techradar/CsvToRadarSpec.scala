package com.github.lhohan.techradar

import java.nio.file.Paths

import cats.data.Chain
import cats.data.Validated.{Invalid, Valid}
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

  "The CSV to JSON conversion" should "a report warning for an invalid csv record" in {

    val csvOnlyHead = Source.fromString("""name,ring,quadrant,moved,description
        |Decision Records,adopt,techniques,none,"Highly readable code and tests can help document and increase long term viability of projects and components. However not all decisions can be easily expressed this way and change does happen. To record certain design decisions for the benefit of future team members as well as for external oversight Lightweight Decision Records can be used a technique for capturing important design or architectural decisions along with their context and consequences. We store these details in source control, instead of a wiki or website, as then they can provide a record that remains in sync with the code itself. (Note: this documentation could also be generated from annotions in the code.)"
        |Wrong technique,none,techniques,none,""
        |""".stripMargin)

    val result =
      CsvToRadar.convert(
        csvOnlyHead,
        Paths.get(System.getProperty("user.dir")).resolve("target"),
        getClass.getResource("/index_template.html")
      )

    result.isValid should be(true)
    val Valid((warnings, _)) = result
    warnings should not be empty
  }
}
