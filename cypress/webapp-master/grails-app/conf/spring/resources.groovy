import com.matchi.BankGiroUtil
import com.matchi.MatchiTokenBasedRememberMeServices
import com.matchi.UserLocaleResolver
import com.matchi.i18n.LocaleChangeInterceptor
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.web.servlet.i18n.CookieLocaleResolver

beans = {
    xmlns aop: "http://www.springframework.org/schema/aop"
    springConfig.addAlias( 'redisConnectionFactory', 'grailsCacheJedisConnectionFactory')

	dateUtil(com.matchi.DateUtil) {
        messageSource = ref("messageSource")
        grailsApplication = ref('grailsApplication')
    }

    bankGiroUtil(BankGiroUtil) {
    }

    def conf = SpringSecurityUtils.securityConfig

    rememberMeServices(MatchiTokenBasedRememberMeServices, conf.rememberMe.key, ref('userDetailsService')) {
        cookieName = conf.rememberMe.cookieName
        alwaysRemember = conf.rememberMe.alwaysRemember
        tokenValiditySeconds = conf.rememberMe.tokenValiditySeconds
        parameter = conf.rememberMe.parameter
        if (conf.rememberMe.useSecureCookie instanceof Boolean) {
            useSecureCookie = conf.rememberMe.useSecureCookie // null
        }
        authenticationDetailsSource = ref('authenticationDetailsSource')
        userDetailsChecker = ref('userDetailsChecker')
        authoritiesMapper = ref('authoritiesMapper')
    }


    groovySql(groovy.sql.Sql, ref('dataSource'))

    excelImportManager(com.matchi.excel.ExcelImportManager) {
    }
    excelExportManager(com.matchi.excel.ExcelExportManager) {
        messageSource = ref("messageSource")
        dateUtil = ref("dateUtil")
    }

    /**
     * ExportHandlers
     */
    customerExportHandler(com.matchi.export.CustomerExportHandler) {
        excelExportManager = ref('excelExportManager')
    }
    membershipExportHandler(com.matchi.export.MembershipExportHandler) {
        excelExportManager = ref('excelExportManager')
    }

    boxnetManager(com.matchi.boxnet.BoxnetManager) {
        grailsApplication = ref('grailsApplication')
        grailsLinkGenerator = ref('grailsLinkGenerator')
    }

    bookingTermsAndConditionsTagLib(com.matchi.BookingTermsAndConditionsTagLib) {

    }

    cookieLocaleResolver(CookieLocaleResolver) {
        cookieMaxAge = grailsApplication.config.i18n.cookie.selectedLanguage.maxAge
    }

    localeResolver(UserLocaleResolver) {
        cookieLocaleResolver = ref("cookieLocaleResolver")
        springSecurityService = ref("springSecurityService")
        defaultLocale = new Locale("en")
        availableLanguages = grailsApplication.config.i18n.availableLanguages.keySet()
    }

    localeChangeInterceptor(LocaleChangeInterceptor) {
        availableLanguages = grailsApplication.config.i18n.availableLanguages.keySet()
        paramName = "lang"
        localeResolver = ref("localeResolver")
    }

    /**
     * JSON Marshallers
     */
    matchiActivityOccasionsMarshaller(com.matchi.marshallers.ActivityOccasionMarshaller) {
        activityService = ref("activityService")
    }
    matchiClassActivityMarshaller(com.matchi.marshallers.ClassActivityMarshaller)
    matchiAvailabilityMarshaller(com.matchi.marshallers.AvailabilityMarshaller)
    matchiBookingMarshaller(com.matchi.marshallers.BookingMarshaller) {
        paymentService = ref("paymentService")
        slotService = ref("slotService")
        grailsApplication = ref("grailsApplication")
    }
    matchiClassActivityWatchMarshaller(com.matchi.marshallers.ClassActivityWatchMarshaller)
    matchiCourtMarshaller(com.matchi.marshallers.CourtMarshaller)
    matchiCameraMarshaller(com.matchi.marshallers.CameraMarshaller)
    matchiCustomerMarshaller(com.matchi.marshallers.CustomerMarshaller)
    matchiExceptionMarshaller(com.matchi.marshallers.ExceptionMarshaller)
    matchiFacilityMarshaller(com.matchi.marshallers.FacilityMarshaller) {
        grailsLinkGenerator = ref('grailsLinkGenerator')
    }
    matchiInvoiceMarshaller(com.matchi.marshallers.InvoiceMarshaller) {
        invoiceService = ref("invoiceService")
    }
    matchiMunicipalityMarshaller(com.matchi.marshallers.MunicipalityMarshaller)
    matchiOrderMarshaller(com.matchi.marshallers.OrderMarshaller)
    matchiPaymentMarshaller(com.matchi.marshallers.PaymentMarshaller)
    matchiRegionMarshaller(com.matchi.marshallers.RegionMarshaller)
    matchiSlotMarshaller(com.matchi.marshallers.SlotMarshaller)
    matchiSlotWatchMarshaller(com.matchi.marshallers.SlotWatchMarshaller) {
        messageSource = ref("messageSource")
    }
    matchiSportMarshaller(com.matchi.marshallers.SportMarshaller)
    matchiUserFavoriteMarshaller(com.matchi.marshallers.UserFavoriteMarshaller)
    matchiUserMarshaller(com.matchi.marshallers.UserMarshaller)

    IReservationsMarshaller(com.matchi.marshallers.IReservationsMarshaller) {
        activityService = ref("activityService")
    }

    aop {
        config("proxy-target-class":true) {
            pointcut(id:"interceptorPointcut", expression:"within(grails.plugin.asyncmail..* )")
            advisor( 'pointcut-ref': "interceptorPointcut", 'advice-ref':"asyncMailInterceptorAdvice")
        }
    }

    asyncMailInterceptorAdvice(org.springframework.aop.interceptor.PerformanceMonitorInterceptor, true) {
        loggerName = "com.matchi.asyncmail.LogTrace"
    }
    // END : Trace of async emails

    /**
     * Asynchronous Integration Platform (AIP) JSON Marshallers
     */
    integrationMarshaller(com.matchi.marshallers.integration.AIPMarshaller)
}
