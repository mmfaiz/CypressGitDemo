package com.matchi

import com.matchi.i18n.Translatable

/**
 * @author Sergei Shushkevich
 */
class GlobalNotification implements Serializable{

    private static final long serialVersionUID = 12L

    String title
    Translatable notificationText
    Date publishDate
    Date endDate
    Boolean isForUsers
    Boolean isForFacilityAdmins

    static constraints = {
        title nullable: false, blank: false
        endDate validator: { val, obj ->
            if (obj.publishDate) {
                return val?.after(obj.publishDate)
            }
        }
        isForFacilityAdmins validator: { val, obj ->
            if (val != null && obj.isForUsers != null) {
                return !(val && obj.isForUsers)
            }
        }
    }

    static mapping = {
        endDate index: "publish_end_date_idx"
        publishDate index: "publish_end_date_idx"
        cache true
    }

    def beforeValidate() {
        if (notificationText) {
            notificationText.translations?.values()?.removeAll { !it?.trim() }
            if (!notificationText.translations) {
                notificationText.delete()
                notificationText = null
            }
        }
    }
}
