Summary:
---
Imports cardholders from a CSV file into [CloudCard Online Photo Submission](https://onlinephotosubmission.com).  This project is designed to be run as a scheduled job and in conjunction with a scheduled report that saves a CSV of new cardholders in a preconfigured input directory, which essentially automates importing new cardholders in CloudCard.

Tutorial Videos
---
1. [YouTube: How to Create a Service Account](https://www.youtube.com/watch?v=ZfrjFwrkwZQ)
1. [YouTube: CloudCard CSV Importer Tutorial](https://youtu.be/Pu6HXLk6jZ4)

Requirements:
---
- Java 8

To test your system, run `java -version`.  The output should look like the following.  The exact version isn't important as long as it starts with `1.8`.
> java version "1.8.0_72"<br/>
> Java(TM) SE Runtime Environment (build 1.8.0_72-b15)<br/>
> Java HotSpot(TM) 64-Bit Server VM (build 25.72-b15, mixed mode)<br/>

Usage:
---
1. Create a separate service account for CloudCard Photo Downloader to use. ([Instructions](https://www.youtube.com/watch?v=ZfrjFwrkwZQ))
1. Download [cloudcard-csv-importer.jar](https://github.com/sharptopco/cloudcard-csv-importer/raw/master/cloudcard-csv-importer.jar).
1. In a terminal window, run the command `java -jar cloudcard-csv-importer.jar {path_to_properties_file}` in the same directory as the JAR file

If not argument is specified `cloudcard-csv-importer` will look for a file called `config.properties` in the current directory.

Configuration:
---
Create a properties file similar to `src/config-template.properties`

* `input.directory`: this is the directory into which your automated report should save CSV files.
* `completed.directory`: once imported, completed CSV files are moved into this directory.
* `report.directory`: a line by line report will be generated in this directory for each input file.
* `base.url`: this is the URL for the online photo submission app.  Most likely `https://api.onlinephotosubmission.com`
* `access.token`: this is tha API key that authenticates the importer with [online photo submission](https://onlinephotosubmission.com). You can use `get-token.sh` on Mac/Linux or `get-token.ps1` on Windows to get your access token.

CSV Requirements
---
- The first field must be the person's Email.
- The second field must be the person's ID number.
- The email group, if specified, must be called `EmailGroup`.
- The Supporting Documents Required field, if specified, must be called `SupportingDocumentsRequired`.
- Custom field values, if specified, must match the name of the custom field exactly.

Create vs. Update
---
- **CREATE:** By default, a create command will be sent for every CSV record in a file. 
- **UPDATE:** To update records, include the word `update` in the name the CSV file.
- Create and update requests must be in separate files. 
