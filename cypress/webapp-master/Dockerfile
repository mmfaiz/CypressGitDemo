FROM 159364473423.dkr.ecr.eu-west-1.amazonaws.com/webapp-builder as builder
ARG CI_COMMIT
ENV JAVA_OPTS="-Xmx4096m"
ARG MAVEN_REPO

RUN mkdir -p /matchi
ADD . /build
WORKDIR /build
RUN grails -Dgrails.work.dir=$PWD//target clean --non-interactive --plain-output
RUN grails -Dgrails.work.dir=$PWD//target war --non-interactive --plain-output
RUN cp target/matchi.war /build/matchi.war
RUN grails -Dgrails.work.dir=$PWD//target test-app -echoOut -echoErr
RUN unzip -q target/matchi.war -d /matchi

# -- build ends

#--- runtime
FROM 159364473423.dkr.ecr.eu-west-1.amazonaws.com/webapp-tomcat
# TZ needed until we set UTC in databases and implement TimeZone support
ENV TZ Europe/Stockholm

# Application: Copy war file
COPY --from=builder /matchi/ /usr/local/tomcat/deploy/matchi/
