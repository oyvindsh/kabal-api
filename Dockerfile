FROM redboxoss/scuttle:latest AS scuttle
FROM navikt/java:11

COPY --from=scuttle /scuttle /bin/scuttle
COPY build/libs/*.jar ./

ENV ENVOY_ADMIN_API=http://127.0.0.1:15000
ENV ISTIO_QUIT_API=http://127.0.0.1:15020
ENTRYPOINT ["scuttle", "/dumb-init", "--", "/entrypoint.sh"]