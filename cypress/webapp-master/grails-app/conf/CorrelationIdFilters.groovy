import org.apache.log4j.MDC

class CorrelationIdFilters {
    def filters = {
        all() {
            before = {
                def headerName = "X-Correlation-Id"
                def correlationId = request.getHeader(headerName) ?: UUID.randomUUID().toString()
                MDC.put(headerName, correlationId)
                response.setHeader(headerName, correlationId)
            }
        }
    }
}
