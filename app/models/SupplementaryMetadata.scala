package models

import java.time.Instant

final case class SupplementaryMetadata(
                                        nino: String,
                                        submissionDate: Instant,
                                        correlationId: String
                                      )
