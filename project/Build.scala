import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "Svensk Bitcoin"
    val appVersion      = "1.0.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
 //    "org.bitcex" %% "bitcex" % "1.0.0-SNAPSHOT"
    )

//  val bitcex = Project("bitcex", file("../bitcex"))


    val main = PlayProject(appName, appVersion, appDependencies).settings(defaultScalaSettings:_*).settings(
      // Add your own project settings here
    )

}
