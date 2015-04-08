import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.rjs.Import._
import com.typesafe.sbt.web.Import._
import play.PlayImport._
import play.PlayScala
import sbt.Keys._
import sbt._

/**
 * This is a simple sbt setup generating Slick code from the given
 * database before compiling the projects code.
 */
object myBuild extends Build {

  lazy val mainProject = Project(
    id="main",
    base=file("codegen"),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.10.3",
      libraryDependencies ++= List(
        "com.typesafe.slick" %% "slick" % "2.1.0",
        "com.typesafe.slick" %% "slick-codegen" % "2.1.0-RC3",
        "org.slf4j" % "slf4j-nop" % "1.6.4",
        "com.h2database" % "h2" % "1.3.170"
      ),
      slick <<= slickCodeGenTask, // register manual sbt command
      sourceGenerators in Compile <+= slickCodeGenTask // register automatic code generation on every compile, remove for only manual use
    )
  )

  // code generation task
  lazy val slick = TaskKey[Seq[File]]("gen-tables")

  lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
    val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder
  val url = "jdbc:h2:mem:test;INIT=runscript from 'codegen/src/main/sql/create.sql'" // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
  val jdbcDriver = "org.h2.Driver"
    val slickDriver = "scala.slick.driver.H2Driver"
    val pkg = "demo"
    toError(r.run("scala.slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg), s.log))
    val fname = outputDir + "/demo/Tables.scala"
    Seq(file(fname))
  }



  lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(
    libraryDependencies ++= List(
    "com.typesafe.slick" %% "slick" % "2.1.0",
    "com.typesafe.slick" %% "slick-codegen" % "2.1.0-RC3",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.h2database" % "h2" % "1.3.170"
    ),

    // TODO Replace with your project's/module's name
    name := """play-angular-require-seed""",

    // TODO Set your organization here; ThisBuild means it will apply to all sub-modules
    organization in ThisBuild := "your.organization",

    // TODO Set your version here
    version := "2.3.7-SNAPSHOT",

    // Scala Version, Play supports both 2.10 and 2.11
    //scalaVersion := "2.10.4"
    scalaVersion := "2.11.4",

    // Dependencies
    libraryDependencies ++= Seq(
      filters,
      cache,
      // WebJars (i.e. client-side) dependencies
      "org.webjars" % "requirejs" % "2.1.14-1",
      "org.webjars" % "underscorejs" % "1.6.0-3",
      "org.webjars" % "jquery" % "1.11.1",
      "org.webjars" % "bootstrap" % "3.1.1-2" exclude("org.webjars", "jquery"),
      "org.webjars" % "angularjs" % "1.2.18" exclude("org.webjars", "jquery")
    ),

    // Scala Compiler Options
    scalacOptions in ThisBuild ++= Seq(
      "-target:jvm-1.7",
      "-encoding", "UTF-8",
      "-deprecation", // warning and location for usages of deprecated APIs
      "-feature", // warning and location for usages of features that should be imported explicitly
      "-unchecked", // additional warnings where generated code depends on assumptions
      "-Xlint", // recommended additional warnings
      "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
      "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
      "-Ywarn-inaccessible",
      "-Ywarn-dead-code"
    ),

    //
    // sbt-web configuration
    // https://github.com/sbt/sbt-web
    //

    // Configure the steps of the asset pipeline (used in stage and dist tasks)
    // rjs = RequireJS, uglifies, shrinks to one file, replaces WebJars with CDN
    // digest = Adds hash to filename
    // gzip = Zips all assets, Asset controller serves them automatically when client accepts them
    pipelineStages := Seq(rjs, digest, gzip),

    // RequireJS with sbt-rjs (https://github.com/sbt/sbt-rjs#sbt-rjs)
    // ~~~
    RjsKeys.paths += ("jsRoutes" -> ("/jsroutes" -> "empty:")),

    //RjsKeys.mainModule := "main"

    // Asset hashing with sbt-digest (https://github.com/sbt/sbt-digest)
    // ~~~
    // md5 | sha1
    //DigestKeys.algorithms := "md5"
    //includeFilter in digest := "..."
    //excludeFilter in digest := "..."

    // HTTP compression with sbt-gzip (https://github.com/sbt/sbt-gzip)
    // ~~~
    // includeFilter in GzipKeys.compress := "*.html" || "*.css" || "*.js"
    // excludeFilter in GzipKeys.compress := "..."

    // JavaScript linting with sbt-jshint (https://github.com/sbt/sbt-jshint)
    // ~~~
    // JshintKeys.config := ".jshintrc"

    // All work and no play...
    emojiLogs
  )


}
