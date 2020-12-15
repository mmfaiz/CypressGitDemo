package com.matchi.fortnox.v3

import org.apache.commons.lang.builder.CompareToBuilder

import java.text.ParseException

/**
 * Fields description http://developer.fortnox.se/documentation/invoicing/invoices/#Fields
 *
 * @author Michael Astreiko
 */
class FortnoxInvoiceRow implements Comparable, Serializable {
    private static final long serialVersionUID = 1L;
//4 digits
    Integer AccountNumber
    String ArticleNumber
    Float ContributionPercent
    Float ContributionValue
    String CostCenter
    String DeliveredQuantity
    String Description
    Float Discount
    //AMOUNT / PERCENT
    String DiscountType = "AMOUNT"
    Boolean HouseWork
    Float Price
    Float Total
    String Unit
    Integer VAT

    String toString() {
        return "AccountNumber: ${AccountNumber}, " +
                "ArticleNumber: ${ArticleNumber}, " +
                "DeliveredQuantity: ${DeliveredQuantity}, " +
                "Description: ${Description}, " +
                "Discount: ${Discount}, " +
                "DiscountType: ${DiscountType}, " +
                "Price: ${Price}, " +
                "Total: ${Total}, " +
                "Unit: ${Unit}, " +
                "VAT: ${VAT}"

    }

    @Override
    int compareTo(Object obj) {
        FortnoxInvoiceRow row = (FortnoxInvoiceRow) obj

        Date thisDate, objDate

        try {
            thisDate = Date.parse('d/M', getDatePartToken(this.Description))
            objDate  = Date.parse('d/M', getDatePartToken(row.Description))
        } catch (ParseException e) {}

        CompareToBuilder compare = new CompareToBuilder()

        if (thisDate && objDate) {
            compare.append(thisDate, objDate)
        }

        return compare
                .append(this.Description, row.Description)
                .toComparison()
    }

    private static String getDatePartToken(String input) {
        if(input) {
            List<String> tokens = input.tokenize()
            if(tokens.size() > 1){
                return tokens[1]
            }
        }
        return ""
    }
}
