grails.servlet.version = "3.0"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.war.file = "target/${appName}.war"
grails.project.target.level = 1.6
grails.project.source.level = 1.6

forkConfig = [maxMemory: 4096, minMemory: 2048, debug: false, maxPerm: 2048]
grails.project.fork = [
        test   : forkConfig, // configure settings for the test-app JVM
        run    : forkConfig, // configure settings for the run-app JVM
        war    : forkConfig, // configure settings for the run-war JVM
        console: forkConfig // configure settings for the Swing console JVM
]
grails.plugin.location.'cache-redis' = "./plugins/cache-redis"
grails.plugin.location.'check-config' = "./plugins/check-config"

grails.project.dependency.resolver = "ivy"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        excludes 'ehcache'
    }
    log "warn"
    checksums true
    legacyResolve false
    repositories {
        mavenLocal()
        mavenRepo name: "matchi", root: System.getenv("MAVEN_REPO") ?: "https://artifactory.matchi.se/artifactory/local"
    }
    dependencies {
        compile "org.jadira.usertype:usertype.jodatime:1.9",
                "com.fasterxml.jackson.core:jackson-core:2.4.5",
                "com.fasterxml.jackson.core:jackson-databind:2.4.5",
                "com.fasterxml.jackson.core:jackson-annotations:2.4.5",
                "org.springframework.security:spring-security-crypto:3.1.4.RELEASE",
                "mysql:mysql-connector-java:5.1.47",
                ":bankgiro-api:1.3.2",
                ":json-lib:2.4",
                "net.coobird:thumbnailator:0.4.8",
                "com.googlecode.libphonenumber:libphonenumber:7.0.5",
                "org.codehaus.gpars:gpars:1.2.1",
                "com.google.guava:guava:20.0",
                "cglib:cglib:2.2.2",
                "junit:junit:4.7",
                "org.hamcrest:hamcrest-library:1.3",
                "asm:asm:3.3.1"

        compile "net.sf.ehcache:ehcache-core:2.6.6"
        compile "net.logstash.log4j:jsonevent-layout:1.7"

        compile "org.jgroups:jgroups:3.6.17.Final"
        compile("io.undertow:undertow-servlet:1.3.15.Final") {
            excludes "jboss-servlet-api_3.1_spec"
        }
        compile("net.sf.ehcache:ehcache-jgroupsreplication:1.7") {
            excludes "jgroups"
            excludes "ehcache-core"
        }

        compile("org.jgroups.kubernetes:kubernetes:0.9.3") {
            excludes "jgroups"
        }

        compile("org.springframework.social:spring-social-core:1.1.0.RELEASE",
                "org.springframework.social:spring-social-web:1.1.0.RELEASE",
                "org.springframework.social:spring-social-facebook:2.0.3.RELEASE") {
            exclude "spring-webmvc"
            exclude "spring-web"
            exclude "spring-social-config"
        }

        compile("commons-validator:commons-validator:1.5.1") {
            exclude "xml-apis"
            exclude "commons-digester"
            exclude "commons-logging"
        }

        compile group: 'org.apache.kafka', name: 'kafka-clients', version: '2.6.0'

        // used by fortnox plugin
        runtime('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1') {
            excludes 'groovy'
            excludes "xercesImpl"
        }

        compile "joda-time:joda-time:2.9"

        // Workaround to resolve dependency issue with aws-java-sdk and http-builder (dependent on httpcore:4.0)
        build 'org.apache.httpcomponents:httpcore:4.3.1'
        build 'org.apache.httpcomponents:httpclient:4.3.1'
        runtime 'org.apache.httpcomponents:httpcore:4.3.1'
        runtime 'org.apache.httpcomponents:httpclient:4.3.1'
        runtime "org.codehaus.jsr166-mirror:jsr166y:1.7.0"

        runtime "org.springframework:spring-test:4.0.9.RELEASE"
        runtime 'org.springframework:spring-aop:4.0.9.RELEASE'

        test "org.grails:grails-datastore-test-support:1.0-grails-2.4"
        test "org.hamcrest:hamcrest:2.2"
        test "org.hamcrest:hamcrest-library:2.2"

        compile 'redis.clients:jedis:2.7.3'
        compile('org.springframework.data:spring-data-redis:1.6.2.RELEASE') {
            exclude group: 'org.springframework', name: 'spring-aop'
            exclude group: 'org.springframework', name: 'spring-context-support'
            exclude group: 'org.springframework', name: 'spring-context'
        }
        compile 'org.springframework:spring-expression:4.0.9.RELEASE'

        compile group: 'org.bouncycastle', name: 'bcpkix-jdk15on', version: '1.65'

        compile group: 'io.jsonwebtoken', name: 'jjwt-api', version: '0.10.7'
        runtime group: 'io.jsonwebtoken', name: 'jjwt-impl', version: '0.10.7'
        runtime group: 'io.jsonwebtoken', name: 'jjwt-jackson', version: '0.10.7'

        compile "com.mashape.unirest:unirest-java:1.4.9"
        compile "org.apache.httpcomponents:httpclient:4.3.6"
        compile "org.apache.httpcomponents:httpasyncclient:4.0.2"
        compile "org.apache.httpcomponents:httpmime:4.3.6"
        compile "org.json:json:20140107"

        // https://stackoverflow.com/questions/32308123/non-existent-mapping-property-none-specified-for-property-while-running-test-c
        compile 'org.grails:grails-datastore-core:3.1.5.RELEASE'

    }
    plugins {
        compile ":cache:1.1.8"

        runtime(":jquery-validation-ui:1.4.9") {
            excludes "json-lib"
        }
        runtime ":constraints:0.6.0" // specifies a fixed searchable version
        compile ":cache-headers:1.1.7"
        runtime ":zipped-resources:1.0"
        runtime ":gsp-resources:0.4.4"
        compile ":lesscss-resources:1.3.3"
        runtime ":cached-resources:1.0"
        runtime ":database-migration:1.4.1"
        compile "org.grails.plugins:hibernate:3.6.10.19"

        compile ":spring-security-core:2.0.0"
        compile ":quartz:1.0.2"
        compile(":joda-time:1.5")

        compile ":jquery-ui:1.10.4"
        runtime ":jquery:1.11.1"
        compile ":rest-client-builder:2.1.1"
        compile "org.grails.plugins:tinyurl:0.1"

        compile "org.grails.plugins:sanitizer:0.12.0"

        compile ":mail:1.0.1"
        compile(":asynchronous-mail:1.2") {
            excludes "quartz"
        }
        compile ":i18n-asset-pipeline:1.0.6"
        compile ":cascade-validation:0.1.4"
        compile ":webflow:2.1.0"
        compile ":jxl:0.54"
        compile (":rendering:1.0.0") {
            excludes "bcprov-jdk14"
        }
        compile ":csv:0.3.1"
        compile(":aws-sdk:1.9.0") {
            excludes "joda-time"
        }
        compile ":ic-alendar:0.4.6"
        compile ":scaffolding:2.1.2"

        runtime ":resources:1.2.14"

        build ":tomcat:7.0.54"

        test ":plastic-criteria:1.6.7"

        compile "org.grails.plugins:hibernate-filter:0.3.2"

        compile ':recaptcha:1.7.0'
    }
}
