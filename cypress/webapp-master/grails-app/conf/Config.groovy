import grails.util.Environment
import org.grails.plugin.hibernate.filter.HibernateFilterDomainConfiguration
import org.springframework.security.authentication.RememberMeAuthenticationToken
import groovy.json.JsonSlurper

/* ********************************************************************************

 MATCHi Configuration

 NOTE! Production secrets that does not have corresponding test/development fallback value but still are
 required in test/development for application to start and features to function properly are listed below.
 They can be passed locally as environment variables and real values are stored in 1Password vault "local-webapp-secrets".

 - ELEVIO_CLIENT_SECRET
 - GOOGLE_SHORTENER_API_KEY
 - GOOGLE_MAPS_API_KEY

Add mandatory checks to the CheckConfigGrailsPlugin in order to fail fast when mandatory env variables are missing.
********************************************************************************* */

def jsonSlurper = new JsonSlurper()

grails.config.locations = ["classpath:logging-config.groovy"]
def adyenPaymentHost = System.getenv("ADYEN_PAYMENT_HOST") ?: "pal-test.adyen.com"
def adyenDetailsHost = System.getenv("ADYEN_DETAILS_HOST") ?: "test.adyen.com"
def adyenPassword = System.getenv("ADYEN_PASSWORD") ?: "GRZF)ZLN*T^(y-cMT7L7*{C+q"
def adyenLibrary = System.getenv("ADYEN_LIBRARY_URL") ?: "https://test.adyen.com/hpp/cse/js/7614600634676473.shtml"
def adyenHmac = System.getenv("ADYEN_DETAILS_HMAC") ?: "2B7CFF0E7D85EB37C60F7AB37D438589249D05CD9A68406C99AA40D95FA1D947"
def adyenConfirmPassword = System.getenv("ADYEN_CONFIRM_PASSWORD") ?: "v(Q2HMm8C[v78/?i"

def archiveBaseUrl = System.getenv("ARCHIVE_BASEURL") ?: "https://s3-eu-west-1.amazonaws.com/assets-development/"
def assetbucket = System.getenv("ASSETS_AMAZON_S3_BUCKET") ?: "assets-development"

def boxnetTransactionsUrl = System.getenv("BOXNET_TRANSACTION_URL") ?: "http://localhost:8080/html/boxnet_trans_sample.xml"
def boxnetRequestUrl = System.getenv("BOXNET_REQUEST_URL") ?: ""
def boxnetSecret = System.getenv("BOXNET_SECRET") ?: "ba4b7c4808cef7a24a1f47cd4fd90b05"
def boxnetKey = System.getenv("BOXNET_KEY") ?: ""

def boxnetFacilities = {
    def list = System.getenv("BOXNET_FACILITIES") ?: "gltk"
    return list.split(",")
}

def elevioClientSecret = System.getenv("ELEVIO_CLIENT_SECRET") ?: ""
def excelleagueBucket = System.getenv("EXCELLEAGUE_S3_BUCKET") ?: "test-matchi-excelleague"
def excelleagueKey = System.getenv("EXCELLEAGUE_S3_KEY") ?: "excelleague.txt"

def facebookClientId = System.getenv("FACEBOOK_CLIENT_ID") ?: "230032491000454"
def facebookClientSecret = System.getenv("FACEBOOK_CLIENT_SECRET") ?: "c70baa0a7ea37a3231592ec27787c19a"

def signInWithAppleKeyId = System.getenv("APPLE_SIGN_IN_KEY_ID") ?: ""
def signInWithAppleTeamId = System.getenv("APPLE_SIGN_IN_TEAM_ID") ?: ""
def signInWithAppleAuthKey = System.getenv("APPLE_SIGN_IN_AUTH_KEY") ?: ""

def fortnoxAccessToken = System.getenv("FORTNOX_API_V3_ACCESSTOKEN") ?: ""
def fortnoxClientSecret = System.getenv("FORTNOX_API_V3_CLIENTSECRET") ?: ""
def fortnoxClientId = System.getenv("FORTNOX_API_V3_CLIENTID") ?: ""
def fortnoxOverrideAccessToken = System.getenv("FORTNOX_API_V3_OVERRIDE_ACCESSTOKEN") ?: ""
def fortnoxOverrideToken = System.getenv("FORTNOX_API_V3_OVERRIDE_TOKEN") ?: ""
def fortnoxOverrideDb = System.getenv("FORTNOX_API_V3_OVERRIDE_DB") ?: ""

def fortnoxArticleFees = System.getenv("FORTNOX_API_V3_ARTICLE_FEES") ?: "1103"
def fortnoxArticleTransactionFees = System.getenv("FORTNOX_API_V3_ARTICLE_TRANSACTION_FEES") ?: "1151"
def fortnoxArticleTransactionVariableFees = System.getenv("FORTNOX_API_V3_ARTICLE_TRANSACTION_VARIABLE_FEES") ?: "1152"
def fortnoxArticleAddonFees = System.getenv("FORTNOX_API_V3_ARTICLE_ADDON_FEES") ?: "1107"
def fortnoxArticleCouponFees = System.getenv("FORTNOX_API_V3_ARTICLE_COUPON_FEES") ?: "1153"
def fortnoxArticleCouponVariableFees = System.getenv("FORTNOX_API_V3_ARTICLE_COUPON_VARIABLE_FEES") ?: "1154"
def fortnoxArticleGiftcardFees = System.getenv("FORTNOX_API_V3_ARTICLE_GIFTCARD_FEES") ?: "1155"
def fortnoxArticleGiftcardVariableFees = System.getenv("FORTNOX_API_V3_ARTICLE_GIFTCARD_VARIABLE_FEES") ?: "1156"
def fortnoxArticleDeductionOnlineBooking = System.getenv("FORTNOX_API_V3_ARTICLE_DEDUCTION_ONLINE_BOOKING") ?: "2101"
def fortnoxArticleDeductionCoupon = System.getenv("FORTNOX_API_V3_ARTICLE_DEDUCTION_COUPON") ?: "2102"
def fortnoxArticleDeductionActivity = System.getenv("FORTNOX_API_V3_ARTICLE_DEDUCTION_ACTIVITY") ?: "2104"
def fortnoxArticleDeductionMembership = System.getenv("FORTNOX_API_V3_ARTICLE_DEDUCTION_MEMBERSHIP") ?: "2105"
def fortnoxArticleDeductionCourseSubmission = System.getenv("FORTNOX_API_V3_ARTICLE_DEDUCTION_COURSE_SUBMISSION") ?: "2106"
def fortnoxArticlePromoCodes = System.getenv("FORTNOX_API_V3_ARTICLE_PROMO_CODES") ?: "2109"

def fortnoxInvoicesModifiedMonthsAgo = Integer.parseInt(System.getenv("FORTNOX_INVOICES_MODIFIED_MONTHS_AGO") ?: "6")

def googleShortenerApiKey = System.getenv("GOOGLE_SHORTENER_API_KEY") ?: ""
def googleMapsApiKey = System.getenv("GOOGLE_MAPS_API_KEY") ?: ""

def grailsMailDisabled = Boolean.parseBoolean(System.getenv("GRAILS_MAIL_DISABLED") ?: "true")
def grailsOverrideMailAddress = Boolean.parseBoolean(System.getenv("GRAILS_MAIL_OVERRIDE_ADDRESS") ?: "true")
def grailsOverrideMailAddressRecipient = System.getenv("GRAILS_MAIL_OVERRIDE_ADDRESS_RECIPIENT") ?: "no-reply@matchi.se"
def grailsMailPassword = System.getenv("GRAILS_MAIL_PASSWORD") ?: ""

def heapApplicationId = System.getenv("HEAP_APPLICATION_ID") ?: "12465937"
def intercomApplicationId = System.getenv("INTERCOM_APPLICATION_ID") ?: "ulhfn8u8"
def intercomAppSecretKey = System.getenv("INTERCOM_SECRET_KEY") ?: "e51248399e05b6fd5ac803198a2a3a8eaa5754f3"

def ioSyncEnabled = Boolean.parseBoolean(System.getenv("IDROTT_ONLINE_SYNC_ENABLED") ?: "false")
def ioUrl = System.getenv("IDROTT_ONLINE_URL") ?: "https://testnewwebapi.knowe.net/IO.Services.WebApi/import/matchi/6274ed26-76f2-47d0-adf2-9300c31c74eb"
def ioApplicationId = System.getenv("IDROTT_ONLINE_APPLICATION_ID") ?: "182616510301091"
def ioUsername = System.getenv("IDROTT_ONLINE_USERNAME") ?: "MATCHi"
def ioPassword = System.getenv("IDROTT_ONLINE_PASSWORD") ?: "!KeefR76NkLMAnQ!45"
def ioTestFacilities = jsonSlurper.parseText(System.getenv("IDROTT_ONLINE_TEST_FACILITIES") ?: "[3]")

def kafkaEnabled = Boolean.parseBoolean(System.getenv("KAFKA_ENABLED") ?: "true")
def kafkaSecurity = Boolean.parseBoolean(System.getenv("KAFKA_SECURITY") ?: "true")
def kafkaServer = System.getenv("KAFKA_SERVER") ?: "pkc-e8mp5.eu-west-1.aws.confluent.cloud:9092"
def kafkaApiKey = System.getenv("KAFKA_API_KEY") ?: "TC5UP36QYMKD7Z2P"
def kafkaApiSecret = System.getenv("KAFKA_API_SECRET") ?: ""

def dbUrl = System.getenv("DB_URL") ?: "jdbc:mysql://localhost:3306/matchi?characterEncoding=UTF-8&useSSL=false"
def dbUser = System.getenv("DB_USER") ?: "matchi"
def dbPassword = System.getenv("DB_PASSWORD") ?: "matchi"
def dbDialect = System.getenv("DB_DIALECT") ?: "org.hibernate.dialect.MySQL5InnoDBDialect"
def dbMinConnections = Integer.parseInt(System.getenv("DB_MIN_CONNECTIONS") ?: "25")
def dbMaxConnections = Integer.parseInt(System.getenv("DB_MAX_CONNECTIONS") ?: "50")
def dbMaxIdleConnections = Integer.parseInt(System.getenv("DB_MAX_IDLE_CONNECTIONS") ?: "30")
def dbMinIdleConnections = Integer.parseInt(System.getenv("DB_MIN_IDLE_CONNECTIONS") ?: "100")

def matchiThreadingNumberOfThreadsOverride = Integer.parseInt(System.getenv("THREADING_NUMBER_OF_THREADS") ?: "10")

def matexHost = System.getenv("MATEX_HOST") ?: "https://dev-mns.matchi.se"
def matexUsername = System.getenv("MATEX_USERNAME") ?: "matchi"
def matexPassword = System.getenv("MATEX_PASSWORD") ?: "7WXyBVxYmfjrhrEciZmK2aLuLrJz"

def minimumAppVersionAndroid = System.getenv("MINIMUM_APPVERSION_ANDROID") ?: ""
def minimumAppVersionIos = System.getenv("MINIMUM_APPVERSION_IOS") ?: ""

def mpcHost = System.getenv("MPC_HOST") ?: "http://localhost:8080"
def mpcUsername = System.getenv("MPC_USERNAME") ?: ""
def mpcPassword = System.getenv("MPC_PASSWORD") ?: ""

def newrelicApplicationid = System.getenv("NEWRELIC_BROWSER_APPLICATIONID")
def newrelicLicensekey = System.getenv("NEWRELIC_BROWSER_LICENSEKEY")

def redisHost = System.getenv("REDIS_HOST") ?: "localhost"
def redisPort = Integer.parseInt(System.getenv("REDIS_PORT") ?: "6379")

def grailServerUrl = System.getenv("GRAILS_SERVER_URL") ?: "http://localhost:8080"

def defaultMatchiFacilityId = Long.parseLong(System.getenv("DEFAULT_MATCHI_FACILITY_ID") ?: "643")

def recaptchaPrivateKey = System.getenv("RECAPTCHA_PRIVATE_KEY") ?: ""

adyen {
    paymentUrl = "https://${adyenPaymentHost}/pal/servlet/Payment/v30/"
    recurringUrl = "https://${adyenPaymentHost}/pal/servlet/Recurring/v30/"
    detailsUrl = "https://${adyenDetailsHost}/hpp/directory/v2.shtml"
    skipDetailsUrl = "https://${adyenDetailsHost}/hpp/skipDetails.shtml"

    iconPath = "https://ca-test.adyen.com/ca/img/pm/"
    cardProviders = ["amex", "mc", "visa"]
    localMethods = ["ideal", "dotpay", "swish"]

    user = "ws@Company.MATCHiAB"
    merchant = "MATCHiABSE"
    skin = "YuTAOTn5"

    password = adyenPassword
    library = adyenLibrary
    hmac = adyenHmac

    confirmUser = "adyen"
    confirmPassword = adyenConfirmPassword
}

amazon.s3.bucket = assetbucket

/** Configure the archive (user uploaded content) */
archive {
    rootPath = "archive"
    baseUrl = archiveBaseUrl
}

customer.personalNumber.settings = [
        SE: [
                securityNumberLength: 4,
                orgPattern          : /^(\d{6}|\d{8})(?:-(\d{4}))?$/,
                orgFormat           : "??XXXXXX-XXXX",
                longFormat          : "yyyyMMdd",
                shortFormat         : "yyMMdd",
                readableFormat      : "yymmdd"
        ],
        NO: [
                securityNumberLength      : 5,
                orgPattern                : /^(\d{9})$/,
                orgFormat                 : "XXXXXXXXX",
                longFormat                : "ddMMyyyy",
                shortFormat               : "ddMMyy",
                readableFormat            : "ddmmyy",
                totalStringParsePattern   : /^(\d{6}|\d{8})(?:-(\d{5}))?$/,
                securityNumberParsePattern: /^([0-9]{5})$/,
        ],
        PL: [
                securityNumberLength: 0,
                orgPattern          : /^.*$/,
                orgFormat           : "XXXXXXXXXX",
                longFormat          : "ddMMyyyy",
                shortFormat         : "ddMMyy",
                readableFormat      : "yymmdd"
        ],
        DK: [
                securityNumberLength: 4,
                orgPattern          : /^(\d{6}|\d{8})(?:-(\d{4}))?$/,
                orgFormat           : "XXXX??XX-XXXX",
                longFormat          : "ddMMyyyy",
                shortFormat         : "ddMMyy",
                readableFormat      : "ddmmyy",
                skipLuhnValidation  : true
        ],
        FI: [
                securityNumberLength      : 4,
                orgPattern                : /^(\d{7})-(\d{1})$/,
                orgFormat                 : "XXXXXXX-X",
                longFormat                : "ddMMyyyy",
                shortFormat               : "ddMMyy",
                readableFormat            : "ddmmyy",
                skipLuhnValidation        : true,
                totalStringParsePattern   : /^(\d{6}|\d{8})(?:[-A]([0-9A-Y]{4}))?$/,
                securityNumberParsePattern: /([0-9A-Y]{4})/
        ],
        ES: [
                securityNumberLength: 0,
                orgPattern          : /^.*$/,
                orgFormat           : "XXXXXXXXXX",
                longFormat          : "ddMMyyyy",
                shortFormat         : "ddMMyy",
                readableFormat      : "ddmmyy"
        ]
]

boxnet {
    secret = boxnetSecret
    requestUrl = boxnetRequestUrl
    windowTitle = "Kassa"
    windowExtras = "scrollbars=no,toolbar=no,menubar=no,resizable=no,width=350,height=200,left=0,top=0"
    transactions {
        url = boxnetTransactionsUrl
        user = "matchi"
        key = boxnetKey

        facilities = boxnetFacilities()
    }
}

dataSource {
    pooled = true
    driverClassName = "com.mysql.jdbc.Driver"
    url = dbUrl
    username = dbUser
    password = dbPassword
    dialect = dbDialect

    properties {
        initialSize = dbMinConnections
        maxActive = dbMaxConnections
        minIdle = dbMinIdleConnections
        maxIdle = dbMaxIdleConnections
        maxWait = 20000
        maxAge = 10 * 60000
        numTestsPerEvictionRun = 3
        testOnBorrow = true
        testWhileIdle = true
        testOnReturn = false
        validationQuery = "SELECT 1"
        validationInterval = 15000
        minEvictableIdleTimeMillis = 60000
        timeBetweenEvictionRunsMillis = 5000

        // controls for leaked connections
        abandonWhenPercentageFull = 80
        removeAbandonedTimeout = 60
        removeAbandoned = true
        logAbandoned = true
        defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
    }

    configClass = HibernateFilterDomainConfiguration
}

environments {
    test {
        fileArchive.rootDirectory = "/tmp/matchi/archive"
        grails.plugin.springsecurity.successHandler.defaultTargetUrl = "/"
        grails.cache.redis.disabled = true
        dataSource {
            dialect = "org.hibernate.dialect.H2Dialect"
            pooled = true
            driverClassName = "org.h2.Driver"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;Mode=MySQL;DATABASE_TO_UPPER=FALSE;IGNORECASE=TRUE;COLLATION=ENGLISH STRENGTH PRIMARY"
            dbCreate = "create-drop"
        }
    }
}

exports.rootPath = "exports"

facility.customerFilter.timeout = 20    // minutes

grails {
    app.context = "/"

    cache {
        redis {
            hostName = redisHost
            port = redisPort.toInteger()
            usePool = true
        }
    }
    controllers.defaultScope = "singleton"
    converters.encoding = "UTF-8"
    databinding {
        useSpringBinder = false
        trimStrings = true
        convertEmptyStringsToNull = true
        dateFormats = ["dd.MM.yyyy HH:mm:ss",
                       "dd.MM.yyyy",
                       "dd/MM/yyyy HH:mm:ss",
                       "dd/MM/yyyy",
                       "yyyy-MM-dd HH:mm:ss",
                       "yyyy-MM-dd HH:mm:ss.S",
                       "yyyy-MM-dd'T'hh:mm:ss'Z'",
                       "yyyy-MM-dd"]
    }
    enable.native2ascii = true
    exceptionresolver.params.exclude = ['password']
    gorm.default.mapping = {
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDateMidnight, class: org.joda.time.DateMidnight
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDateTime, class: org.joda.time.DateTime
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDateTimeZoneAsString, class: org.joda.time.DateTimeZone
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDurationAsString, class: org.joda.time.Duration
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentInstantAsMillisLong, class: org.joda.time.Instant
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentInterval, class: org.joda.time.Interval
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentLocalDate, class: org.joda.time.LocalDate
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime, class: org.joda.time.LocalDateTime
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentLocalTime, class: org.joda.time.LocalTime
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentPeriodAsString, class: org.joda.time.Period
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentTimeOfDay, class: org.joda.time.TimeOfDay
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentYearMonthDay, class: org.joda.time.YearMonthDay
        "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentYears, class: org.joda.time.Years
    }
    json.legacy.builder = false
    logging.jul.usebridge = true
    mail {
        disabled = grailsMailDisabled
        host = "smtp-relay.sendinblue.com"
        port = 587
        username = "info@matchi.se"
        password = grailsMailPassword
        props = ["smtp_sasl_auth_enable"  : "yes", "smtp_sasl_password_maps": "static:info@matchi.se:${grailsMailPassword}", "smtp_sasl_security_options": "noanonymous",
                 "smtp_tls_security_level": "may", "header_size_limit": "4096000", "relayhost": "smtp-relay.sendinblue.com:587"]
    }
    mime {
        file.extensions = true // enables the parsing of file extensions from URLs into the request format
        use.accept.header = false
        types = [html         : ['text/html', 'application/xhtml+xml'],
                 xml          : ['text/xml', 'application/xml'],
                 text         : 'text/plain',
                 js           : 'text/javascript',
                 rss          : 'application/rss+xml',
                 atom         : 'application/atom+xml',
                 css          : 'text/css',
                 csv          : 'text/csv',
                 xls          : 'text/xls',
                 all          : '*/*',
                 json         : ['application/json', 'text/json'],
                 form         : 'application/x-www-form-urlencoded',
                 multipartForm: 'multipart/form-data'
        ]
    }
    plugin {
        databasemigration {
            updateOnStart = Environment.getCurrent() != Environment.TEST
            updateOnStartFileNames = ['changelog.groovy']
            ignoredObjects = ["QRTZ_BLOB_TRIGGERS", "QRTZ_CALENDARS", "QRTZ_CRON_TRIGGERS",
                              "QRTZ_FIRED_TRIGGERS", "QRTZ_JOB_DETAILS", "QRTZ_LOCKS", "QRTZ_PAUSED_TRIGGER_GRPS", "QRTZ_SCHEDULER_STATE",
                              "QRTZ_SIMPLE_TRIGGERS", "QRTZ_SIMPROP_TRIGGERS", "QRTZ_TRIGGERS"]
        }
        springsecurity {
            rememberMe {
                useSecureCookie = true
                cookieName = "matchi.remember_me"
                key = "matchi"
            }
            apf.storeLastUsername = false
            authority.className = 'com.matchi.Role'
            facebook.domain.classname = 'com.matchi.FacebookUser'
            interceptUrlMap = [
                    '/user/**'                 : ['ROLE_ADMIN', 'ROLE_USER'],
                    '/coach/**'                : ['ROLE_ADMIN', 'ROLE_USER'],
                    '/matching/**'             : ['ROLE_ADMIN', 'ROLE_USER'],
                    '/profile/**'              : ['ROLE_ADMIN', 'ROLE_USER'],
                    '/facility/**'             : ['ROLE_ADMIN', 'ROLE_USER'],
                    '/forms/memberform/**'     : ['ROLE_ADMIN', 'ROLE_USER'],
                    '/forms/protectedform/**'  : ['ROLE_ADMIN', 'ROLE_USER'],
                    '/admin/**'                : ['ROLE_ADMIN'],
                    '/facilities/membership/**': ['ROLE_USER'],
                    '/activityPayment/**'      : ['isAuthenticated()'],
                    '/couponPayment/**'        : ['isAuthenticated()'],
                    '/'                        : ['IS_AUTHENTICATED_ANONYMOUSLY'],
                    '/**'                      : ['IS_AUTHENTICATED_ANONYMOUSLY']
            ]
            onInteractiveAuthenticationSuccessEvent = { e, appCtx ->
                if (e.source instanceof RememberMeAuthenticationToken) {
                    appCtx.userService.updateLastLoggedInDate()
                }
            }
            password.algorithm = 'SHA-256'
            password.hash.iterations = 1
            portMapper.httpPort = 8080
            portMapper.httpsPort = 8443
            rejectIfNoRule = true
            sessionFixationPrevention.alwaysCreateSession = true
            securityConfigType = 'InterceptUrlMap'
            successHandler.defaultTargetUrl = '/loginSuccess/'

            userLookup {
                userDomainClassName = 'com.matchi.User'
                authorityJoinClassName = 'com.matchi.UserRole'
                usernamePropertyName = 'email'
            }

            useSecurityEventListener = true
        }
    }
    plugins {
        springsocial {
            facebook {
                clientId = facebookClientId
                clientSecret = facebookClientSecret
                apiVersion = '3.2'
                signinPermissions = 'email, user_birthday'
                signupPermissions = 'email, user_birthday, user_location, user_gender'
            }
        }
    }
    project.groupId = appName
    resources {
        adhoc.includes = [
                '/images/**', '/img/**', '/css/**', '/js/**', '/bootstrap/**', '/bootstrap3/**', '/fonts/**', '/less/**', '/plugins/**'
        ]
        debug = false
        processing.enabled = true
        uriToUrlCacheTimeout = 30000
    }
    scaffolding.templates.domainSuffix = 'Instance'
    serverURL = grailServerUrl
    spring.bean.packages = []
    views {
        javascript.library = "jquery"
        gsp {
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // we don't escape output from taglibs, leaving this to the developer
                staticparts = 'none' // escapes output from static template parts
            }
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping, faster at encode/decode and stricter
            sitemesh.preprocess = true
        }
    }
}

google {
    shortener.api.key = googleShortenerApiKey
    maps.api.key = googleMapsApiKey
}

i18n {
    cookie.selectedLanguage.maxAge = 365 * 24 * 60 * 60 // seconds (eq to 1 year)
    availableLanguages = [en: "English", sv: "Svenska", no: "Norsk", es: "Español", da: "Dansk", fi: "Suomi", de: "Deutsch"] as TreeMap
}

images.thumb.square.size = 300

jodatime.format.org.joda.time.LocalTime = "HH:mm"
jodatime.format.org.joda.time.LocalDate = "yyyy-MM-dd"
jodatime.format.org.joda.time.DateTime = "yyyy-MM-dd HH:mm:ss"

signInWithApple = [
    appleAuthUrl: "https://appleid.apple.com/auth/token",
    clientId: "com.matchiab.matchi",

    keyId: signInWithAppleKeyId,
    teamId: signInWithAppleTeamId,
    authKey: signInWithAppleAuthKey
]

// Added by the JQuery Validation plugin:
jqueryValidation {
    packed = true
    cdn = false  // false or "microsoft"
    additionalMethods = false
}
// Added by the JQuery Validation UI plugin:
jqueryValidationUi {
    errorClass = 'invalid'
    validClass = 'valid'
    onsubmit = true
    renderErrorsOnTop = false

    qTip {
        packed = true
        classes = 'ui-tooltip-red ui-tooltip-shadow ui-tooltip-rounded'
    }

    /*
    Grails constraints to JQuery Validation rules mapping for client side validation.
    Constraint not found in the ConstraintsMap will trigger remote AJAX validation.
    */
    StringConstraintsMap = [
            blank     : 'required', // inverse: blank=false, required=true
            creditCard: 'creditcard',
            email     : 'email',
            inList    : 'inList',
            minSize   : 'minlength',
            maxSize   : 'maxlength',
            size      : 'rangelength',
            matches   : 'matches',
            notEqual  : 'notEqual',
            url       : 'url',
            nullable  : 'required',
            unique    : 'unique',
            validator : 'validator'
    ]

    // Long, Integer, Short, Float, Double, BigInteger, BigDecimal
    NumberConstraintsMap = [
            min      : 'min',
            max      : 'max',
            range    : 'range',
            notEqual : 'notEqual',
            nullable : 'required',
            inList   : 'inList',
            unique   : 'unique',
            validator: 'validator'
    ]

    CollectionConstraintsMap = [
            minSize  : 'minlength',
            maxSize  : 'maxlength',
            size     : 'rangelength',
            nullable : 'required',
            validator: 'validator'
    ]

    DateConstraintsMap = [
            min      : 'minDate',
            max      : 'maxDate',
            range    : 'rangeDate',
            notEqual : 'notEqual',
            nullable : 'required',
            inList   : 'inList',
            unique   : 'unique',
            validator: 'validator'
    ]

    ObjectConstraintsMap = [
            nullable : 'required',
            validator: 'validator'
    ]

    CustomConstraintsMap = [
            phone                    : 'true', // International phone number validation
            phoneUS                  : 'true',
            alphanumeric             : 'true',
            letterswithbasicpunc     : 'true',
            lettersonly              : 'true',
            firstTimeLargerThenSecond: 'true'
    ]
}

kafka {
    enabled = (Environment.current != Environment.TEST) && kafkaEnabled
    props {
        bootstrap.servers = kafkaServer
        if (kafkaSecurity) {
            security.protocol = "SASL_SSL"
            sasl.jaas.config = "org.apache.kafka.common.security.plain.PlainLoginModule   required username='${kafkaApiKey}'   password='${kafkaApiSecret}';".toString()
            ssl.endpoint.identification.algorithm = "https"
            sasl.mechanism = "PLAIN"
        }
        client.id = "webapp"
        key.serializer = "org.apache.kafka.common.serialization.StringSerializer"
        key.deserializer = "org.apache.kafka.common.serialization.StringDeserializer"
        value.serializer = "org.apache.kafka.common.serialization.StringSerializer"
        value.deserializer = "org.apache.kafka.common.serialization.StringDeserializer"
    }
}

matchi {
    defaultFacilityId = defaultMatchiFacilityId

    booking {
        cancel.maxBatchSize = 500
        export.batchSize = 50
    }
    customer {
        "import" {
            batchSize = 50
            poolSize = 8
        }
    }
    excelleague {
        s3Bucket = excelleagueBucket
        s3Key = excelleagueKey
    }

    elevio.clientSecret = elevioClientSecret

    // Batch size when sending emails
    email.batchSize = 500

    fortnox {
        api {
            rateLimit = 3.0
            v3 {
                clientSecret = fortnoxClientSecret
                clientId = fortnoxClientId
                accessToken = fortnoxAccessToken
                override {
                    db = fortnoxOverrideDb
                    token = fortnoxOverrideToken
                    accessToken = fortnoxOverrideAccessToken
                }
                article {
                    fees = fortnoxArticleFees
                    transaction.fees = fortnoxArticleTransactionFees
                    transaction.variable.fees = fortnoxArticleTransactionVariableFees
                    addon.fees = fortnoxArticleAddonFees
                    coupon.fees = fortnoxArticleCouponFees
                    coupon.variable.fees = fortnoxArticleCouponVariableFees
                    giftcard.fees = fortnoxArticleGiftcardFees
                    giftcard.variable.fees = fortnoxArticleGiftcardVariableFees
                    promoCodes = fortnoxArticlePromoCodes
                    onlineBooking.deduction = fortnoxArticleDeductionOnlineBooking
                    coupon.deduction = fortnoxArticleDeductionCoupon
                    activity.deduction = fortnoxArticleDeductionActivity
                    membership.deduction = fortnoxArticleDeductionMembership
                    courseSubmission.deduction = fortnoxArticleDeductionCourseSubmission
                }
            }
        }
        invoices.modifiedMonthsAgo = fortnoxInvoicesModifiedMonthsAgo
    }
    heap.appId = heapApplicationId

    intercom.appId = intercomApplicationId
    intercom.appSecretKey = intercomAppSecretKey

    io {
        syncEnabled = ioSyncEnabled
        url = ioUrl
        applicationId = ioApplicationId
        username = ioUsername
        password = ioPassword
        testFacilities = ioTestFacilities
    }

    matex {
        host = matexHost
        user = matexUsername
        secret = matexPassword
    }

    mpc {
        host = mpcHost
        username = mpcUsername
        password = mpcPassword
    }

    membership {
        upcoming.purchase.daysInAdvance.monthly = 10
        upcoming.purchase.daysInAdvance.yearly = 30
        payment.failedAttemptsThreshold = 5
        update.batchSize = 50
        update.poolSize = 8
    }
    membershipRenewal {
        batchSize = 100
        poolSize = 50
    }
    pending {
        interval = 1000 // milliseconds
        timeout = 15000 // milliseconds
    }

    settings {
        facility {
            countries = [
                    "SE": "Sverige",
                    "NO": "Norge",
                    "NL": "Nederland",
                    "DE": "Deutschland",
                    "BE": "Belgique",
                    "FI": "Suomi",
                    "JP": "Japan",
                    "DK": "Danmark",
                    "CH": "Switzerland",
                    "ID": "Indonesia",
                    "TH": "Thailand",
                    "HR": "Croatia",
                    "ES": "España",
            ]
        }
        available {
            countries = ["AF", "AX", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG", "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH", "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BA", "BW", "BV", "BR", "VG", "IO", "BN", "BG", "BF", "BI", "KH", "CM", "CA", "CV", "KY", "CF", "TD", "CL", "CN", "HK", "MO", "CX", "CC", "CO", "KM", "CG", "CD", "CK", "CR", "CI", "HR", "CU", "CY", "CZ", "DK", "DJ", "DM", "DO", "EC", "EG", "SV", "GQ", "ER", "EE", "ET", "FK", "FO", "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE", "DE", "GH", "GI", "GR", "GL", "GD", "GP", "GU", "GT", "GG", "GN", "GW", "GY", "HT", "HM", "VA", "HN", "HU", "IS", "IN", "ID", "IR", "IQ", "IE", "IM", "IL", "IT", "JM", "JP", "JE", "JO", "KZ", "KE", "KI", "KP", "KR", "KW", "KG", "LA", "LV", "LB", "LS", "LR", "LY", "LI", "LT", "LU", "MK", "MG", "MW", "MY", "MV", "ML", "MT", "MH", "MQ", "MR", "MU", "YT", "MX", "FM", "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM", "NA", "NR", "NP", "NL", "AN", "NC", "NZ", "NI", "NE", "NG", "NU", "NF", "MP", "NO", "OM", "PK", "PW", "PS", "PA", "PG", "PY", "PE", "PH", "PN", "PL", "PT", "PR", "QA", "RE", "RO", "RU", "RW", "BL", "SH", "KN", "LC", "MF", "PM", "VC", "WS", "SM", "ST", "SA", "SN", "RS", "SC", "SL", "SG", "SK", "SI", "SB", "SO", "ZA", "GS", "SS", "ES", "LK", "SD", "SR", "SJ", "SZ", "SE", "CH", "SY", "TW", "TJ", "TZ", "TH", "TL", "TG", "TK", "TO", "TT", "TN", "TR", "TM", "TC", "TV", "UG", "UA", "AE", "GB", "US", "UM", "UY", "UZ", "VU", "VE", "VN", "VI", "WF", "EH", "YE", "ZM", "ZW"]
        }

        currency = [
                SEK: [
                        serviceFee   : 12.5,
                        decimalPoints: 2,
                        recordingPrice: 99
                ],
                NOK: [
                        serviceFee   : 12.5,
                        decimalPoints: 2,
                        recordingPrice: 99
                ],
                EUR: [
                        serviceFee   : 1.25,
                        decimalPoints: 2
                ],
                PLN: [
                        serviceFee   : 5,
                        decimalPoints: 2
                ],
                JPY: [
                        serviceFee   : 150,
                        decimalPoints: 0
                ],
                DKK: [
                        serviceFee   : 10,
                        decimalPoints: 2,
                        recordingPrice: 79
                ],
                CHF: [
                        serviceFee   : 1.3,
                        decimalPoints: 2
                ],
                IDR: [
                        serviceFee   : 14000,
                        decimalPoints: 0
                ],
                THB: [
                        serviceFee   : 40,
                        decimalPoints: 2
                ],
                HRK: [
                        serviceFee   : 8,
                        decimalPoints: 2
                ]
        ]
        redirect = [
                facilityControllerShow: [
                        "sthlmsportcenter": "frescatihallen"
                ]
        ]
        countryVAT = [
            SE: [6, 12, 25],
            ES: [3, 4 ,7, 10, 15, 21]
        ]
    }
    slot.create.batchSize = 25
    slot.create.poolSize = 8
    subscription.copy.batchSize = 40
    subscription.delete.batchSize = 20
    subscription.delete.poolSize = 8

    system.user.email = "worker@matchi.se"
    // Number of threads when using GParsPool
    threading.numberOfThreads = matchiThreadingNumberOfThreadsOverride
}

minimumAppVersion = [
        "android": minimumAppVersionAndroid,
        "ios"    : minimumAppVersionIos
]

newrelic {
    browser {
        applicationid = newrelicApplicationid
        licensekey = newrelicLicensekey
    }
}

sanitizer {
    //Configure our sanitizer to use AntiSamy Ebay policy file as it fits better to our needs since we use Rich HTML editor
    sanitizer.config = 'antisamyconfigs/antisamy-policy.xml'
    //By default, if there is a message given by the sanitizer during cleaning, the sanitizer codec will return an empty
    //string. Setting trustSanitizer to true will allow you to ignore the messages issued by the sanitizer and just use the
    //output.
    trustSanitizer = true
}

/** Number of days a reset password tickets expires */
ticket.resetPassword.expiresInDays = 2

if (grailsOverrideMailAddress) {
    grails.mail.overrideAddress = grailsOverrideMailAddressRecipient
}

log4j.main = {
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d{yyyy-MM-dd HH:mm:ss,SSS} [%-5p] %c{1} - %m%n')
        'null' name: 'stacktrace'
        environments {
            production {
                console name: 'stdout', layout: new net.logstash.log4j.JSONEventLayoutV1()
            }
        }
    }

    root {
        error 'stdout'
        additivity = false
    }

    environments {
        development {
            error 'grails.app'
        }
        production {
            debug 'grails.app'
        }
    }
    // Log Hibernate SQL
    if (false) {
        debug 'org.hibernate.SQL'
        trace 'org.hibernate.type.descriptor.sql'
    }

    info 'grails.app.conf'
    info 'grails.app.services.com.matchi'
    info 'grails.app.taglib.com.matchi'
    info 'grails.app.controllers.com.matchi'
    info 'grails.app.domain.com.matchi'
    info 'grails.app.jobs.com.matchi'
    info 'com.matchi'
    error 'org.apache.http'
    fatal 'org.jgroups'
}

recaptcha {
    publicKey = "6Lffe94ZAAAAADSnqkLcKFkRDH2OGPi6iUloifdW"
    privateKey = recaptchaPrivateKey ?: "empty"
    includeNoScript = !"".equalsIgnoreCase(recaptchaPrivateKey)
    includeScript = !"".equalsIgnoreCase(recaptchaPrivateKey)
    enabled = !"".equalsIgnoreCase(recaptchaPrivateKey)
}
