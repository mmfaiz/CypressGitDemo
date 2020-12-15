package com.matchi.dynamicforms

import com.matchi.Facility

/**
 * Templates are created by admins anc can be assigned to Facilities via 'facilities'.
 * Facility can use template to create Real Form
 *
 * @author Michael Astreiko
 */
class FormTemplate implements Serializable {
    private static final long serialVersionUID = 12L

    String name
    String description

    Date dateCreated
    Date lastUpdated

    List templateFields

    static hasMany = [templateFields: FormField, facilities: Facility]

    static constraints = {
        description(maxSize: 20000)
    }

    static mapping = {
        facilities joinTable: [name: "facility_form_templates", key: "form_template_id"]
        cache true
    }

    static namedQueries = {
        shared { facility ->
            facilities {
                eq("id", facility.id)
            }
            order("name", "asc")
        }
    }
}
