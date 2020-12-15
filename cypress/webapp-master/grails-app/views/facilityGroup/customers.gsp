<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="customer.label.plural"/></title>
    <r:require modules="matchi-customerselect" />
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="group.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active">${group.name} - <g:message code="customer.label.plural"/></li>
</ul>

<ul class="nav nav-tabs">
    <li class="active"><g:link action="customers" id="${group?.id}"><g:message code="customer.label.plural"/></g:link></li>
    <li>
        <g:link action="edit" id="${group?.id}"><g:message code="button.edit.label"/></g:link>
    </li>

</ul>

<g:form name="addUserToGroupForm" action="addCustomer" id="${group.id}" method="post" class="form-horizontal form-well">
    <g:hiddenField name="customerId" id="customerId" value="${customer ? customer.id : ''}"/>
    <div class="form-header">
        <g:message code="facilityGroup.customers.message19"/><span class="ingress"><g:message code="facilityGroup.customers.message5"/></span>
    </div>
    <fieldset>
        <div class="control-group ${hasErrors(bean:cmd, field:'customerId', 'error')}">
            <label class="control-label" for="search"><g:message code="customer.search.placeholder"/></label>
            <div class="controls">
                <input type="hidden" id="search" name="customerSearch" />
                <!-- <g:link class="new inline btn btn-inverse" action="addNewCustomer">Ny kund</g:link>-->
            </div>
        </div>
        <div id="customerInfo" style="display: ${customer ? 'block' : 'none'}">
            <div class="control-group no-margin">
                <label class="control-label"></label>
                <div class="controls">
                    <h4 style="margin-left: 0;"><g:message code="facilityGroup.customers.message7"/></h4>
                </div>
            </div>
            <div class="control-group ${hasErrors(bean:cmd, field:'customerId', 'error')}">
                <label class="control-label" for="number"><g:message code="customer.number.label"/></label>
                <div class="controls strong-inline">
                    <strong id="number">${customer? customer.number : ''}</strong>
                </div>
            </div>
            <div class="control-group ${hasErrors(bean:cmd, field:'customerId', 'error')}">
                <label class="control-label" for="name"><g:message code="default.name.label"/></label>
                <div class="controls strong-inline">
                    <strong id="name">${customer? customer.fullName() : ''}</strong>
                </div>
            </div>
            <div class="control-group ${hasErrors(bean:cmd, field:'email', 'error')}">
                <label class="control-label" for="email"><g:message code="customer.email.label"/></label>
                <div class="controls strong-inline">
                    <strong id="email">${customer? customer.email : ''}</strong>
                </div>
            </div>
        </div>
        <div class="control-group">
            <div class="control-label"></div>
            <div class="controls">
                <g:submitButton id="saveBtn" name="submit" value="${message(code: 'button.add.label')}" class="btn ${customer? 'btn-success' : 'btn-inverse'}"/>
                <g:link action="index" id="${group.id}" class="btn btn-danger"><g:message code="button.back.label"/></g:link>
            </div>
        </div>
    </fieldset>
</g:form>

<h4><g:message code="facilityGroup.customers.message20" args="[group.name, customers.size()]"/></h4>
<table class="table table-striped table-bordered table-hover">
    <thead>
    <tr>
        <th><g:message code="customer.number.label"/></th>
        <th><g:message code="default.name.label"/></th>
        <th><g:message code="customer.email.label"/></th>
        <th><g:message code="customer.city.label"/></th>
        <th width="100" class="center-text"><g:message code="button.edit.label"/></th>
    </tr>
    </thead>

    <g:if test="${customers.size() == 0}">
        <tr>
            <td colspan="4"><i><g:message code="facilityGroup.customers.message17"/></i></td>
        </tr>
    </g:if>
    <tbody data-provides="rowlink">
    <g:each in="${customers}" var="customer">
        <tr>
            <td><g:link controller="facilityCustomer" action="show" id="${customer.id}" class="rowlink">${customer.number}</g:link></td>
            <td>${customer.fullName()}</td>
            <td>${customer.email}</td>
            <td>${customer.city}</td>
            <td class="center-text nolink"><g:link action="removeCustomer" onclick="return confirm('${message(code: 'facilityGroup.customers.message21')}')" params="[id: group.id, customerId: customer.id]"><g:message code="button.delete.label"/></g:link> </td>
        </tr>
    </g:each>
    </tbody>
</table>
<r:script>
    var $search = $('#search');

    $(document).ready(function() {
        $("#addUserToGroupForm").preventDoubleSubmission({});
        $search.matchiCustomerSelect({width:'250px', onchange: onCustomerSelectChange, excludeCustomersInGroup: "${g.forJavaScript(data: group.id)}" });
    });

    function onCustomerSelectChange(customer) {
        $("#customerId").val(customer.id);
        $("#number").text(customer.number);
        $("#email").text(customer.email);
        $("#name").text(customer.fullname);
        $("#customerInfo").slideDown("fast");
        $("#saveBtn").removeClass("btn-inverse");
        $("#saveBtn").addClass("btn-success");
    }
</r:script>
</body>
</html>
