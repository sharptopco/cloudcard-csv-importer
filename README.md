# CloudCard CSV Importer

Summary:
---
Imports cardholders from a CSV file into [CloudCard Online Photo Submission](https://onlinephotosubmission.com). This project is designed to be run as
a scheduled job and in conjunction with a scheduled report that saves a CSV of new cardholders in a preconfigured input directory, which essentially
automates importing new cardholders in CloudCard.

Tutorial Videos
---

1. [YouTube: How to Create a Service Account](https://youtu.be/_J9WKAMZOdY)
1. [YouTube: CloudCard CSV Importer Tutorial](https://youtu.be/Pu6HXLk6jZ4)

Requirements:
---

## Requirements

- JDK 1.8 - Choose one of the following:
  - Amazon Corretto 8 (recommended)
    - [Download](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html)
    - [Windows Installation Instructions](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/windows-7-install.html)
    - [Linux Installation Instructions](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/generic-linux-install.html)
  - [Red Hat OpenJDK 8](https://developers.redhat.com/products/openjdk/download)
  - Oracle JDK (requires an Oracle support license)
- 512MB RAM
- Storage: 1GB 
- OS: Any
- Processor: Any
- Storage Location - OS or Data: Any
- [Service account with office level access](https://sharptop.atlassian.net/wiki/spaces/CCD/pages/1226440705/User+Roles) to CloudCard Online Photo Submission
- Outbound network access to the following servers/ports if your organization requires all outbound traffic to be whitelisted
  - api.onlinephotosubmission.com:443
  - api.cloudcard.ca:443 (only necessary if using CloudCard's Canada specific instance)
  - test-api.onlinephotosubmission.com:443 (only necessary if using CloudCard's test instance)

To test your system, run `java -version`.  The output should look like the following.  The exact version isn't important as long as it starts with `1.8`.
> openjdk version "1.8.0_232" <br/>
> OpenJDK Runtime Environment Corretto-8.232.09.2 (build 1.8.0_232-b09) <br/>
> OpenJDK 64-Bit Server VM Corretto-8.232.09.2 (build 25.232-b09, mixed mode)

Usage:
---

1. Create a separate service account for CloudCard Photo Downloader to use. ([Instructions](https://youtu.be/_J9WKAMZOdY))
1. Download [cloudcard-csv-importer.jar](https://github.com/sharptopco/cloudcard-csv-importer/raw/master/cloudcard-csv-importer.jar).
    - **Note**: Transact Online Photo Submission customers should download [this version](https://github.com/sharptopco/cloudcard-csv-importer/raw/master/22.09.26/cloudcard-csv-importer.jar).
1. In a terminal window, run the command `java -jar cloudcard-csv-importer.jar {path_to_properties_file}` in the same directory as the JAR file

If not argument is specified `cloudcard-csv-importer` will look for a file called `config.properties` in the current directory.

Configuration:
---
Create a properties file similar to `src/config-template.properties`

* `input.directory`: the directory into which your automated report should save CSV files.
* `completed.directory`: once imported, completed CSV files are moved into this directory.
* `report.directory`: a line by line report will be generated in this directory for each input file.
* `base.url`: the URL for the online photo submission API. 
  *  Production: `https://api.onlinephotosubmission.com`
  *  Canada: `https://api.cloudcard.ca`
  *  Test: `https://test-api.onlinephotosubmission.com`
  *  Transact: `https://onlinephoto-api.transactcampus.net`
* `access.token`: the API key that authenticates the importer with CloudCard ([Video](https://www.youtube.com/watch?v=_J9WKAMZOdY)).
* `character.set`: the character set encoding for input files, i.e. `utf8`, `utf16`. Default: `utf8`

CSV Requirements
---

- The file must include a header row.
- The number of fields in each row should always match the number of fields in the header row.
- The first field must be the person's Email.
- The second field must be the person's ID number.
- The email group, if specified, must be called `CardholderGroup` in the header row.
- The manager's email, if specified, must be called `ManagerEmail` in the header row.
- The manager's identifier, if specified, must be called `ManagerIdentifier` in the header row.
- The manager's cardholder group, if specified, must be called `ManagerCardholderGroupName` in the header row.
- The Supporting Documents Required field, if specified, must be called `SupportingDocumentsRequired` in the header row.
- Custom field values, if specified, the name in the header row must match the name of the custom field exactly.

Create vs. Update
---

- **CREATE:** By default, a create command will be sent for every CSV record in a file.
- **UPDATE:** To update records, include the word `update` in the name the CSV file.
- Create and update requests must be in separate files.
