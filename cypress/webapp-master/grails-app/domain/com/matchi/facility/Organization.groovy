package com.matchi.facility

import com.matchi.Facility

/**
 * @author Michael Astreiko
 */
class Organization implements Serializable {
    String name
    String number
    String fortnoxAuthCode
    String fortnoxAccessToken
    String fortnoxCostCenter
    String fortnoxCustomerId

    static belongsTo = [facility: Facility]

    static constraints = {
        number nullable: true
        fortnoxAuthCode nullable: true
        fortnoxAccessToken nullable: true
        fortnoxCostCenter nullable: true, maxSize: 6
        fortnoxCustomerId nullable: true
    }
}
