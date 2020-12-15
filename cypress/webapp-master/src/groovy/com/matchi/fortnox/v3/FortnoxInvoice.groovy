package com.matchi.fortnox.v3


import org.joda.time.DateTime
/**
 * Fields description http://developer.fortnox.se/documentation/invoicing/invoices/#Fields
 *
 * For now EmailInformation and EDIInformation
 *
 * @author Michael Astreiko
 */
class FortnoxInvoice implements Serializable {
    private static final long serialVersionUID = 1L;
    //Identifier
    String DocumentNumber

    //INVOICE, CREDIT, INTEREST, CASHINVOICE
    String InvoiceType
    //SV/EN
    String Language

    Date InvoiceDate
    Date DueDate
    Date DeliveryDate
    Date LastRemindDate

    String Currency
    String CurrencyRate
    Float CurrencyUnit

    Float AdministrationFee
    Float AdministrationFeeVAT

    String CustomerName
    String CustomerNumber
    String OrganisationNumber

    String Address1
    String Address2
    String City
    String Country
    String ZipCode
    String Phone1
    String Phone2

    Boolean Sent
    Boolean Booked
    Boolean Cancelled
    Boolean Credit
    Boolean NotCompleted
    Boolean HouseWork

    String Comments

    Float ContributionPercent
    Float ContributionValue

    String CostCenter

    String CreditInvoiceReference

    //Delivery
    String DeliveryAddress1
    String DeliveryAddress2
    String DeliveryCity
    String DeliveryCountry
    String DeliveryName
    String DeliveryZipCode

    String ExternalInvoiceReference1
    String ExternalInvoiceReference2

    Integer Balance
    Float Gross
    Float Net
    Float Freight
    Float FreightVAT
    Float BasisTaxReduction
    Float Total
    Float TotalToPay
    Float TotalVAT
    Boolean VATIncluded

    String OCR

    String InvoiceReference
    Integer OfferReference
    Integer OrderReference
    String YourReference
    String OurReference

    String PriceList
    String PrintTemplate
    String Remarks
    Integer Reminders
    Float RoundOff

    Integer TaxReduction
    String TermsOfDelivery
    String TermsOfPayment

    Integer VoucherNumber
    String VoucherSeries
    Integer VoucherYear
    String WayOfDelivery
    String YourOrderNumber
    //Fields that not documented
    String EUQuarterlyReport

    List<FortnoxInvoiceRow> InvoiceRows = new ArrayList<FortnoxInvoiceRow>()

    enum Filters {
        UNBOOKED('unbooked'),
        UNPAID_OVERDUE('unpaidoverdue'),
        UNPAID('unpaid'),
        CANCELLED('cancelled'),
        FULLY_PAID('fullypaid')

        String filterId

        Filters(String filterId) {
            this.filterId = filterId
        }
    }

    String getId() {
        DocumentNumber
    }

    BigDecimal getAmountPaid() {
        new BigDecimal(Total - Balance)
    }

    /**
     *
     * @param invoiceDate
     * @param total
     * @return
     */
    static Date createInternalInvoiceDueDate(DateTime invoiceDate, def total) {
        return total < 0 ? invoiceDate.plusMonths(1).withDayOfMonth(15).toDate() : invoiceDate.plusMonths(1).withDayOfMonth(15).plusDays(30).toDate()
    }
}
