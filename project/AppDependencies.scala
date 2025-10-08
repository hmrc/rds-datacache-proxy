import play.sbt.PlayImport.jdbc
import sbt.*

object AppDependencies {
  private val bootstrapVersion = "10.2.0"
  val oraVersion               = "19.3.0.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "com.oracle.jdbc"         %  "ojdbc8"                     % oraVersion,
    "com.oracle.jdbc"         %  "orai18n"                    % oraVersion,
    "org.scala-lang"          % "scala-library"               % "2.13.17",
    jdbc
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion  % Test,
    "org.scalatest"           %% "scalatest"                  % "3.2.19"          % Test,
    "org.scalatestplus"       %% "scalacheck-1-17"            % "3.2.18.0"        % Test
  )

  val it: Seq[Nothing] = Seq.empty
}
