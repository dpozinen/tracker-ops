FROM gradle:jdk17 AS BUILD
WORKDIR /usr/app/
COPY . .
RUN gradle build -x test

FROM ubuntu:latest
LABEL authors="dpozinen"

ARG PROJECT_VERSION

RUN apt -y update
RUN apt-get -y install software-properties-common
RUN add-apt-repository -y ppa:tomtomtom/yt-dlp
RUN apt -y install yt-dlp
RUN apt -y install openjdk-17-jre

WORKDIR /opt/app/

COPY /src/main/resources/ytdlp.sh /usr/local/ytdlp.sh
COPY --from=BUILD "/usr/app//build/distributions/tracker-ops-$PROJECT_VERSION.tar" .

#RUN cp /build/distributions/${TAR_FILE} /opt/app

RUN tar -xvf tracker-ops-$PROJECT_VERSION.tar

WORKDIR /opt/app/tracker-ops-$PROJECT_VERSION/bin

EXPOSE 8080
EXPOSE 8081

ENTRYPOINT ["./tracker-ops"]
