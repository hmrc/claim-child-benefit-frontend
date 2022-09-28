package config

import play.api.Configuration

import javax.inject.Inject

class FeatureFlags @Inject()(configuration: Configuration) {

  val validateBankDetails: Boolean = configuration.get[Boolean]("features.validate-bank-details")
  val auditDownload: Boolean       = configuration.get[Boolean]("features.audit-download")
}
