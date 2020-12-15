import grails.util.Environment

jdbcProps = {
    scheduler.instanceName = "quartz_matchi"
    scheduler.instanceId = "AUTO"

    threadPool.class = "org.quartz.simpl.SimpleThreadPool"
    threadPool.threadCount = 3
    threadPool.threadPriority = 5

    jobStore.misfireThreshold = 60000

    jobStore.class = "org.quartz.impl.jdbcjobstore.JobStoreTX"
    jobStore.driverDelegateClass = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate"

    jobStore.useProperties = false
    jobStore.tablePrefix = "QRTZ_"
    jobStore.isClustered = true
    jobStore.clusterCheckinInterval = 5000

    plugin.shutdownhook.class = "org.quartz.plugins.management.ShutdownHookPlugin"
    plugin.shutdownhook.cleanShutdown = true
}

quartz {
    waitForJobsToCompleteOnShutdown = true
    exposeSchedulerInRepository = false
    autoStartup = Boolean.parseBoolean(System.getenv("BATCH_JOBS_ENABLED") ?: "true") && Environment.getCurrent() != Environment.TEST
    jdbcStore = Environment.getCurrent() != Environment.TEST
    if (Environment.getCurrent() != Environment.TEST) {
        props(jdbcProps)
    }
}
