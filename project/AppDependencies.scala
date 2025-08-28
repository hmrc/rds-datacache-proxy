import play.sbt.PlayImport.jdbc
import sbt.Keys.libraryDependencies
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.19.0"

  val oraVersion           = "19.3.0.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "com.oracle.jdbc"         %  "ojdbc8"                     % oraVersion,
    "com.oracle.jdbc"         %  "orai18n"                    % oraVersion,
    jdbc
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion            % Test,
  )

  val it = Seq.empty
}
