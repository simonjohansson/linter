FROM openjdk:8-slim

RUN mkdir /opt/linter
COPY build/libs/linter-1.0-SNAPSHOT.jar /opt/linter
COPY lint.sh /opt/linter

ENTRYPOINT ["/opt/linter/lint.sh"]
CMD []