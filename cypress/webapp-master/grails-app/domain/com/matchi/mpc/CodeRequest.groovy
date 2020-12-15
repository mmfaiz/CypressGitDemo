package com.matchi.mpc

import com.matchi.Booking
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

class CodeRequest {

    public final static String DEFAULT_CODE = ""
    public final static String DEFAULT_MPC_ID = ""

    String  code   = DEFAULT_CODE
    Booking booking
    String  mpcId  = DEFAULT_MPC_ID
    Status  status = Status.UNVERIFIED

    Date dateCreated
    Date lastUpdated

    public static final int WARNING_TIME_MINUTES = 30
    public static final int UPDATE_FUTURE_LIMIT = 3

    static constraints = {
        code nullable: false
        booking nullable: true
        status nullable: false
        mpcId nullable: false
    }

    static enum Status {
        UNVERIFIED, VERIFIED, FAILED, PENDING

        static list() {
            return [ UNVERIFIED, VERIFIED, FAILED, PENDING ]
        }
    }

    boolean hasProblems() {
        if(this.status == Status.PENDING) return false

        Date warningTimeDate = new DateTime().minusMinutes(WARNING_TIME_MINUTES).toDate()
        Date upperWarningLimit = getUpdateFutureLimit()
        Date startTime = booking?.slot?.startTime

        return this.status == Status.UNVERIFIED && dateCreated.before(warningTimeDate) && startTime?.before(upperWarningLimit)
    }

    static Date getUpdateFutureLimit() {
        return new LocalDateTime().plusDays(CodeRequest.UPDATE_FUTURE_LIMIT).toDate()
    }
}
