FROM openjdk:8-slim

RUN mkdir /opt/src
ADD . /opt/src

WORKDIR /opt/src
RUN ./gradlew clean build -xtest

RUN mkdir /opt/linter
RUN cp build/libs/linter-1.0-SNAPSHOT.jar /opt/linter
RUN cp lint.sh /opt/linter

WORKDIR /opt/linter
RUN rm -rf /opt/src

ENTRYPOINT ["/opt/linter/lint.sh"]
CMD []
