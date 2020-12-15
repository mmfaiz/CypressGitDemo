package com.matchi.coupon

import com.matchi.Amount
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Slot
import com.matchi.User
import com.matchi.price.PriceListCustomerCategory
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.DateTime

/**
 * @author Michael Astreiko
 */
class GiftCard extends Offer {

    public final static String MAPPING_VALUE = "gift_card"

    static mapping = {
        discriminator MAPPING_VALUE
    }

    String getOfferTypeString() {
        return 'giftCard'
    }
}
