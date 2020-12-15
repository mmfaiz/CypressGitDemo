import grails.util.Environment
import org.apache.commons.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CheckConfigGrailsPlugin {
    private final Logger log = LoggerFactory.getLogger('grails.plugin.config.CheckConfigPlugin')

    String version = '1.0-SNAPSHOT'

    String grailsVersion = '2.4 > *'
    def loadBefore = ['*']

    def doWithSpring = {
        if (Environment.current == Environment.TEST) {
            return
        }
        check("ELEVIO_CLIENT_SECRET")
        check("GOOGLE_SHORTENER_API_KEY")
        check("GOOGLE_MAPS_API_KEY")
    }

    private def check(String key) {
        if (StringUtils.isEmpty(System.getenv(key))) {
            throw new IllegalStateException("Missing mandatory environment variable ${key}")
        }
    }
}
