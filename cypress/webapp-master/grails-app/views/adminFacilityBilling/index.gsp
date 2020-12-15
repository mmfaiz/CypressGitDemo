<%@ page import="com.matchi.Court; com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="adminFacilityBilling.index.title"/></title>
</head>
<body>
<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link></li>
        <li><i class="fas fa-edit"></i><g:link controller="adminFacility" action="edit" id="${facility?.id}"><g:message code="adminFacility.edit.title"/></g:link><span class="divider"></span></li>
        <li class="active"><g:message code="adminFacilityBilling.index.title"/></li>
    </ol>

    <g:b3StaticErrorMessage bean="${facility}"/>

    <div class="panel panel-default panel-admin">
        <g:render template="/adminFacility/adminFacilityMenu" model="[selected: 5]"/>

        <div class="form-group col-sm-12 well">
        <g:each in="${invoiceGroups}" var="invoiceGroup">
            <h2>${invoiceGroup.name}</h2>

            <table class="table table-striped table-hover table-bordered">
                <thead>
                <tr>
                    <th class="vertical-padding10 col-md-2"><g:message code="adminFacilityBilling.invoiceNumber.label"/></th>
                    <th class="vertical-padding10 col-md-2"><g:message code="adminFacilityBilling.invoiceDate.label"/></th>
                    <th class="vertical-padding10 col-md-2"><g:message code="adminFacilityBilling.invoiceBooked.label"/></th>
                    <th class="vertical-padding10 col-md-2"><g:message code="adminFacilityBilling.invoiceCurrency.label"/></th>
                    <th class="vertical-padding10 col-md-4" style="text-align: right;"><g:message code="adminFacilityBilling.invoiceTotal.label"/></th>
                </tr>
                </thead>
                <tbody>

                <g:each in="${invoiceGroup.invoices}" var="invoice">
                    <tr>
                        <td>
                            <g:link action="preview" id="${invoice.DocumentNumber}">${invoice.DocumentNumber}</g:link>
                        </td>
                        <td><g:formatDate date="${invoice.InvoiceDate}" format="yyyy-MM-dd"/></td>
                        <td>
                            <g:if test="${invoice.Booked}">
                                <g:message code="default.yes.label"/>
                            </g:if>
                            <g:else>
                                <g:message code="default.no.label"/>
                            </g:else>
                        </td>
                        <td>${invoice.Currency}</td>
                        <td style="text-align: right;"><g:formatMoneyShort value="${invoice.Total}" forceZero="true"/></td>
                    </tr>
                </g:each>

                </tbody>
                <g:if test="${invoiceGroup?.invoices?.size() < 1}">
                    <tfoot>
                    <tr>
                        <td colspan="7" class="vertical-padding20">
                            <span class="text-muted text-md"><i class="ti-info-alt"></i><g:message code="default.noElements"/></span>
                        </td>
                    </tr>
                    </tfoot>
                </g:if>
            </table>

        </g:each>
        </div>

    </div>
</div>
</body>
</html>
