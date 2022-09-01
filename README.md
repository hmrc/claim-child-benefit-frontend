
# claim-child-benefit-frontend

This service allows unauthenticated users to claim Child Benefit by answering the relevant questions, printing out a PDF and posting it to HMRC to be processed.  It replaces an existing iForm offering similar functionality.

The service calls the Bank Account Reputation service’s [Validate bank details endpoint](https://github.com/hmrc/bank-account-reputation/blob/main/docs/eiscd/v3/validateBankDetails.md), but otherwise does not integrate with any other services beyond those ubiquitous to the platform (audit, contact, tracking consent etc.)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").