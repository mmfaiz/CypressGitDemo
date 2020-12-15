import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
eventTestSuiteEnd = { type ->
    println("Failed tests")
    new File(testReportsDir, "plain").eachFileMatch(~/.*[(Spec)|(Test)]\.txt/) { file ->
        file.withReader("UTF-8") {reader ->
            reader.readLine()
            def line = reader.readLine()
            (line =~ /.*, Failures: (\d), Errors: (\d), .*/).each {m0, m1, m2 ->
                if (m1 == "0" && m2 == "0") return
                println "== ${file.name} =============================================="
                println line
                while ((line = reader.readLine()) != null) println line
            }
        }
    }}
eventCreateWarStart = { String warname, File stagingDir ->
    File file = new File(stagingDir, "WEB-INF/classes/application.properties")
    def props = new Properties()
    props.load(new FileInputStream(file))
    props.setProperty("app.version", version())
    def os = new FileOutputStream(file)
    props.store(os, "Grails Metadata file")
    os.close()
}

eventCreateWarStart = { String warname, File stagingDir ->
    def webXML = new java.io.File("${stagingDir}/WEB-INF/web.xml")
    webXML.text = webXML.text.replaceFirst("<session-timeout>30</session-timeout>", "")
}

def version() {
    "${getDate()}.${getRevision()}"
}

def getDate() {
    LocalDate.now().format(new DateTimeFormatterBuilder().appendPattern("yyMMdd").toFormatter())
}

def getRevision() {
    def commit = System.getenv("CI_COMMIT")
    if (commit && commit.length() > 6) {
        println("Using CI_COMMIT variable: ${commit}")
        commit.substring(0, 7)
    } else {
        println("Failed to get CI_COMMIT variable")
        ""
    }
}

// TODO: fixes issue with joda-time types specific queries in unit tests; remove it after joda-time plugin upgrade
eventTestPhaseStart = { phase ->
    if (phase == "unit") {
        event "StatusUpdate", ["configuring joda-time support for simple datastore"]
        def marshallerClass = classLoader.loadClass("grails.plugin.jodatime.simpledatastore.SimpleMapJodaTimeMarshaller")
        marshallerClass.initialize()
    }
}
