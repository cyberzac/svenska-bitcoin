import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "Svenska Bitcoin"
  val appVersion = "1.0.2-SNAPSHOT"
  val retrievedManaged = true
  val logbackVersion = "1.0.0"
  val slf4jVersion = "1.6.2"
  val scalaVersion = "2.9.2"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "org.mockito" % "mockito-all" % "1.9.0",
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "ch.qos.logback" % "logback-core" % logbackVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "org.jasypt" % "jasypt" % "1.9.0"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(

    libraryDependencies += "com.typesafe.akka" % "akka-testkit" % "2.0"
    // Add your own project settings here
  )

  }

//  val appDependencies = Seq(
   // "org.bitcex" %% "bitcex-core" % "1.0.0-SNAPSHOT"
//  )

  //  val bitcex = Project("bitcex", file("../bitcex"))


 // val main = PlayProject(appName, appVersion, appDependencies).settings(defaultScalaSettings: _*).settings(
    // Add your own project settings here
 //  resolvers += Resolver.file("local Ivy2", file(Path.userHome.absolutePath+"/.ivy2/local"))(Resolver.ivyStylePatterns) //transactional()
/*    resolvers ++= Seq (
         Resolver.file("Local Ivy2", file(Path.userHome.absolutePath+"/.ivy2/local"))(Resolver.ivyStylePatterns), //transactional(),
          "Local Maven" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
          "Saxon" at "http://www.eviware.com/repository/maven2",
         "Restlet" at "http://maven.restlet.org"
      )
*/
 // )
