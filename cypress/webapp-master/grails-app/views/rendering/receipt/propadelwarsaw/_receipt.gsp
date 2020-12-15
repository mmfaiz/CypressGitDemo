<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="com.matchi.FacilityProperty; com.matchi.HtmlUtil" contentType="text/html;charset=UTF-8" %>
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

    /*


    #main {
        padding-top: 10mm;
        clear:left;
        float: left;
        width: 180mm;
    }

    */

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
<g:set var="facility" value="${order?.facility}"/>
<g:set var="currency" value="${facility?.currency}" />

<g:if test="${index}"><div class="break"/></g:if>

<div class="footer" style="margin-top: -20pt;height:80mm">
    <table width="100%">
        <tr>
            <td width="40%"><g:message code="facility.telephone.label"/>: ${facility?.telephone}</td>
        </tr>
        <tr>
            <td width="40%"><g:message code="facility.email.label"/>: ${facility?.email}</td>
            <td width="40%" align="right"><g:message code="rendering.invoice.invoice.message20"/>: ${facility?.orgnr}</td>
        </tr>
    </table>
</div>

<table width="200mm" cellpadding="0" border="0">
    <tr>
        <td valign="top" width="50%">

            <h1 style="margin-bottom: 0;"><g:message code="default.receipt.label"/></h1>
            <small><g:message code="default.receipt.number.label"/>: ${order.getReceiptNumber()}</small>
            <h3 style="margin-bottom: 4px;"><g:message code="default.debit.date.label"/></h3>
            <g:formatDate date="${order.dateCreated}" formatName="date.format.dateOnly"/>

            <br/><br/>
            <strong>PADEL POLAND SP. Z O.O.</strong><br/>
            02-781 Warszawa<br/>
            CZAPLI 57<br/>
            NIP: 9512428088<br/>
            VAT nr: PL9512428088<br/>
            Organisation Number: 0000660665<br/>

            <g:if test="${order.customer.isCompany()}">
                <br/>
                <strong><g:message code="default.recipient.label"/></strong><br/>
                ${order.customer?.fullName()?.encodeAsHTML()}<br/>
                ${order.customer?.address1}<br/>
                ${order.customer?.zipcode} ${order.customer?.city}<br/>
                <g:if test="${order.customer?.orgNumber}"><g:message code="organization.number.label" />: ${order.customer?.orgNumber}<br/></g:if>
                <g:if test="${order.customer?.vatNumber}"><g:message code="facilityCustomer.form.vatnumber" />: ${order.customer?.vatNumber}<br/></g:if>
            </g:if>
            <g:else>
                <br/>
                <strong><g:message code="default.recipient.label"/></strong><br/>
                ${order?.customer?.fullName()}${order?.customer?.getPersonalNumber() ? ", " + order?.customer?.getPersonalNumber() : ""}<br/>
                ${order?.customer?.address1}<br/>
                ${order?.customer?.address2}<br/>
                ${order?.customer?.zipcode} ${order?.customer?.city}<br/>
            </g:else>
        </td>
        <td valign="top" width="50%">
            <table border="0">
                <tr>
                    <td valign="top" align="center">
                        <g:fileArchiveFacilityLogoImage file="${facility.facilityLogotypeImage}" alt="${facility.name.encodeAsHTML()}" height="150" class="img-responsive"/>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div class="main">
                <table border="0" width="100%">
                    <thead>
                    <tr>
                        <th width="60%"><g:message code="invoiceRow.description.label"/></th>
                        <th align="center"><g:message code="default.quantity.label"/></th>
                        <th align="right"><g:message code="default.price.label"/></th>
                    </tr>
                    <tr>
                        <th colspan="4"><hr size="1"/></th>
                    </tr>
                    </thead>

                    <g:each in="${order.payments}" var="payment">
                        <tr>
                            <td>
                                ${HtmlUtil.escapeAmpersands(order.description)}
                                <br/>
                                <small><g:message code="adminStatistics.index.paymentMethod"/>: <strong>${message(code:"payment.method.${payment.method}")}</strong></small>
                                <g:if test="${activity}">
                                    <small><g:message code="default.activity.label"/>: <strong>${activity}</strong></small>
                                </g:if>
                            </td>
                            <td align="center">${1}</td>
                            <td align="right"><g:formatMoneyShort value="${payment.total()}"/> ${currency}</td>
                        </tr>
                    </g:each>

                    <tfoot>
                    <tr>
                        <th colspan="4"><hr size="1"/></th>
                    </tr>
                    <tr>
                        <th colspan="4">&nbsp;</th>
                    </tr>
                    <tr>

                        <th align="right"><g:message code="rendering.invoice.invoice.message10"/></th>
                        <th colspan="3" align="right"><g:formatMoneyShort value="${order.getTotalAmountPaid()}"/> ${currency} (<g:message code="default.varav.label"/> ${order.payments.sum { it.vat }} ${currency} <g:message code="default.vat.label"/>)</th>
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
</body>
</html>