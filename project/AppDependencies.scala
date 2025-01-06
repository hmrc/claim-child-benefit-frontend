import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.5.0"
  private val hmrcMongoVersion = "2.3.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30"     % "11.7.0",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"             % hmrcMongoVersion,
    "uk.gov.hmrc"                   %% "crypto-json-play-30"            % "8.1.0",
    "org.typelevel"                 %% "cats-core"                      % "2.12.0",
    "uk.gov.hmrc"                   %% s"domain-play-30"                % "10.0.0",
    "com.googlecode.libphonenumber" % "libphonenumber"                  % "8.13.42",
    "org.apache.xmlgraphics"        % "fop"                             % "2.9"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalacheck"          %% "scalacheck"              % "1.18.0",
    "org.scalatestplus"       %% "scalacheck-1-17"         % "3.2.18.0",
    "io.vertx"                %  "vertx-json-schema"       % "4.5.9"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
