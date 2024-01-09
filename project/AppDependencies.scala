import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.1.0"
  private val hmrcMongoVersion = "1.7.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30"     % "8.1.0",
//    "uk.gov.hmrc"                   %% "play-conditional-form-mapping"  % "1.13.0-play-28",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"             % hmrcMongoVersion,
    "uk.gov.hmrc"                   %% "crypto-json-play-30"            % "7.6.0",
    "org.typelevel"                 %% "cats-core"                      % "2.3.0",
    "uk.gov.hmrc"                   %% s"domain-play-30"                  % "9.0.0",
    "com.googlecode.libphonenumber" % "libphonenumber"                  % "8.12.47",
    "org.apache.xmlgraphics"        % "fop"                             % "2.8"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatest"           %% "scalatest"               % "3.2.10",
    "org.scalatestplus"       %% "scalacheck-1-15"         % "3.2.10.0",
    "org.scalatestplus"       %% "mockito-3-4"             % "3.2.10.0",
    "org.mockito"             %% "mockito-scala"           % "1.16.42",
    "org.scalacheck"          %% "scalacheck"              % "1.15.4",
    "org.pegdown"             %  "pegdown"                 % "1.6.0",
    "org.jsoup"               %  "jsoup"                   % "1.14.3",
    "com.vladsch.flexmark"    %  "flexmark-all"            % "0.62.2",
    "com.github.tomakehurst"  %  "wiremock-standalone"     % "2.27.2",
    "io.vertx"                %  "vertx-json-schema"       % "4.3.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
