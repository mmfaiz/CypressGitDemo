package com.matchi.fortnox.v3

/**
 * Fields description http://developer.fortnox.se/documentation/common/customers/#Fields
 *
 * @author Michael Astreiko
 */
class FortnoxCustomer implements Serializable {
    private static final long serialVersionUID = 1L;

    /** state */
    Boolean Active

    /** identifier */
    String CustomerNumber

    /** contact name (surname and lastname if person) */
    String Name
    String YourReference

    /** Customer type PRIVATE/COMPANY */
    String Type

    /** Contact address */
    String Address1
    String Address2
    String City
    String CountryCode
    String ZipCode

    /** Contact delivery address */
    String DeliveryName
    String DeliveryAddress1
    String DeliveryAddress2
    String DeliveryCity
    String DeliveryZipCode

    Map DefaultDeliveryTypes = ["Invoice":"EMAIL", "Offer":"EMAIL", "Order":"EMAIL"]

    /** Contact comment */
    String Comments

    /** Customer number */
    String OrganisationNumber

    /** Contact information */
    String Phone1
    String Phone2
    String Fax

    /** Email */
    String Email
    String EmailInvoice

    //"SEK"
    String Currency

    String getId() {
        CustomerNumber
    }

}