# cloudcard-csv-importer

Summary:
---

Imports cardholders from a CSV file into [CloudCard Online Photo Submission](https://onlinephotosubmission.com).  This project is designed to be run as a scheduled job and in conjunction with a scheduled report that saves a CSV of new cardholders in a preconfigured input directory, which essentially automates importing new cardholders in CloudCard.

Tutorial
---
<iframe width="560" height="315" src="https://www.youtube.com/embed/YjtsWjxRoq4" frameborder="0" allowfullscreen></iframe>

Requirements:
---
Java 7 or higher (preferably Java 8)

Usage:
---
Build the project into a JAR file. For convenience, a JAR file built with Java 8 has been included in the project's root directory.

    java -jar cloudcard-csv-importer.jar {path_to_properties_file}

If not argument is specified `cloudcard-csv-importer` will look for a file called `config.properties` in the current directory.

Configuration:
---
Create a properties file similar to `src/config-template.properties`

* `input.directory`: this is the directory into which your automated report should save CSV files.
* `completed.directory`: once imported, completed CSV files are moved into this directory.
* `report.directory`: a line by line report will be generated in this directory for each input file.
* `organization.id`: this is the organization's ID number in [online photo submission](https://onlinephotosubmission.com).
* `base.url`: this is the URL for the online photo submission app.  Most likely https://app.cloudcardtools.com
* `access.token`: this is tha API key that authenticates the importer with [online photo submission](https://onlinephotosubmission.com).
