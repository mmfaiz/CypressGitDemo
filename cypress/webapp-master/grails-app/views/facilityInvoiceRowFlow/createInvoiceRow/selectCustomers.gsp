<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message1"/></title>
    <r:require modules=" matchi-customerselect" />
</head>
<body>

<g:render template="/facilityInvoiceRowFlow/createInvoiceRow/invoiceRowBreadcrumb"/>

<ul class="nav nav-tabs">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link></li>
    <li class="active"><g:link controller="facilityInvoiceRow" action="index"><g:message code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message1"/></g:link></li>
</ul>

<g:render template="/templates/wizard" model="[steps: [message(code: 'facilityInvoiceRow.createInvoiceRow.wizard.step1'), message(code: 'facilityInvoiceRow.createInvoiceRow.wizard.step2'), message(code: 'facilityInvoiceRow.createInvoiceRow.wizard.step3')], current: 0]"/>

<h2><g:message code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message4"/></h2>
<p class="lead"><g:message code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message5"/></p>

<div class="well">
    <g:form>
        <div class="row">
            <div class="span6">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="userSearch"><g:message code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message6"/></label>
                        <div class="controls">
                            <input tabIndex="0" id="userSearch" name="userId" />
                        </div>
                    </div>
                    <g:if test="${availableGroups}">
                        <div class="control-group">
                            <label class="control-label" for="groupId"><g:message code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message7"/></label>
                            <div class="controls">
                                <g:select noSelection="[null: message(code: 'facilityInvoiceRow.createInvoiceRow.selectCustomers.message12')]" tabIndex="1"  name="groupId" from="${availableGroups}" optionKey="id" optionValue="name"/>
                            </div>
                        </div>
                    </g:if>
                </fieldset>


            </div>
            <div class="span5">
                <p class="lead"><g:message code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message8"/></p>
                <table class="table table-transparent">
                    <thead>
                    <tr>
                        <th width="200"><g:message code="default.name.label"/></th>
                        <th></th>
                        <th width="20"></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:if test="${customers.isEmpty() && groups.isEmpty()}">
                        <tr>
                            <td colspan="3" class="">
                                <p><g:message code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message10"/></p>
                            </td>
                        </tr>
                    </g:if>

                    <g:each in="${groups}" var="group">
                        <tr>
                            <td colspan="2">${group.name} <small><g:message code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message11"/></small></td>
                            <td><g:link title="Ta bort kund från underlaget" event="removeGroup" params="[groupId:group.id]"><i class="icon-remove"></i></g:link> </td>
                        </tr>
                    </g:each>
                    <g:each in="${customers}" var="customer">
                        <tr>
                            <td>${customer.fullName()}</td>
                            <td>${customer.email}</td>
                            <td><g:link title="${message(code: 'facilityInvoiceRow.createInvoiceRow.selectCustomers.message13')}" event="removeCustomer" params="[customerId:customer.id]"><i class="icon-remove"></i></g:link> </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>

            </div>
        </div>

        <div class="form-actions">
            <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
            <g:submitButton class="btn right  btn-success" id="next" name="next" value="${message(code: 'button.next.label')}" show-loader="${message(code: 'default.loader.label')}" />
        </div>
    </g:form>
</div>

<r:script>
    $(document).ready(function() {
        var usersearch = $("#userSearch").matchiCustomerSelect({width:'250px', onchange: onUserSelectChange});
        $("#groupId").select2({width:'250px'});
        $("#groupId").on("change", function(e) {
            window.location.href = "<g:createLink event="addGroup"/>&groupId=" + e.val
        });

        $("#userSearch").select2('focus');
    });

    function onUserSelectChange(customer) {
        if(customer) {
            var href = "<g:createLink event="addCustomer"/>&customerId=" + customer.id
            window.location.href = href
        }
    }





</r:script>

</body>
</html>