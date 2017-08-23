FROM openjdk:8

ADD . /code

WORKDIR /code/src
RUN javac com/onlinephotosubmission/csv_importer/Main.java
CMD java com/onlinephotosubmission/csv_importer/Main && ls ../output