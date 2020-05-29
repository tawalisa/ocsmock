FROM java:8-jre

ADD ./target/ocsmock-0.0.1-SNAPSHOT.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/ocsmock-0.0.1-SNAPSHOT.jar", "/app/data/mock.json"]