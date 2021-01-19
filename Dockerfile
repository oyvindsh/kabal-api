FROM navikt/java:common AS java-common
FROM redboxoss/scuttle:latest AS scuttle
FROM openjdk:11-jdk-slim
COPY --from=java-common /init-scripts /init-scripts
COPY --from=java-common /entrypoint.sh /entrypoint.sh
COPY --from=java-common /run-java.sh /run-java.sh
COPY --from=java-common /dumb-init /dumb-init
COPY --from=scuttle /scuttle /bin/scuttle
RUN apt-get update && apt-get install -y wget locales
RUN sed -i -e 's/# nb_NO.UTF-8 UTF-8/nb_NO.UTF-8 UTF-8/' /etc/locale.gen && locale-gen
ENV LC_ALL="nb_NO.UTF-8"
ENV LANG="nb_NO.UTF-8"
ENV TZ="Europe/Oslo"
ENV APP_BINARY=app
ENV APP_JAR=app.jar
ENV MAIN_CLASS="Main"
ENV CLASSPATH="/app/WEB-INF/classes:/app/WEB-INF/lib/*"
ENV ENVOY_ADMIN_API=http://127.0.0.1:15000
ENV ISTIO_QUIT_API=http://127.0.0.1:15020
WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["scuttle", "/dumb-init", "--", "/entrypoint.sh"]

COPY build/libs/*.jar ./