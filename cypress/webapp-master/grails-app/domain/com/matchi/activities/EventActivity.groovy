package com.matchi.activities

import com.matchi.dynamicforms.Form

/**
 * @author Sergei Shushkevich
 */
class EventActivity extends Activity {

    private static final long serialVersionUID = 12L

    public static final String DISCRIMINATOR = "event_activity"

    Date startDate
    Date endDate
    Form form
    Boolean showOnline

    static constraints = {
        endDate validator: { val, obj ->
            return val && !val.before(obj.startDate) ?: 'course.endDate.validation.error'
        }
        form nullable: false    // it's only on domain level, in DB it should remain nullable
        showOnline nullable: true
    }

    static mapping = {
        discriminator DISCRIMINATOR
    }

    static namedQueries = {
        facilityActiveEvents { f ->
            eq("facility", f)
            ge("endDate", new Date().clearTime())
        }
        archivedEvents { f ->
            eq("facility", f)
            lt("endDate", new Date().clearTime())
        }
    }

    @Override
    String[] getToMails() {
        return [facility?.email]
    }
}