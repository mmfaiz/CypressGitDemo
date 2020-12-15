<%@ page import="com.matchi.Customer; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayoutBooking"/>
    <title><g:message code="facilityCustomerImport.import.title"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerImport.import.title"/></li>
</ul>

<g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerImport.import.wizard.step1'), message(code: 'facilityCustomerImport.import.wizard.step2'), message(code: 'facilityCustomerImport.import.wizard.step3'), message(code: 'default.completed.message')], current: 2]"/>

<h3><g:message code="facilityCustomerImport.import.confirm.message4"/></h3>

<p class="lead">
    <g:message code="facilityCustomerImport.import.confirm.message23"/>
</p>

<g:form>
    <div class="well" style="overflow-x: scroll">
    <table id="columns" class="table table-transparent table-condensed">
        <thead>
        <th width="30"><g:message code="default.status.label"/></th>
        <th width="30" class="center-text"><g:message code="facilityCustomerImport.import.confirm.message6"/></th>
        <th class="center-text"><g:message code="customer.type.label"/></th>
        <th class="center-text"><g:message code="facilityCustomerImport.import.confirm.message7"/></th>
        <th class="ellipsis"><g:message code="default.name.label"/></th>
        <th class="ellipsis"><g:message code="customer.lastname.label"/></th>
        <th class="ellipsis"><g:message code="customer.contact.label"/></th>
        <th class="ellipsis"><g:message code="customer.email.label"/></th>
        <th class="ellipsis"><g:message code="facilityCustomerImport.import.confirm.message11"/></th>
        <th class="ellipsis"><g:message code="facilityCustomerImport.import.confirm.message12"/></th>
        <th class="ellipsis"><g:message code="default.address.label"/></th>
        <th class="ellipsis"><g:message code="customer.zipcode.label"/></th>
        <th class="ellipsis"><g:message code="customer.city.label"/></th>
        <th class="ellipsis"><g:message code="default.country.label"/></th>
        <th class="ellipsis"><g:message code="customer.securityNumber.label"/></th>
        <th class="ellipsis"><g:message code="customer.web.label"/></th>
        <th class="ellipsis"><g:message code="default.address.label"/> (<g:message code="default.invoice.label" />)</th>
        <th class="ellipsis"><g:message code="customer.zipcode.label"/> (<g:message code="default.invoice.label" />)</th>
        <th class="ellipsis"><g:message code="customer.city.label"/> (<g:message code="default.invoice.label" />)</th>
        <th class="ellipsis"><g:message code="customer.contact.label"/> (<g:message code="default.invoice.label" />)</th>
        <th class="ellipsis"><g:message code="customer.telephone.label"/> (<g:message code="default.invoice.label" />)</th>
        <th class="ellipsis"><g:message code="customer.email.label"/> (<g:message code="default.invoice.label" />)</th>
        <th class="ellipsis"><g:message code="customer.guardianName.label"/> (<g:message code="customer.guardian.label" /> 1)</th>
        <th class="ellipsis"><g:message code="customer.guardianEmail.label"/> (<g:message code="customer.guardian.label" /> 1)</th>
        <th class="ellipsis"><g:message code="customer.guardianTelephone.label"/> (<g:message code="customer.guardian.label" /> 1)</th>
        <th class="ellipsis"><g:message code="customer.guardianName.label"/> (<g:message code="customer.guardian.label" /> 2)</th>
        <th class="ellipsis"><g:message code="customer.guardianEmail.label"/> (<g:message code="customer.guardian.label" /> 2)</th>
        <th class="ellipsis"><g:message code="customer.guardianTelephone.label"/> (<g:message code="customer.guardian.label" /> 2)</th>
        <th class="ellipsis"><g:message code="membership.type.label"/></th>
        </thead>
        <tbody>
        <g:each in="${customersData}" var="customer" status="i">
            <tr>
                <g:if test="${customer?.cmd.hasErrors()}">
                    <td class="${i}_popover"><span class="label label-important"><i class="icon-warning-sign"></i></span></td>
                </g:if>
                <g:elseif test="${customer?.cmd.willBeUpdated}">
                    <td class="${i}_popover_update"><span class="label label-warning"><i class="icon-exclamation-sign"></i></span></td>
                </g:elseif>
                <g:else>
                    <td><span class="label label-success"><g:message code="default.status.ok"/></span></td>
                </g:else>
                <td class="center-text ${customer?.cmd?.getErrors()['number'] ? 'text-error' : ''}">${customer?.cmd?.number}</td>
                <td class="center-text">${customer?.cmd?.type ? message(code: "customer.type.${customer.cmd.type}") : ""}</td>
                <td class="center-text ${customer?.cmd?.getErrors()['membershipStatusString'] ? 'text-error' : ''}">
                    <g:message code="default.${(!customer?.cmd?.getErrors()['membershipStatusString'] && customer?.cmd?.membershipStatusString) ? 'yes' : 'no'}.label"/>
                </td>
                <td class="ellipsis ${customer?.cmd?.getErrors()['firstname'] || customer?.cmd?.getErrors()['companyname']? 'text-error' : ''}">${customer?.cmd?.type.toString() == Customer.CustomerType.ORGANIZATION.toString() ? customer?.cmd?.companyname : customer?.cmd?.firstname }</td>
                <td class="ellipsis ${customer?.cmd?.getErrors()['lastname'] ? 'text-error' : ''}">${customer?.cmd?.lastname}</td>
                <td class="ellipsis">${customer?.cmd?.contact}</td>
                <td class="ellipsis ${customer?.cmd?.getErrors()['email'] ? 'text-error' : ''}">${customer?.cmd?.email}</td>
                <td class="ellipsis">${customer?.cmd?.telephone}</td>
                <td class="ellipsis">${customer?.cmd?.cellphone}</td>
                <td class="ellipsis">
                    ${customer?.cmd?.address1}
                    <g:if test="${customer?.cmd?.address2}">
                        <br/>${customer?.cmd?.address2}
                    </g:if>
                </td>
                <td>${customer?.cmd?.zipcode}</td>
                <td class="ellipsis">${customer?.cmd?.city}</td>
                <td class="ellipsis ${customer?.cmd?.getErrors()['country'] ? 'text-error' : ''}">${customer?.cmd?.country ? message(code: "country.${customer?.cmd?.country}") : ""}</td>
                <g:if test="${customer?.cmd?.type == com.matchi.Customer.CustomerType.ORGANIZATION}">
                    <td class="ellipsis ${customer?.cmd?.getErrors()['orgNumber'] ? 'text-error' : ''}">${customer?.cmd?.orgNumber}</td>
                </g:if>
                <g:else>
                    <td class="ellipsis ${customer?.cmd?.getErrors()['securityNumber'] || customer?.cmd?.getErrors()['personalNumber'] ? 'text-error' : ''}">${customer?.cmd?.personalNumber}${customer?.cmd?.securityNumber ? ("-" + customer?.cmd?.securityNumber) : ""}</td>
                </g:else>
                <td class="ellipsis ${customer?.cmd?.getErrors()['web'] ? 'text-error' : ''}">${customer?.cmd?.web}</td>
                <td class="ellipsis">
                    ${customer?.cmd?.invoiceAddress1}
                    <g:if test="${customer?.cmd?.invoiceAddress2}">
                        <br/>${customer?.cmd?.invoiceAddress2}
                    </g:if>
                </td>
                <td>${customer?.cmd?.invoiceZipcode}</td>
                <td class="ellipsis">${customer?.cmd?.invoiceCity}</td>
                <td class="ellipsis">${customer?.cmd?.invoiceContact}</td>
                <td class="ellipsis">${customer?.cmd?.invoiceTelephone}</td>
                <td class="ellipsis ${customer?.cmd?.getErrors()['invoiceEmail'] ? 'text-error' : ''}">${customer?.cmd?.invoiceEmail}</td>
                <td class="ellipsis">${customer?.cmd?.guardianName}</td>
                <td class="ellipsis ${customer?.cmd?.getErrors()['guardianEmail'] ? 'text-error' : ''}">${customer?.cmd?.guardianEmail}</td>
                <td class="ellipsis">${customer?.cmd?.guardianTelephone}</td>
                <td class="ellipsis">${customer?.cmd?.guardianName2}</td>
                <td class="ellipsis ${customer?.cmd?.getErrors()['guardianEmail2'] ? 'text-error' : ''}">${customer?.cmd?.guardianEmail2}</td>
                <td class="ellipsis">${customer?.cmd?.guardianTelephone2}</td>
                <td class="ellipsis ${customer?.cmd?.getErrors()['membershipTypeString'] ? 'text-error' : ''}">${customer?.cmd?.membershipTypeString}</td>
            </tr>
        </g:each>
        </tbody>
    </table>
    </div>
    <div class="form-actions">
        <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>

        <g:submitButton name="submit" type="submit" value="${message(code: 'button.confirm.label')}"
                        onclick="return confirm('${message(code: 'facilityCustomerImport.import.confirm.message24')}')"
                        data-toggle="button" class="btn btn-success pull-right" show-loader="${message(code: 'default.loader.label')}"/>
        <g:link event="previous" class="btn btn-info pull-right right-margin5"><g:message code="button.back.label"/></g:link>
    </div>
</g:form>
<r:script>
$(document).ready(function() {
    <g:each in="${customersData}" var="customer" status="i">
        $(".${i}_popover").popover({title: "${message(code: 'facilityCustomerImport.import.confirm.message26')}", content: "" +
                "<g:renderErrors bean="${customer.cmd}" as="list" />",
                trigger: "hover",
                placement: "top"
            });

        $(".${i}_popover_update").popover({title: "${message(code: 'facilityCustomerImport.import.confirm.message27.label')}", content: "" +
                "${message(code: 'facilityCustomerImport.import.confirm.message27.content')}",
                trigger: "hover",
                placement: "top"
            });
    </g:each>
});
</r:script>
</body>
</html>
