<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="com.matchi.FacilityProperty;" contentType="text/html;charset=UTF-8" defaultCodec="html"%>
<html>
<head>
  <title></title>
    <style type="text/css">

    body {
        font-family: "Arial", Arial, sans-serif;
        font-size: 0.9em;
    }

    @page {

        size: 210mm 297mm;
        padding: 0;
        margin: 10mm;

        @bottom-center {
            content: element(footer);
        }
        @top-center {
            content: element(header);

        }

        @bottom-right {
            padding-right:20px;
            /*  content: "Sida " counter(page) " av " counter(pages)  ; */
        }
    }

    .main {
        padding-top: 10mm;
        clear:left;
        float: left;
        width: 180mm;
    }

    .footer {
        border-top: thin solid black;
        position: running(footer);
        width: 180mm;
        height: 200mm;
    }

    div.footer table td {
        font-size: 0.9em;
    }

    .break {
        page-break-after:always;
    }

        table {
            padding: 0;
            margin: 0;
        }



</style>
<r:layoutResources/>
</head>
<body>
<g:each in="${invoices}" var="invoice" status="index">
    <g:set var="discount" value="${invoice.rows.any{it.getDiscount()}}"/>
    <g:if test="${index}"><div class="break"/></g:if>
    <div class="footer" style="margin-top: -10pt;height:80mm">
        <table width="100%">
            <tr>
                <td width="30%"><g:message code="facility.telephone.label"/>: ${facility.telephone}</td>
                <td width="40%">
                    <g:if test="${facility.plusgiro}">
                        <b><g:message code="facility.plusgiro.label"/>: ${facility.plusgiro}&nbsp;</b>
                    </g:if>
                    <g:if test="${facility.bankgiro}">
                        <b><g:message code="facility.bankgiro.label"/>: ${facility.bankgiro}</b>
                    </g:if>
                </td>

                <td width="30%">www.oslotk.no</td>
            </tr>
            <tr>
                <td width="30%"><g:message code="facility.email.label"/>: ${facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.INVOICE_EMAIL.toString())?:facility.email}</td>
                <td width="40%"><g:message code="rendering.invoice.invoice.message20"/>: ${facility.orgnr}</td>
                <td width="30%"></td>
            </tr>
        </table>
    </div>

    <table width="200mm" cellpadding="0" border="0">
        <tr>
            <td valign="top" width="100mm">
                <h1>${facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.INVOICE_FACILITY_NAME.toString())}</h1>
                <p>
                    Postbok 15, Skøyen<br/>
                    0212 Oslo<br/>
                </p>
                <g:customerInvoiceAddress customer="${invoice.customer}" checkAge="true"/>
            </td>
            <td valign="top">
                <table border="0" width="100%">
                    <tr>
                        <td>
                            <h3 style="margin-bottom: 4px;"><g:message code="rendering.invoice.invoice.message2"/></h3>
                            <table width="90mm" border="0" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td width="40mm" align="left"><strong><g:message code="customer.number.label"/></strong><br/>${invoice.customer.number}</td>
                                    <td>&nbsp;</td>
                                    <td><strong><g:message code="rendering.invoice.invoice.message4"/></strong><br/>${invoice.getOCR()}</td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td height="29mm" valign="top">
                            <table width="90mm" border="0" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td width="40mm" align="left"><strong><g:message code="invoice.invoiceDate.label"/></strong><br/><g:formatDate date="${invoice.invoiceDate.toDate()}" formatName="date.format.dateOnly"/></td>
                                    <td>&nbsp;</td>
                                    <td><strong><g:message code="invoice.expirationDate.label"/></strong><br/><g:formatDate date="${invoice.expirationDate.toDate()}" formatName="date.format.dateOnly"/></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td height="20mm">
                            <table width="90mm" border="0" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td>
                                        <p style="margin-top: 3mm">
                                            <small>${invoice.text}</small>
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                </table>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <div class="main">

                    <g:set var="colspanLong" value="${discount ? "5" : "4"}"/>
                    <g:set var="colspanShort" value="${discount ? "4" : "3"}"/>

                    <table border="0" width="100%">
                        <thead>
                        <tr>
                            <th width="60%"><g:message code="invoiceRow.description.label"/></th>
                            <th align="center"><g:message code="default.quantity.label"/></th>
                            <th align="right"><g:message code="default.price.label"/></th>
                            <g:if test="${discount}">
                                <th align="right"><g:message code="default.discount.label"/></th>
                            </g:if>
                            <th align="right"><g:message code="rendering.invoice.invoice.message10"/></th>
                        </tr>
                        <tr>
                            <th colspan="${colspanLong}"><hr size="1"/></th>
                        </tr>
                        </thead>
                        <g:render template="/templates/invoice/row" model="[invoice: invoice]"/>
                        <tfoot>
                        <tr>
                            <th colspan="${colspanLong}"><hr size="1"/></th>
                        </tr>
                        <tr>
                            <th colspan="${colspanLong}">&nbsp;</th>
                        </tr>
                        <tr>
                            <th colspan="${colspanShort}" align="right"><g:message code="rendering.invoice.invoice.message11"/></th>
                            <th align="right"><g:formatMoneyShort value="${invoice.getTotalExcludingVAT()}"/> </th>
                        </tr>
                        <g:if test="${invoice.getTotalVAT() > 0}">
                        <tr>
                            <th colspan="${colspanShort}" align="right"><g:message code="default.vat.label"/></th>
                            <th align="right"><g:formatMoneyShort value="${invoice.getTotalVAT()}"/> </th>
                        </tr>
                        </g:if>
                        <g:if test="${invoice.isRounded()}">
                        <tr>
                            <th colspan="${colspanShort}" align="right"><g:message code="rendering.invoice.invoice.message13"/></th>
                            <th align="right"><g:formatMoneyShort value="${invoice.getRoundedAmount()}"/></th>
                        </tr>
                        </g:if>
                        <tr>
                            <th colspan="${colspanShort}" align="right"><h2><g:message code="rendering.invoice.invoice.message14"/></h2></th>
                            <th align="right"><h2><g:formatMoneyShort value="${invoice.getTotalIncludingVATRounded()}"/></h2></th>
                        </tr>
                        <tr>
                            <th colspan="4">&nbsp;</th>
                        </tr>
                        </tfoot>
                    </table>
                </div>
            </td>
        </tr>
    </table>

</g:each>
</body>
</html>