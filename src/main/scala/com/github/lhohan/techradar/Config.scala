package com.github.lhohan.techradar

import java.nio.file.{Path, Paths}

case class Config(
    sourceFile: Path = Paths.get("radar.csv"),
    targetDir: Path = Paths.get(System.getProperty("user.dir"))
)
