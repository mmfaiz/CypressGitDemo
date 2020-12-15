<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message1"/></title>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="subscription.label"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityInvoiceRow.createInvoiceRow"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <g:form>
        <g:render template="/templates/wizard"
                  model="[steps: [message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.wizard.step1'), message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.wizard.step2')], current: 1]"/>
        <h1><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.confirmSubscriptions.message4"/></h1>
        <p class="lead"><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.confirmSubscriptions.message5"/></p>

        <hr>
        <div class="row">
            <div class="span3">
                <strong><g:message code="default.description.label"/></strong><br>
                ${invoiceSubscriptionCommand.text}
            </div>

            <div class="span2">
                <strong><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.confirmSubscriptions.message7"/></strong><br>
                 ${confirmInformation.size()}st
            </div>

            <div class="span2">
                <strong><g:message code="default.total.label"/></strong><br>
                <g:formatMoney value="${confirmInformation.sum(0) { it.price }}"/>
            </div>
        </div>

        <hr>

        <table class="table table-transparent">
            <thead>
                <th><g:message code="customer.label"/></th>
                <th width="100"><g:message code="default.booking.plural"/></th>
                <th width="80"><g:message code="default.discount.label"/></th>
                <th width="150"><g:message code="default.price.label"/></th>
                <th width="100">
                    <span class="right"><g:message code="default.total.label"/></span>
                </th>
            </thead>
            <g:each in="${confirmInformation}" var="confirmInfo">
                <tr>
                    <td>${confirmInfo.customerName}</td>
                    <td>${confirmInfo.numSlots}st</td>
                    <td><g:formatDiscount invoiceRow="${confirmInfo}" zeroValue="-"/></td>
                    <td>
                        <g:formatMoney value="${confirmInfo.price}"/>
                        <g:if test="${confirmInfo?.vatPercentage > 0}"><small>(${confirmInfo.vatPercentage}% moms)</small></g:if>
                        <g:else><small>(<g:message code="default.vat.excluded"/>)</small></g:else>
                    </td>
                    <td>
                        <span class="right">
                            <g:formatMoney value="${confirmInfo.total}"/>

                        </span>
                    </td>
                </tr>

            </g:each>

            <tfoot>
                <tr>
                    <td><b><g:message code="default.total.label"/></b></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td>
                        <span class="right">
                            <b><g:formatMoney value="${confirmInformation.sum(0) { it.total }}"/></b>
                        </span>
                    </td>
                </tr>
            </tfoot>
        </table>

        <div class="form-actions">
            <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
            <g:submitButton class="btn right btn-success" name="next" id="btnNext" value="${message(code: 'facilityInvoiceRow.createInvoiceRow')}" show-loader="${message(code: 'default.loader.label')}"/>
            <g:submitButton class="btn right btn-info right-margin5" name="back" value="${message(code: 'button.back.label')}" />
        </div>
    </g:form>
    <r:script>
        $(document).ready(function() {
            $("#btnNext").focus();
        });
    </r:script>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>