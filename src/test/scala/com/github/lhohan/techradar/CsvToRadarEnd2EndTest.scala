package com.github.lhohan.techradar

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class CsvToRadarEnd2EndTest extends AnyFlatSpec with Matchers {

  "The CSV to Radar conversion" should "convert a list of valid CSV records to HTML radar page" in {
    val rawData: java.net.URL = getClass.getResource("/example.csv")

    CsvToRadar.convert(
      Source.fromURL(rawData),
      testTargetPath,
      getClass.getResource("/index_template.html")
    )

    val expectedOutputFileHtml = testTargetPath.resolve("index.html")
    Files.exists(expectedOutputFileHtml) should be(true)
    val expectedOutputRadarCss = testTargetPath.resolve("radar.css")
    Files.exists(expectedOutputRadarCss) should be(true)
    val expectedOutputRadarJs = testTargetPath.resolve("radar.js")
    Files.exists(expectedOutputRadarJs) should be(true)
  }

  lazy val testTargetPath: Path = {
    val path = Paths
      .get(System.getProperty("user.dir"))
      .resolve("target")
      .resolve(System.currentTimeMillis().toString)
    Files.createDirectory(path)
    path
  }
}
