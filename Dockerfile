# Etapa 1: compila e empacota a aplicação
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY docker/h2/init.sql ./docker/h2/init.sql

RUN mvn -B -ntp clean package

RUN mvn -B -ntp org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy \
    -Dartifact=com.oracle.database.jdbc:ojdbc11:23.26.3.0.0 \
    -DoutputDirectory=/app/jdbc

# Etapa 2: configura o servidor e executa a aplicação
FROM quay.io/wildfly/wildfly:37.0.0.Final-jdk17

USER root

RUN mkdir -p /opt/jboss/dados /opt/jboss/scripts \
    && chown -R jboss:jboss /opt/jboss/dados /opt/jboss/scripts

USER jboss

COPY --chown=jboss:jboss docker/h2/init.sql \
    /opt/jboss/scripts/init.sql

COPY --chown=jboss:jboss docker/wildfly/configurar.cli \
    /opt/jboss/scripts/configurar.cli

COPY --from=build --chown=jboss:jboss \
    /app/jdbc/ojdbc11-23.26.3.0.0.jar \
    /opt/jboss/scripts/ojdbc11.jar

RUN /opt/jboss/wildfly/bin/jboss-cli.sh \
    --file=/opt/jboss/scripts/configurar.cli \
    && rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history

COPY --from=build --chown=jboss:jboss \
    /app/target/prontus.war \
    /opt/jboss/wildfly/standalone/deployments/prontus.war

EXPOSE 8080

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]