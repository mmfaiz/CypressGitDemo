class JSONFilters {

    def filters = {
        all(controller:'*', controllerExclude: "customerResource|articleResource|invoiceResource", action:'*') {
            before = {
                if (request.JSON) {
                    params << request.JSON
                }

                return true
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
    }
}
