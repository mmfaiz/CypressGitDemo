package com.matchi

/**
 * @author Sergei Shushkevich
 */
class FacilityContractItem implements Serializable {

    Long articleNumber
    String description
    String account
    BigDecimal price
    RecurringType type = RecurringType.MONTHLY
    Integer chargeMonth     // for YEARLY type
    Date chargeDate         // for ONE_TIME_CHARGE type
    Date dateCreated
    Date lastUpdated

    static belongsTo = [contract: FacilityContract]

    static hasMany = [chargeMonths: Integer]     // for MONTHLY type

    static constraints = {
        articleNumber nullable: true, min: 1L
        description blank: false, maxSize: 255
        account nullable: true, blank: true
        price scale: 2
        chargeMonth nullable: true, range: 1..12, validator: { val, obj ->
            val || obj.type != RecurringType.YEARLY
        }
        chargeDate nullable: true, validator: { val, obj ->
            val || obj.type != RecurringType.ONE_TIME_CHARGE
        }
        chargeMonths validator: { val, obj ->
            val || obj.type != RecurringType.MONTHLY
        }
    }

    static mapping = {
        chargeDate type: "date"
        autoTimestamp(true)
    }

    public static enum RecurringType {
        MONTHLY, YEARLY, ONE_TIME_CHARGE
    }
}
