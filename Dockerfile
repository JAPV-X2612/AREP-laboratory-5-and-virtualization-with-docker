FROM openjdk:17-jdk-slim

WORKDIR /usrapp/bin

ENV PORT 8080

COPY target/classes ./classes
COPY target/dependency ./dependency

CMD ["java", "-cp", "./classes:./dependency/*", "edu.eci.arep.app.TcpWebServerApp"]