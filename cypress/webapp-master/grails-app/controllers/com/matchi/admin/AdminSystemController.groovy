package com.matchi.admin

import org.quartz.Scheduler
class AdminSystemController {
    
    def mpcService
    Scheduler quartzScheduler

    def index() {
        [scheduler: quartzScheduler]
    }

    def mpc() {
        [qtStatus: mpcService.getProviderStatus(1)]
    }
}
