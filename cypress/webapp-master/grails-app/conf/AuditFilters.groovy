import com.matchi.Facility
import com.matchi.User
import org.apache.log4j.MDC
import org.springframework.util.StopWatch

class AuditFilters {
    // Add controller/action that should be excluded from audit logging.
    private static final List excludedUri = new ArrayList()
    static {
        excludedUri.add("facilityBooking/checkUpdate")
        excludedUri.add("scheduledTask/getCurrentlyRunningTasks")
    }

    def springSecurityService
    def securityService

    def filters = {
        all(uri: "/facility/**") {
            StopWatch stopWatch
            Long controllerMillis = 0L
            Long viewMillis = 0L
            Map data

            before = {
                if (isExcludedUri(controllerName + "/" + actionName)) {
                    return
                }

                stopWatch = new StopWatch()
                stopWatch.start()

                log.info("BEGIN: ${controllerName}/${actionName}, " +
                        "Facility=${getUserFacility()?.shortname}, " +
                        "User=${getCurrentUser()?.email}")
            }
            after = { Map model ->
                if (isExcludedUri(controllerName + "/" + actionName)) {
                    return
                }

                if (stopWatch?.isRunning()) {
                    stopWatch.stop()
                    controllerMillis = stopWatch.totalTimeMillis
                }

                data = getData(model)
                if (!stopWatch?.isRunning()) {
                    stopWatch?.start()
                }
            }
            afterView = { Exception e ->
                if (isExcludedUri(controllerName + "/" + actionName)) {
                    return
                }

                if (stopWatch?.isRunning()) {
                    stopWatch.stop()
                    viewMillis = stopWatch.totalTimeMillis
                }

                log.info("END: ${controllerName}/${actionName}, " +
                        "Facility=${getUserFacility()?.shortname}, " +
                        "User=${getCurrentUser()?.email}, " +
                        "Controller=${controllerMillis}, " +
                        "View=${viewMillis}, " +
                        "Data=${data.toMapString()}")
            }
        }
        mdc() {
            before = {
                def user = getCurrentUser()
                if (user) {
                    MDC.put("userId", user.id)
                    MDC.put("userEmail", user.email)
                }
            }
            after = {
                MDC.remove("userId")
                MDC.remove("userEmail")
            }
        }
    }

    private static boolean isExcludedUri(String uri) {
        if (excludedUri.contains(uri)) {
            return true
        }
        return false
    }

    protected User getCurrentUser() {
        return springSecurityService.getCurrentUser()
    }

    protected Facility getUserFacility() {
        securityService.getUserFacility()
    }

    /**
     * The purpose of this method is to return a map of only the model objects that
     * are of type Collection and their sizes.
     * @param model
     * @return
     */
    protected static Map getData(Map model) {
        Map data = new HashMap()

        if (model == null) {
            return data
        }

        try {
            Iterator it = model.entrySet().iterator()
            while (it.hasNext()) {
                Map.Entry kv = (Map.Entry) it.next()
                if (kv.getValue() instanceof Collection) {
                    data.put(kv.getKey(), ((Collection) kv.getValue()).size())
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        return data
    }

}
