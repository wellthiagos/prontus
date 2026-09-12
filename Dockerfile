# Etapa 1: compila e empacota a aplicação
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -ntp clean package

# Etapa 2: executa a aplicação
FROM quay.io/wildfly/wildfly:37.0.0.Final-jdk17

COPY --from=build --chown=jboss:jboss \
    /app/target/prontus.war \
    /opt/jboss/wildfly/standalone/deployments/prontus.war

EXPOSE 8080

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]