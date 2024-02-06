[![Build, Test, and Publish Release](https://github.com/humbertodias/java-opencv-photo-db/actions/workflows/release.yml/badge.svg)](https://github.com/humbertodias/java-opencv-photo-db/actions/workflows/release.yml)

### OpenCV Photo-db

Java desktop application using OpenCV to take photos from your webcam and store on a database as a BLOB file

Requirement
* [Java 11+](https://www.oracle.com/br/java/technologies/javase/jdk11-archive-downloads.html)
* [OpenCV](https://opencv-java-tutorials.readthedocs.io/en/latest)


```mermaid
sequenceDiagram
    participant App as Java Application
    participant Webcam as Webcam
    participant Database as Database

    App->>Webcam: Start capturing image
    Webcam-->>App: Captured Image
    App->>App: Convert image to Blob
    App->>Database: Save Blob image
    Database-->>App: Image Saved
```

### Usage

```shell
./gradlew run
```
![Screenshot 2024-02-06 at 2 12 04 PM](https://github.com/humbertodias/java-opencv-photo-db/assets/9255997/e053632e-31ec-46bf-ae6d-12348dc0d894)

ESQ to exit

ENTER to save

## Db Configuration
At [src/main/resources/connection.properties](src/main/resources/connection.properties)
```properties
jdbc.driver=org.h2.Driver
jdbc.user=sa
jdbc.pass=
jdbc.url=jdbc:h2:file:~/photo-db
```
