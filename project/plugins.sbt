lazy val root = (project in file(".")).dependsOn(commonPlugin)

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.0")

lazy val commonPlugin = RootProject(file("../commonsSbtPlugin"))

