resolvers ++= Seq(
  "sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
  Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)
)

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "0.11.0")

libraryDependencies <+= sbtVersion(v => "com.mojolly.scalate" %% "xsbt-scalate-generator" % (v + "-0.1.4"))

addSbtPlugin("com.eed3si9n" %% "sbt-appengine" % "0.3.1")

libraryDependencies <+= sbtVersion(v => "org.fusesource.scalate" % "sbt-scalate-plugin" % ("1.5.3"))