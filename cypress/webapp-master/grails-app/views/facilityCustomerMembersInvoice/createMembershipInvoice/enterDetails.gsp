<%@ page import="org.joda.time.DateTime; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message1"/></title>
    <r:require modules="matchi-customerselect" />
</head>
<body>

<ul class="breadcrumb">
    <li><g:link controller="facilityCustomerMembers" action="index"><g:message code="default.member.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityInvoiceRow.createInvoiceRow"/></li>
</ul>

<g:form class="form-inline">
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step0'), message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step1'), message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step2'), message(code: 'default.modal.done')], current: 1]"/>

    <g:errorMessage bean="${invoiceMembershipCommand}"/>

    <g:if test="${!confirmInformation}">
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert"><i class="fa fa-times-circle"></i></button>
            <g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.noMemberships"/>
        </div>
    </g:if>

    <g:if test="${cancelMemberships || upcomingDifferentMemberships || upcomingInvoicedMemberships || invoicedMemberships || paidMemberships || freeMemberships}">
        <div class="alert">
            <button type="button" class="close" data-dismiss="alert"><i class="fa fa-times-circle"></i></button>

            <g:if test="${cancelMemberships}">
                <div>
                    <g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.cancelMemberships"
                            args="[cancelMemberships]"/>
                </div>
            </g:if>
            <g:if test="${upcomingDifferentMemberships}">
                <div>
                    <g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.upcomingDifferentMemberships"
                            args="[upcomingDifferentMemberships]"/>
                </div>
            </g:if>
            <g:if test="${upcomingInvoicedMemberships}">
                <div>
                    <g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.upcomingInvoicedMemberships"
                            args="[upcomingInvoicedMemberships]"/>
                </div>
            </g:if>
            <g:if test="${invoicedMemberships}">
                <div>
                    <g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.invoicedMemberships"
                            args="[invoicedMemberships]"/>
                </div>
            </g:if>
            <g:if test="${paidMemberships}">
                <div>
                    <g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.paidMemberships"
                            args="[paidMemberships]"/>
                </div>
            </g:if>
            <g:if test="${freeMemberships}">
                <div>
                    <g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.freeMemberships"
                            args="[freeMemberships]"/>
                </div>
            </g:if>
        </div>
    </g:if>

    <h1><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.message4"/></h1>
    <p class="lead"><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.message5"/></p>

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
                            &nbsp;<label class="control-label" for="vatPercentage"><g:message code="default.article.label"/></label>
                            <g:select name="articleId" class="span2" from="${articles}" value="${invoiceMembershipCommand?.articleId}"
                                      noSelection="['': message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.message19')]" optionValue="descr" optionKey="id"/>
                            &nbsp;
                        </g:if>
                        <g:else>
                            <label class="control-label" for="text"><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.message6"/></label>
                            <g:textField class="span2 ${hasErrors(bean: invoiceMembershipCommand, field:'account', 'error')}"
                                         name="account" id="account" value="${invoiceMembershipCommand?.account}" maxlength="50"/>
                        </g:else>

                        <label class="control-label" for="text"><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.message7"/>*</label>
                        <g:textField class="span3 ${hasErrors(bean: invoiceMembershipCommand, field:'text', 'error')}"
                                name="text" id="text" value="${invoiceMembershipCommand?.text}" maxlength="50"/>

                        &nbsp;<label class="control-label" for="vatPercentage"><g:message code="default.vat.label"/></label>

                        <g:select class="span2" name="vatPercentage"
                                  from="${vats.entrySet()}" value="${invoiceMembershipCommand?.vatPercentage}" optionValue="value" optionKey="key"/>

                        &nbsp;<label class="control-label" for="discount"><g:message code="default.discount.label"/></label>
                        <g:textField class="span1" name="discount" id="discount" value="${invoiceMembershipCommand?.discount}"/>
                        <g:render template="/templates/facility/discountTypeSelect"
                                model="[rowObj: invoiceMembershipCommand]"/>
                    </div>

                    <span class="help-block"><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.message16" args="[formatDate(date: new Date(), format: 'yyyy', locale: 'sv')]"/></span>


                </fieldset>
            </div>
        </div>
    </div>

    <table class="table table-transparent">
        <thead>
        <th width="80"><g:message code="customer.number.label"/></th>
        <th><g:message code="default.member.label"/></th>
        <th class="center-text"><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.message12"/></th>
        <th width="100"><g:message code="membershipType.label"/></th>
        <th width="100"><g:message code="default.price.label"/> (<g:currentFacilityCurrency />)</th>
        </thead>
        <g:each in="${confirmInformation}" var="confirmInfo">
            <tr>
                <td>${confirmInfo.customerNr}</td>
                <td>${confirmInfo.customerName}</td>
                <g:if test="${confirmInfo.isFamilyContact}">
                    <td class="center-text">
                        <strong><span id="${confirmInfo.membershipId}_family" class="popover-hint">${confirmInfo.familyMembers.size()}<g:message code="unit.st"/></span></strong>
                    </td>
                </g:if>
                <g:else>
                    <td class="center-text">-</td>
                </g:else>
                <td>${confirmInfo.typeName}</td>
                <td>
                    <g:textField class="span1 ${!confirmInfo.price?"error":""}" name="pricePerMembership[${confirmInfo.membershipId}]" value="${confirmInfo.price}"/>
                    <g:if test="${confirmInfo.isFamilyContact}">
                        <g:inputHelp title="${message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.message17')}"/>
                    </g:if>
                </td>
            </tr>

        </g:each>
    </table>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
        <g:if test="${confirmInformation}">
            <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'button.next.label')}" />
        </g:if>
        <g:submitButton class="btn right btn-info ${confirmInformation ? 'right-margin5' : ''}"
                name="back" value="${message(code: 'button.back.label')}" />
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

        <g:each in="${confirmInformation}" var="confirmInfo">
            <g:if test="${confirmInfo.isFamilyContact && confirmInfo.familyMembers}">
                $("#${g.forJavaScript(data: confirmInfo.membershipId)}_family").popover({
                    title: "${message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.message18')}",
                    content: "" +
                        "<table class='table no-margin-padding' style='font-size: 12px; color: #2c2c2c;'>" +
                        <g:each in="${confirmInfo.familyMembers}" var="member">
                            "<tr><td nowrap>${g.forJavaScript(data: member.fullName)}</td><td nowrap>${g.forJavaScript(data: member.birthyear)}</td><td nowrap>${g.forJavaScript(data: member.type)}</td><td nowrap><g:formatMoney value="${member.price}"/></td></tr>" +
                        </g:each>
                        "</table>",
                    trigger: "hover",
                    placement: "top"
                });
            </g:if>
        </g:each>
    });
</r:script>
</body>
</html>