FROM eclipse-temurin:21.0.8_9-jdk-jammy AS builder
WORKDIR /opt/marcobrico
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY ./src ./src
RUN ./mvnw clean install -DskipITs -Dmaven.failsafe.skip=true

FROM eclipse-temurin:21.0.8_9-jre-jammy AS final
WORKDIR /opt/marcobrico
EXPOSE 8080
COPY --from=builder /opt/marcobrico/target/marcobrico-*.jar marcobrico.jar
ENTRYPOINT ["java", "-jar", "marcobrico.jar"]