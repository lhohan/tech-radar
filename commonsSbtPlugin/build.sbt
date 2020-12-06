sbtPlugin := true

name := "sbt-commons"

organization := "com.github.lhohan.sbt-commons"

libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.8.2"

addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"    % "0.1.15")
addSbtPlugin("ch.epfl.scala"             % "sbt-scalafix"    % "0.9.21")
addSbtPlugin("org.scoverage"             % "sbt-scoverage"   % "1.6.1")
addSbtPlugin("com.dwijnand"              % "sbt-dynver"      % "4.1.1")
addSbtPlugin("org.wartremover"           % "sbt-wartremover" % "2.4.13")
addSbtPlugin("com.beautiful-scala"       % "sbt-scalastyle"  % "1.5.0")
