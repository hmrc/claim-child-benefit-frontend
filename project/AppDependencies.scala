import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.4.0"
  private val hmrcMongoVersion = "1.7.0"

  val compile = Seq(
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30"     % "8.3.0",
//    "uk.gov.hmrc"                   %% "play-conditional-form-mapping"  % "1.13.0-play-28",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"             % hmrcMongoVersion,
    "uk.gov.hmrc"                   %% "crypto-json-play-30"            % "7.6.0",
    "org.typelevel"                 %% "cats-core"                      % "2.9.0",
    "uk.gov.hmrc"                   %% s"domain-play-30"                  % "9.0.0",
    "com.googlecode.libphonenumber" % "libphonenumber"                  % "8.13.12",
    "org.apache.xmlgraphics"        % "fop"                             % "2.8"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatest"           %% "scalatest"               % "3.2.15",
    "org.scalatestplus"       %% "mockito-4-6"              % "3.2.15.0",
//    "org.scalatestplus"       %% "mockito-4-6"                    % "3.2.15.0"  % Test,
//    "org.scalatestplus"       %% "scalacheck-1-15"         % "3.2.11.0",
//    "org.scalatestplus"       %% "mockito-4-6"             % "3.2.15.0",
    "org.scalacheck"          %% "scalacheck"              % "1.17.0",
//    "org.pegdown"             %  "pegdown"                 % "1.6.0",
//    "org.jsoup"               %  "jsoup"                   % "1.15.4",
//    "com.vladsch.flexmark"    %  "flexmark-all"            % "0.64.6",
//    "com.github.tomakehurst"  %  "wiremock-standalone"     % "2.27.2",
//    "io.vertx"                %  "vertx-json-schema"       % "4.4.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
