
# claim-child-benefit-frontend

This service allows authenticated and unauthenticated users to claim Child Benefit by answering the relevant questions, printing out a PDF and posting it to HMRC to be processed.  It replaces an existing iForm offering similar functionality.

The service calls the Bank Account Reputation service’s [Validate bank details endpoint](https://github.com/hmrc/bank-account-reputation/blob/main/docs/eiscd/v3/validateBankDetails.md), but otherwise does not integrate with any other services beyond those ubiquitous to the platform (audit, contact, tracking consent etc.)

### How to run the service
You can run the service using service manager with profile `CLAIM_CHILD_BENEFIT_ALL` `CLAIM_CHILD_BENEFIT_FRONTEND` or locally with `sbt "run 11303"`

1. To begin the journey got to - http://localhost:11303/fill-online/claim-child-benefit/
2. You'll be prompted to `Make a new claim`
3. For unauthenticated journey there is no need to sign up with Government Gateway
4. For authenticated journey you select Yes and `Register SCP User` submit and select `Successful SCP MFA`
5. You can then select a journey result. A succesful NINO and slection would be `AB111111A` and `Success`. [Refer to stub on rules of NINO](https://github.com/hmrc/claim-child-benefit-stub#get--------individualsdetailsninonino)
6. You can then start the claim journey. [Refer to Stub](https://github.com/hmrc/claim-child-benefit-stub) in order to determine the data based on your testing scenario 


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
