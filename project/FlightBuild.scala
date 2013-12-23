import sbt._
import Keys._
import java.io.File
import scala.io.Source
import IO._

object BuildSettings {

  val buildSettings: Seq[Setting[_]] = Defaults.defaultSettings ++ Seq[Setting[_]](
    organization := "com.flyobjectspace",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.10.3",
    scalaBinaryVersion := "2.10")
}

object Dependencies {
  val specs2 = Seq(
    "org.scala-lang" % "scala-reflect" % "2.10.3",
    "org.scalatest" %% "scalatest" % "1.9.2" % "test")
}

/* see http://www.cakesolutions.net/teamblogs/2012/01/28/publishing-sbt-projects-to-nexus/
 * Instructions from sonatype: https://issues.sonatype.org/browse/OSSRH-2841?focusedCommentId=150049#comment-150049
 * Deploy snapshot artifacts into repository https://oss.sonatype.org/content/repositories/snapshots
 * Deploy release artifacts into the staging repository https://oss.sonatype.org/service/local/staging/deploy/maven2
 * Promote staged artifacts into repository 'Releases'
 * Download snapshot and release artifacts from group https://oss.sonatype.org/content/groups/public
 * Download snapshot, release and staged artifacts from staging group https://oss.sonatype.org/content/groups/staging
 * Actually publishing:
   For snapshots make sure the version number has a -SNAPHOT suffix. To release to staging, remove the suffix.
    In both cases use 'sbt publish-signed'

    To actually release:
    (See section 8a https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-7b.StageExistingArtifacts)
        Do a staging release by removing the -SNAPSHOT in the version number and publish-signed
      Login to the Nexus UI at https://oss.sonatype.org/
      Under 'Build Promotion' select Staging Repositories.
      Find our release item and click it
      Click the Close button (next to the Refresh button)
      Wait ...
        Keep checking the Activity tab to see if any rules failed.
      If that succeeds, select the release again and click 'Release'
      Wait ...
        Keep checking the Activity tab to see if there are any problems
      Look for the release in https://oss.sonatype.org/content/repositories/releases/com/flyobjectspace/...
 */
object Publishing {

  def publishSettings: Seq[Setting[_]] = Seq(
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    publishTo <<= version { v: String =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },

    pomExtra := (
      <licenses>
        <license>
          <name>MIT License</name>
          <url>http://www.opensource.org/licenses/mit-license/</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:fly-object-space/Flight.git</url>
        <connection>scm:git:git@github.com:fly-object-space/Flight.git</connection>
      </scm>
      <description>
        Flight is an asynchronous version of the Fly Object Spaces that runs ( for the moment ) in a local Java Virtual Machine.
      </description>
      <url>http://www.flyobjectspace.com</url>
      <developers>
        <developer>
          <id>cjw</id>
          <name>Channing Walton</name>
          <email>channing [dot] walton [at] underscoreconsulting [dot] com</email>
          <organization>Underscore Consulting Ltd</organization>
        </developer>
        <developer>
          <id>nw</id>
          <name>Nigel Warren</name>
          <organization>Zink Digital Ltd</organization>
        </developer>
      </developers>
      <mailingLists>
        <mailingList>
          <name>User and Developer Discussion List</name>
          <archive>http://groups.google.com/group/flyobjectspace</archive>
          <post>flyobjectspace@googlegroups.com</post>
          <subscribe>flyobjectspace+subscribe@googlegroups.com</subscribe>
          <unsubscribe>flyobjectspace+unsubscribe@googlegroups.com</unsubscribe>
        </mailingList>
      </mailingLists>))
}

object FlightBuild extends Build {

  import BuildSettings._
  import Dependencies._
  import Publishing._

  lazy val flight = Project(
    "Flight",
    file("."),
    settings = buildSettings ++ publishSettings ++ Seq(resolvers := Seq(Classpaths.typesafeResolver), libraryDependencies ++= specs2))
}
