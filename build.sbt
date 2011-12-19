import sbtappengine.Plugin.{AppengineKeys => gae}

name := "appengine-ocr-poc"

version := "1.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
    "org.specs2" %% "specs2" % "1.6.1",
    "org.specs2" %% "specs2-scalaz-core" % "6.0.1" % "test",
    "junit" % "junit" % "4.9",    
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
)

seq(appengineSettings: _*)