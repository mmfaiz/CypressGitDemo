<%@ page import="com.matchi.User; com.matchi.Customer;" %>
<head>
    <meta name="layout" content="b3main" />
    <title><g:message code="updateCustomerRequest.index.message1"/></title>
</head>
<body>

<!-- Messages -->
<g:b3StaticErrorMessage bean="${cmd}"/>

<div class="block block-grey">
    <div class="container">
        <div class="container">
            <div class="row">
                <div class="col-md-8 col-md-offset-2 text-center">
                    <h1><g:message code="updateCustomerRequest.index.message1"/></h1>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="block block-white">
    <div class="container">
        <div class="container">
            <div class="row">
                <div class="col-md-8 col-md-offset-2 text-center vertical-padding20">
                    <p>
                        <g:message code="updateCustomerRequest.index.message16"/>
                    </p>
                    <p>
                        <g:message code="updateCustomerRequest.index.message22"/>
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="block block-white">
    <div class="container">
        <g:form controller="updateCustomerRequest" action="update" class="form-center">
            <g:hiddenField name="customerId" value="${cmd?.customerId ?: customer.id}" />
            <g:hiddenField name="facilityId" value="${cmd?.facilityId ?: facility.id}" />

            <div class="row vertical-padding30">
                <div class="col-md-4 col-md-offset-2">
                    <div class="form-group ${hasErrors(bean:cmd, field:'firstname', 'has-error')}">
                        <label class="control-label" for='firstname'><g:message code="customer.firstname.label"/></label>
                        <div class="controls">
                            <g:textField class="form-control" name="firstname" value="${cmd?.firstname ?: customer?.firstname}"/>
                        </div>
                    </div>
                    <div class="form-group ${hasErrors(bean:cmd, field:'lastname', 'has-error')}">
                        <label class="control-label" for='lastname'><g:message code="customer.lastname.label"/></label>
                        <div class="controls">
                            <g:textField class="form-control" name="lastname" value="${cmd?.lastname ?: customer?.lastname}"/>
                        </div>
                    </div>
                    <div class="form-group ${hasErrors(bean:cmd, field:'email', 'has-error')}">
                        <label class="control-label" for="email"><g:message code="customer.email.label"/></label>
                        <div class="controls">
                            <g:textField class="form-control" name="email" value="${cmd?.email ?: customer?.email}"/>
                        </div>
                    </div>
                    <div class="form-group ${hasErrors(bean:cmd, field:'address', 'has-error')}">
                        <label class="control-label" for="address"><g:message code="updateCustomerRequest.index.message8"/></label>
                        <div class="controls">
                            <g:textField class="form-control" name="address" value="${cmd?.address ?: customer?.address1}"/>
                        </div>
                    </div>
                    <div class="form-group ${hasErrors(bean:cmd, field:'zipcode', 'has-error')}">
                        <label class="control-label" for="zipcode"><g:message code="updateCustomerRequest.index.message9"/></label>
                        <div class="controls">
                            <g:textField class="form-control" name="zipcode" value="${cmd?.zipcode ?: customer?.zipcode}"/>
                        </div>
                    </div>
                    <div class="form-group ${hasErrors(bean:cmd, field:'city', 'has-error')}">
                        <label class="control-label" for="zipcode"><g:message code="updateCustomerRequest.index.message10"/></label>
                        <div class="controls">
                            <g:textField class="form-control" name="city" value="${cmd?.city ?: customer?.city}"/>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="form-group ${hasErrors(bean:cmd, field:'country', 'has-error')}">
                        <label for="country"><g:message code="default.country.label"/></label>
                        <g:textField class="form-control" name="country" value="${cmd?.country ?: customer?.country}"/>
                    </div>
                    <div class="form-group ${hasErrors(bean:cmd, field:'cellphone', 'has-error')}">
                        <label for="cellphone"><g:message code="customer.cellphone.label"/></label>
                        <g:textField class="form-control" name="cellphone" value="${cmd?.cellphone ?: (customer?.cellphone ?: customer?.user?.telephone)}"/>
                    </div>
                    <div class="form-group ${hasErrors(bean:cmd, field:'telephone', 'has-error')}">
                        <label for="telephone"><g:message code="customer.telephone.label"/></label>
                        <g:textField class="form-control" name="telephone" value="${cmd?.telephone}"/>
                    </div>

                    <div class="form-group ${hasErrors(bean:cmd, field:'type', 'has-error')}">
                        <label for="type"><g:message code="default.gender.label"/></label><br>
                        <select id="type" name="type" data-style="form-control" data-title="${message(code: 'updateCustomerRequest.index.message14')}">
                            <option data-icon="fas fa-male" value="${Customer.CustomerType.MALE}" ${cmd?.type?.equals(Customer.CustomerType.MALE) ? "selected": customer?.type?.equals(Customer.CustomerType.MALE) ? "selected" : ""}>
                                <g:message code="customer.type.${Customer.CustomerType.MALE}"/></option>
                            <option data-icon="fas fa-female" value="${Customer.CustomerType.FEMALE}" ${cmd?.type?.equals(Customer.CustomerType.FEMALE) ? "selected": customer?.type?.equals(Customer.CustomerType.FEMALE) ? "selected" : ""}>
                                <g:message code="customer.type.${Customer.CustomerType.FEMALE}"/></option>
                        </select>
                    </div>
                    <div class="form-group ${hasErrors(bean:cmd, field:'birthday', 'has-error')} ${hasErrors(bean:cmd, field:'securitynumber', 'has-error')}">
                        <label for="birthday">
                            <g:message code="updateCustomerRequest.index.message19"
                                    args="[message(code: 'membershipRequestCommand.birthday.format', locale: new Locale(facility.language))]"/>
                            <g:inputHelp title="${message(code: 'updateCustomerRequest.index.message20')}"/>
                        </label>
                        <div class="row">
                            <div class="col-xs-5 col-md-5">
                                <g:textField name="birthday" class="form-control text-right" value="${cmd?.birthday ?: ""}"/>
                            </div>
                            <div class="col-xs-3 col-md-3">
                                <g:textField name="securitynumber" class="form-control" value="${cmd?.securitynumber ?: ""}"/>
                            </div>
                        </div>
                    </div>
                    <div class="form-group ${hasErrors(bean:cmd, field:'message', 'has-error')}">
                        <label for="message"><g:message code="updateCustomerRequest.index.message15"/></label>
                        <g:textArea name="message" rows="3" cols="50" class="form-control" value="${cmd?.message}"/>
                    </div>
                    <div class="form-group ${hasErrors(bean:cmd, field:'confirmation', 'has-error')}">
                        <div class="checkbox">
                            <g:checkBox class="checkbox" name="confirmation" value="${cmd?.confirmation}"/>
                            <label for="confirmation">
                                <g:message code="updateCustomerRequest.index.message21"/>
                            </label>
                        </div>
                    </div>

                    <g:submitButton name="sumbit" value="${message(code: 'updateCustomerRequest.index.message3')}" class="btn btn-success col-xs-12 col-md-6"/>
                </div>
            </div>
        </g:form>
    </div>
</div>
<g:javascript>
    $(document).ready(function() {
        $("[rel=tooltip]").tooltip();

        $("#type").selectpicker({
            title: "${message(code: 'updateCustomerRequest.index.message14')}"
        });
    });
</g:javascript>
</body>
