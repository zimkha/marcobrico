FROM bellsoft/liberica-openjdk-debian:25-cds AS builder
WORKDIR /builder

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} marcobrico.jar

RUN java -Djarmode=tools -jar marcobrico.jar extract --layers --destination extracted

# Runtime container
FROM bellsoft/liberica-openjdk-debian:25-cds
WORKDIR /marcobrico

COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

RUN java -XX:ArchiveClassesAtExit=marcobrico/marcobrico.jsa \
         -Dspring.context.exit=onRefresh \
         -jar marcobrico/marcobrico.jar

ENTRYPOINT ["java", "-XX:SharedArchiveFile=marcobrico.jsa", "-jar", "marcobrico.jar"]