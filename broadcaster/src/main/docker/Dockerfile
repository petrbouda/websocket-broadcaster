FROM adoptopenjdk/openjdk11

COPY maven/target/dependency /app/modules
COPY maven/target/*.jar /app/modules

LABEL APP_VERSION="${project.version}"

CMD java ${JAVA_OPTS} \
         --module-path app/modules \
         --module broadcaster.server/Server