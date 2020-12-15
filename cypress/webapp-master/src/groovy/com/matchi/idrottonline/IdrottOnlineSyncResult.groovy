package com.matchi.idrottonline

import com.matchi.IOSync
import org.omg.PortableInterceptor.SUCCESSFUL

class IdrottOnlineSyncResult {

    public static final String SUCCESSFUL_JSON_STATUS = "OK"

    int statusCode
    String status
    String batchId
    String message
    List<Long> customerIds = []
    List<Long> activityOccasionIds = []
}
