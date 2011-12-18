import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "net.renalias"
  val buildVersion      = "1.0"
  val buildScalaVersion = "2.9.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt  := ShellPrompt.buildShellPrompt,
    scalacOptions ++= Seq("-deprecation", "-optimize")
  )
}

// Shell prompt which show the current net.renalias.appengine.ocr,
// git branch and build version
object ShellPrompt {
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }
  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
  )

  val buildShellPrompt = { 
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

object Resolvers {
  val sunrepo      = "Sun Maven2 Repo" at "http://download.java.net/maven/2"
  val sunrepoGF    = "Sun GF Maven2 Repo" at "http://download.java.net/maven/glassfish" 
  val oraclerepo   = "Oracle Maven2 Repo" at "http://download.oracle.com/maven"
  val sonatyperepo = "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/releases/"
  val mavenrepo    = "Maven 2 repo" at "http://repo2.maven.org/maven2/"

  val allResolvers = Seq (sunrepo, sunrepoGF, oraclerepo, sonatyperepo, mavenrepo)
}

object Dependencies {
  val scalatraVersion = "2.0.2"
  val logbackVersion = "0.9.25"
  
  val scalatraCore    = "org.scalatra" %% "scalatra"         % scalatraVersion
  val scalatraScalate = "org.scalatra" %% "scalatra-scalate" % scalatraVersion
  val scalatraAuth    = "org.scalatra" %% "scalatra-auth"    % scalatraVersion
  val scalatraFileUpload = "org.scalatra" %% "scalatra-fileupload" % scalatraVersion
  
  val scalateBuild    = "com.mojolly.scalate" %% "scalate-generator" % "0.0.1" % "scalate"

  val logbackCore    = "ch.qos.logback" % "logback-core"     % logbackVersion
  val logbackClassic = "ch.qos.logback" % "logback-classic"  % logbackVersion  

  val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  
  val jacksonJson = "org.codehaus.jackson" % "jackson-core-lgpl" % "1.7.2"
}

object ProjectBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._
  import com.mojolly.scalate.ScalatePlugin._
  import sbtappengine.Plugin._
  import AppengineKeys._

  val deps = Seq (
    logbackCore,
    logbackClassic,
    jacksonJson,
    scalatraCore,
    scalatraScalate,
    scalatraAuth,
    scalateBuild,
    scalatraFileUpload,
    servletApi
  )

  lazy val mainProject = Project (
    "mainProject",
    file ("."),
    settings = buildSettings ++
               webSettings ++ scalateSettings ++
               Seq (resolvers := allResolvers, 
                     libraryDependencies ++= deps, 
                     scalateTemplateDirectory in Compile <<= (baseDirectory) { (basedir) => new File(basedir, "src/main/webapp/WEB-INF") }
                )
  )
}