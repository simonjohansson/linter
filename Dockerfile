FROM openjdk:8-slim

RUN mkdir /opt/linter
WORKDIR /opt/linter

ADD lint.sh /opt/linter
ADD build/libs/linter-1.0-SNAPSHOT.jar /opt/linter

ENTRYPOINT ["/opt/linter/lint.sh"]
CMD []
