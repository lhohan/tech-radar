lazy val root = (project in file("."))
  .settings(
    name := "tech-radar-from-csv",
    generateBuildInfo := {
      val file = (sourceDirectory in Compile).value / "scala" / "buildinfo" / "BuildInfo.scala"
      IO.write(
        file,
        s"""//format: off
           |package buildinfo
           |
           |object BuildInfo {
           |  val version: String      = "${version.value}"
           |  val scalaVersion: String = "${scalaVersion.value}"
           |  val sbtVersion: String   = "${sbtVersion.value}"
           |  val builtAtMillis: Long  = ${System.currentTimeMillis()}L
           |}
           |
           |""".stripMargin
      )
      Seq(file)
    },
    compile in Compile := {
      generateBuildInfo.value
      (compile in Compile).value
    }
  )

lazy val generateBuildInfo = taskKey[Seq[File]]("Generates the build information")
