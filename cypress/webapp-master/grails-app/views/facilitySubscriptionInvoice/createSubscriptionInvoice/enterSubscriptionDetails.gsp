<%@ page import="com.matchi.invoice.InvoiceRow; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message1"/></title>
    <r:require modules="matchi-customerselect" />
</head>
<body>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="subscription.label"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityInvoiceRow.createInvoiceRow"/></li>
</ul>


<g:if test="${!facility?.isMasterFacility()}">
    <g:form class="form-inline">
        <g:render template="/templates/wizard"
                  model="[steps: [message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.wizard.step1'), message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.wizard.step2')], current: 0]"/>

        <g:hasErrors bean="${rows}">
            <div class="alert alert-error">
                <g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.message4"/>
            </div>
        </g:hasErrors>
        <g:if test="${flowException}">
            <div class="alert alert-error">
                ${flowException}
            </div>
        </g:if>

        <g:errorMessage bean="${invoiceSubscriptionCommand}"/>



        <h1><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.message5"/></h1>
        <p class="lead"><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.message6"/></p>



        <div class="well">
            <div class="row">
                <div class="span12">

                    <fieldset>
                        <g:if test="${availableOrganizations}">
                            <div class="control-group">
                                <label class="control-label" for="organizationId"><g:message
                                    code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message14"/></label>
                                <div class="controls">
                                    <g:select tabIndex="1" optionValue="name" value="${organization?.id}"
                                        noSelection="[null: message(code: 'facilityInvoiceRow.createInvoiceRow.selectCustomers.message15')]"
                                        name="organizationId" from="${availableOrganizations}" optionKey="id" />
                                </div>
                            </div>
                        </g:if>
                        <div class="control-group">
                            <g:if test="${articles}">
                                <label class="control-label" for="vatPercentage"><g:message code="default.article.label"/></label>
                                <g:select name="articleId" class="span2" from="${articles}" value="${invoiceSubscriptionCommand?.articleId}" noSelection="['': message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.message15')]" optionValue="descr" optionKey="id"/>
                                &nbsp;
                            </g:if>

                            <label class="control-label" for="text"><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.message8"/>*</label>
                            <g:textField class="span3 ${hasErrors(bean: invoiceSubscriptionCommand, field:'text', 'error')}"
                                    name="text" id="text" value="${invoiceSubscriptionCommand?.text}" maxlength="50" rel="tooltip" title="${message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.message9')}"/>

                            &nbsp;<label class="control-label" for="vatPercentage"><g:message code="default.vat.label"/></label>
                            <g:select class="span2" name="vatPercentage"
                                      from="${[0: message(code: 'default.vat.none'), 6:'6%', 12:'12%', 25:'25%'].entrySet()}" value="${invoiceSubscriptionCommand?.vatPercentage}" optionValue="value" optionKey="key"/>

                            &nbsp;<label class="control-label" for="discount"><g:message code="default.discount.label"/></label>
                            <g:textField class="span1" name="discount" id="discount" value="${invoiceSubscriptionCommand?.discount}"/>
                            <g:render template="/templates/facility/discountTypeSelect"
                                    model="[rowObj: invoiceSubscriptionCommand]"/>
                        </div>
                        <div class="control-group">
                            <label class="checkbox">
                              <g:checkBox name="addAppendix" value="true"
                                    checked="${invoiceSubscriptionCommand ? invoiceSubscriptionCommand.addAppendix : true}"/>
                              <g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.addAppendix"/> (<g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.mightStripEnd" args="${[com.matchi.invoice.InvoiceRow.DESCRIPTION_MAX_SIZE]}"/>)
                            </label>
                        </div>
                    </fieldset>
                </div>
            </div>
        </div>

        <g:if test="${rejectedSubscriptions.size() > 0}">
        <div class="alert alert-info">
            <g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.message18" args="[rejectedSubscriptions.size()]"/>
        </div>
        </g:if>

        <table class="table table-transparent">
            <thead>
            <th><g:message code="customer.label"/></th>
            <th width="100"><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.message12"/></th>
            <th width="100"><g:message code="default.booking.plural"/></th>
            <th width="100"><g:message code="default.price.label"/> (<g:currentFacilityCurrency />)</th>
            </thead>
            <g:each in="${confirmInformation}" var="confirmInfo">
                <tr>
                    <td>${confirmInfo.customerName}</td>
                    <td>${confirmInfo.account}</td>
                    <td>${confirmInfo.numSlots}st</td>
                    <td>
                        <g:textField class="span1 ${!confirmInfo.price?"error":""}" name="pricePerSubscription[${confirmInfo.subscriptionId}]" value="${confirmInfo.price}"/>
                        <g:inputHelp title="${message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.enterSubscriptionDetails.message19')}: ${confirmInfo.priceListName} (${formatMoney(value: confirmInfo.priceListPrice)})"/>
                    </td>
                </tr>

            </g:each>
        </table>

        <div class="form-actions">
            <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
            <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'button.next.label')}" />
        </div>

    </g:form>
    <r:script>
        var articles = {};
        <g:each in="${articles}" var="art">
            articles['${g.forJavaScript(data: art.id)}'] = ${g.forJavaScript(json: art)};
        </g:each>

        $(document).ready(function() {
            $("[rel='tooltip']").tooltip();
            $("[name='articleId']").focus().on("change", function() {
                if ($(this).val()) {
                    $("#text").val(articles[$(this).val()].descr);
                    $("#vatPercentage").val(articles[$(this).val()].VAT || 0);
                }
            });
            $("#organizationId").select2({width:'250px'}).on("change", function(e) {
                window.location.href = "<g:createLink id="addOrganizationLink" event="addOrganization"/>&organizationId=" + e.val
            });
        });
    </r:script>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>