import com.typesafe.sbt.packager.docker.Cmd

name := "codacy-spotbugs"

scalaVersion := "2.12.10"

val findsecbugsVersion = "1.10.1"
val sbContribVersion = "7.4.7"
val spotBugsVersion = "4.0.1"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "com.codacy" %% "codacy-engine-scala-seed" % "4.0.0",
  "com.github.spotbugs" % "spotbugs" % spotBugsVersion,
  "com.h3xstream.findsecbugs" % "findsecbugs-plugin" % findsecbugsVersion,
  "com.mebigfatguy.sb-contrib" % "sb-contrib" % sbContribVersion,
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",
  "com.github.tkqubo" % "html-to-markdown" % "0.7.2"
).map(_.withSources())

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.7" % Test

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)

sourceGenerators.in(Compile) += Def.task {
  val file = sourceManaged.in(Compile).value / "com" / "codacy" / "tools" / "spotbugs" / "Keys.scala"
  IO.write(file, s"""package com.codacy.tools.spotbugs
                    |
                    |object Keys {
                    |
                    |  val toolName: String = "spotbugs"
                    |
                    |  val spotBugsVersion: String = "$spotBugsVersion"
                    |  val findsecbugsVersion: String = "$findsecbugsVersion"
                    |  val sbContribVersion: String = "$sbContribVersion"
                    |
                    |  val defaultLinuxInstallLocation: String = "${defaultLinuxInstallLocation.in(Docker).value}"
                    |  val dependenciesLocation: String = "${csrCacheDirectory.in(Compile).value}"
                    |
                    |}
                    |""".stripMargin)
  Seq(file)
}.taskValue

mappings in Universal ++= {
  (resourceDirectory in Compile).map { resourceDir: File =>
    val src = resourceDir / "docs"
    val dest = "/docs"

    for {
      path <- src.allPaths.get if !path.isDirectory
    } yield path -> path.toString.replaceFirst(src.toString, dest)
  }
}.value

Universal / javaOptions ++= Seq("-XX:MinRAMPercentage=60.0", "-XX:MaxRAMPercentage=95.0")

val dockerUser = "docker"

Docker / daemonUser := dockerUser
Docker / daemonGroup := dockerUser

dockerBaseImage := "openjdk:8-jre-alpine"

dockerEntrypoint := Seq("/opt/docker/bin/engine")

dockerCommands := dockerCommands.value.flatMap {
  case cmd @ Cmd("WORKDIR", _) => Seq(Cmd("WORKDIR", "/src"))
  case cmd @ Cmd("ADD", _) =>
    Seq(Cmd("RUN", s"adduser -u 2004 -D $dockerUser"), cmd, Cmd("RUN", "mv /opt/docker/docs /docs"))
  case other => List(other)
}
