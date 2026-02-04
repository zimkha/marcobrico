# Platform for the base image
FROM eclipse-temurin:21-jdk-alpine AS build
# Directory workspace
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN mvn install
RUN ./mvnw clean package -DskipTests
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]


#kubectl run busybox --image=busybox --rm -it --restart=Never -- wget -0- $(kubectl get pod mypod -o jsonpath='{.status.podIP}:{.spec.containers[0].ports[0].containerPort}')
#kubectl run busybox --image=busybox --rm -it --restart=Never -- wget -O- $(kubectl get pod nginx -o jsonpath='{.status.podIP}:{.spec.containers[0].ports[0].containerPor
