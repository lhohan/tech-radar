package com.github.lhohan.techradar

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

case class Config(
    sourceFile: Path = Paths.get("radar.csv"),
    targetDir: Path = Paths.get(System.getProperty("user.dir")),
    htmlTemplate: URL = Config.getClass.getResource("/index_template.html")
)
