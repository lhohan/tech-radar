sbtPlugin := true

name := "sbt-commons"

organization := "com.github.lhohan.sbt-commons"

addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"  % "0.1.14")
addSbtPlugin("ch.epfl.scala"             % "sbt-scalafix"  % "0.9.21")
addSbtPlugin("org.scoverage"             % "sbt-scoverage" % "1.6.1")
