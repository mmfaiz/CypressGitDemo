<%@ page import="com.matchi.Customer" %>
<g:set var="isCompany" value="${cmd?.type == com.matchi.Customer.CustomerType.ORGANIZATION.name()}"/>
<h4><g:message code="facilityCustomer.form.message1"/></h4>
<div class="control-group ${hasErrors(bean:cmd, field:'number', 'error')}  ${hasErrors(bean:cmd, field:'email', 'error')}">
    <label class="control-label" for="number">
        <g:message code="customer.number.label2"/> <g:inputHelp title="${message(code: 'facilityCustomer.form.message3')}"/>
    </label>
    <div class="controls controls-row">
        <g:textField name="number" value="${cmd?.number}" class="span3"/>

        <label class="control-label span1" for="email"><g:message code="customer.email.label"/></label>
        <g:textField name="email" value="${cmd?.email}" class="span3"/>
    </div>
</div>
<div class="control-group ${hasErrors(bean:cmd, field:'type', 'error')}">
    <label class="control-label" for="type"><g:message code="facilityCustomer.form.message5"/></label>
    <div class="controls controls-row">
        <select id="type" name="type" class="span3">
            <g:each in="${Customer.CustomerType.list()}">
                <option value="${it}" ${cmd?.type.toString() == it.toString() ? "selected":""}><g:message code="customer.type.${it}"/></option>
            </g:each>
        </select>

        <div class="person-control-fields control-group ${hasErrors(bean:cmd, field:'securityNumber', 'error')}">
            <label class="control-label span1" for="personalNumber">
                <g:message code="facilityCustomer.form.message6"/>
                <g:inputHelp id="person-number-help" title="${message(code: "facilityCustomer.form.${isCompany ? 'message8' : 'message7'}", args: [message(code: "facilityCustomer.form.${isCompany ? 'orgNumberFormat' : 'personalNumberFormat'}", locale: new Locale(facility.language))])}"/>
            </label>

            <g:textField name="personalNumber" value="${cmd?.personalNumber}" class="span2"/>

            <span class="span0" style="text-align: center; position: absolute; width: 20px; margin-left: 0; line-height: 28px;">-</span>
            <g:textField name="securityNumber" value="${cmd?.securityNumber}" class="span1"/>
        </div>
        <div class="company-control-fields control-group ${hasErrors(bean:cmd, field:'orgNumber', 'error')}" style="display: ${((cmd?.type).toString()).equals((Customer.CustomerType.ORGANIZATION.toString())) ? "block" : "none"};">
            <label class="control-label span1" for="orgNumber">
                <g:message code="facilityCustomer.form.message61"/>
                <g:inputHelp id="person-number-help" title="${message(code: "facilityCustomer.form.message8", args: [message(code: "facilityCustomer.form.orgNumberFormat", locale: new Locale(facility.language))])}"/>
            </label>

            <g:textField name="orgNumber" value="${cmd?.orgNumber}" class="span3"/>
        </div>
    </div>
</div>
<div class="control-group person-control-fields ${hasErrors(bean:cmd, field:'firstname', 'error')} ${hasErrors(bean:cmd, field:'lastname', 'error')}" style="display: ${!((cmd?.type).toString()).equals((Customer.CustomerType.ORGANIZATION.toString())) ? "block" : "none"};">
    <label class="control-label" for="firstname"><g:message code="customer.firstname.label"/> <g:inputHelp title="${message(code: 'default.mandatoryField.label')}"/></label>
    <div class="controls controls-row">
        <g:textField name="firstname" value="${cmd?.firstname}" class="span3"/>

        <label class="control-label span1" for="lastname"><g:message code="customer.lastname.label"/> <g:inputHelp title="${message(code: 'default.mandatoryField.label')}"/></label>
        <g:textField name="lastname" value="${cmd?.lastname}" class="span3"/>
    </div>
</div>
<g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name()}">
    <div id="club-control" class="control-group" ${hasErrors(bean:cmd, field:'club', 'error')} style="display: ${!((cmd?.type).toString()).equals((Customer.CustomerType.ORGANIZATION.toString())) ? "block" : "none"};">
        <label class="control-label" for="club"><g:message code="default.club.label"/></label>
        <div class="controls controls-row">
            <g:textField name="club" value="${cmd?.club}" class="span8"/>
        </div>
    </div>
</g:ifFacilityPropertyEnabled>

<div class="company-control-fields" style="display: ${((cmd?.type).toString()).equals((Customer.CustomerType.ORGANIZATION.toString())) ? "block" : "none"};">
    <div class="control-group ${hasErrors(bean:cmd, field:'companyname', 'error')}">
        <label class="control-label" for="companyname"><g:message code="facilityCustomer.form.message10"/> <g:inputHelp title="${message(code: 'default.mandatoryField.label')}"/></label>
        <div class="controls">
            <g:textField name="companyname" value="${cmd?.companyname}" class="span5"/>
        </div>
    </div>
    <div class="control-group ${hasErrors(bean:cmd, field:'vatnumber', 'error')}">
        <label class="control-label" for="vatNumber"><g:message code="facilityCustomer.form.vatnumber"/></label>
        <div class="controls">
            <g:textField name="vatNumber" value="${cmd?.vatNumber}" class="span5"/>
        </div>
    </div>
    <div class="control-group ${hasErrors(bean:cmd, field:'contact', 'error')}">
        <label class="control-label" for="contact"><g:message code="customer.contact.label"/></label>
        <div class="controls">
            <g:textField name="contact" value="${cmd?.contact}" class="span5"/>
        </div>
    </div>
    <div class="control-group ${hasErrors(bean:cmd, field:'web', 'error')}">
        <label class="control-label" for="web"><g:message code="customer.web.label"/></label>
        <div class="controls">
            <g:textField name="web" value="${cmd?.web}" class="span5"/>
        </div>
    </div>
</div>
<div class="control-group ${hasErrors(bean: cmd, field: 'clubMessagesDisabled', 'error')}">
    <div class="controls">
        <label class="checkbox">
            <g:checkBox name="clubMessagesDisabled" value="${cmd?.clubMessagesDisabled}"
            /><g:message code="customer.clubMessagesDisabled.label"/>  <g:inputHelp title="${message(code: 'facilityCustomer.form.message13')}"/>
        </label>
    </div>
</div>
<g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name()}">
    <div class="control-group ${hasErrors(bean: cmd, field: 'exludeFromNumberOfBookingsRule', 'error')}">
        <div class="controls">
            <label class="checkbox">
                <g:checkBox name="exludeFromNumberOfBookingsRule" value="${cmd?.exludeFromNumberOfBookingsRule}"
                /><g:message code="customer.exludeFromNumberOfBookingsRule.label"/>
            </label>
        </div>
    </div>
</g:ifFacilityPropertyEnabled>
<hr>
<div class="control-group ${hasErrors(bean:cmd, field:'address1', 'error')}">
    <label class="control-label" for="address1"><g:message code="customer.address1.label"/></label>
    <div class="controls">
        <g:textField name="address1" value="${cmd?.address1}" class="span8"/>
    </div>
</div>
<div class="control-group ${hasErrors(bean:cmd, field:'address2', 'error')}">
    <label class="control-label" for="address1"><g:message code="customer.address2.label"/></label>
    <div class="controls">
        <g:textField name="address2" value="${cmd?.address2}" class="span8"/>
    </div>
</div>
<div class="control-group ${hasErrors(bean:cmd, field:'zipcode', 'error')} ${hasErrors(bean:cmd, field:'city', 'error')}">
    <label class="control-label" for="zipcode"><g:message code="customer.zipcode.label"/></label>
    <div class="controls controls-row">
        <g:textField name="zipcode" value="${cmd?.zipcode}" class="span3"/>

        <label class="control-label span1" for="city"><g:message code="customer.city.label"/></label>
        <g:textField name="city" value="${cmd?.city}" class="span3"/>
    </div>
</div>
<div class="control-group ${hasErrors(bean:cmd, field:'country', 'error')}">
    <label class="control-label" for="country"><g:message code="default.country.label"/></label>
    <div class="controls controls-row"">
        <g:select id="country" name="country" from="${grailsApplication.config.matchi.settings.available.countries}"
                  valueMessagePrefix="country" value="${cmd?.country}" noSelection="['':message(code: 'userProfile.edit.message29')]"
                  class="span3"/>

        <label class="control-label span1" for="nationality"><g:message code="default.nationality.label"/></label>
        <g:select id="nationality" name="nationality" from="${grailsApplication.config.matchi.settings.available.countries}"
                  valueMessagePrefix="country" value="${cmd?.nationality}" noSelection="['':message(code: 'userProfile.edit.message30')]"
                  class="span3"/>
    </div>
</div>

<div class="control-group ${hasErrors(bean:cmd, field:'telephone', 'error')} ${hasErrors(bean:cmd, field:'cellphone', 'error')}">
    <label class="control-label" for="telephone"><g:message code="customer.telephone.label"/></label>
    <div class="controls controls-row">
        <g:textField name="telephone" value="${cmd?.telephone}" class="span3"/>

        <label class="control-label span1" for="cellphone"><g:message code="customer.cellphone.label"/></label>
        <g:textField name="cellphone" value="${cmd?.cellphone}" class="span3"/>
    </div>
</div>
<hr>
<% def showGuardian = cmd?.guardianName || cmd?.guardianEmail || cmd?.guardianTelephone || cmd?.guardianName2 || cmd?.guardianEmail2 || cmd?.guardianTelephone2 %>
<h4 id="guardian-control"><a href="javascript:void(0)"><g:message code="customer.guardian.label"/></a> <i id="guardian-marker" class="${showGuardian ? "icon-chevron-down":"icon-chevron-right"}"></i></h4>
<div class="${showGuardian ? "":"hidden"} guardian-content">
    <div class="control-group ${hasErrors(bean:cmd, field:'guardianName', 'error')}">
        <label class="control-label" for="guardianName"><g:message code="customer.guardianName.label"/></label>
        <div class="controls">
            <g:textField name="guardianName" value="${cmd?.guardianName}" class="span8"/>
        </div>
    </div>
    <div class="control-group ${hasErrors(bean:cmd, field:'guardianEmail', 'error')} ${hasErrors(bean:cmd, field:'guardianTelephone', 'error')}">
        <label class="control-label" for="guardianEmail"><g:message code="customer.guardianEmail.label"/></label>
        <div class="controls controls-row">
            <g:textField name="guardianEmail" value="${cmd?.guardianEmail}" class="span3"/>

            <label class="control-label span1" for="guardianTelephone"><g:message code="customer.guardianTelephone.label"/></label>
            <g:textField name="guardianTelephone" value="${cmd?.guardianTelephone}" class="span3"/>
        </div>
    </div>
</div>
<div class="${showGuardian ? "":"hidden"} guardian-content">
    <hr>
    <div class="control-group ${hasErrors(bean:cmd, field:'guardianName2', 'error')}">
        <label class="control-label" for="guardianName2"><g:message code="customer.guardianName2.label"/></label>
        <div class="controls">
            <g:textField name="guardianName2" value="${cmd?.guardianName2}" class="span8"/>
        </div>
    </div>
    <div class="control-group ${hasErrors(bean:cmd, field:'guardianEmail2', 'error')} ${hasErrors(bean:cmd, field:'guardianTelephone2', 'error')}">
        <label class="control-label" for="guardianEmail2"><g:message code="customer.guardianEmail2.label"/></label>
        <div class="controls controls-row">
            <g:textField name="guardianEmail2" value="${cmd?.guardianEmail2}" class="span3"/>

            <label class="control-label span1" for="guardianTelephone2"><g:message code="customer.guardianTelephone2.label"/></label>
            <g:textField name="guardianTelephone2" value="${cmd?.guardianTelephone2}" class="span3"/>
        </div>
    </div>
</div>
<hr>
<% def showInvoice = cmd?.invoiceContact || cmd?.invoiceAddress1 || cmd?.invoiceAddress2 %>
<h4 id="invoice-control"><a href="javascript:void(0)"><g:message code="facilityCustomer.form.message22"/></a> <i id="toggleMarker" class="${showInvoice ? "icon-chevron-down":"icon-chevron-right"}"></i></h4>
<div id="invoice-content" class="${showInvoice ? "":"hidden"}">
    <div class="control-group ${hasErrors(bean:cmd, field:'invoiceContact', 'error')}">
        <label class="control-label" for="invoiceContact"><g:message code="customer.invoiceContact.label"/></label>
        <div class="controls">
            <g:textField name="invoiceContact" value="${cmd?.invoiceContact}" class="span8"/>
        </div>
    </div>
    <div class="control-group ${hasErrors(bean:cmd, field:'invoiceAddress1', 'error')}">
        <label class="control-label" for="invoiceAddress1"><g:message code="customer.address1.label"/></label>
        <div class="controls">
            <g:textField name="invoiceAddress1" value="${cmd?.invoiceAddress1}" class="span8"/>
        </div>
    </div>
    <div class="control-group ${hasErrors(bean:cmd, field:'invoiceAddress2', 'error')}">
        <label class="control-label" for="invoiceAddress2"><g:message code="customer.address2.label"/></label>
        <div class="controls">
            <g:textField name="invoiceAddress2" value="${cmd?.invoiceAddress2}" class="span8"/>
        </div>
    </div>
    <div class="control-group ${hasErrors(bean:cmd, field:'invoiceZipcode', 'error')} ${hasErrors(bean:cmd, field:'city', 'error')}">
        <label class="control-label" for="invoiceZipcode"><g:message code="customer.zipcode.label"/></label>
        <div class="controls controls-row">
            <g:textField name="invoiceZipcode" value="${cmd?.invoiceZipcode}" class="span3"/>

            <label class="control-label span1" for="invoiceCity"><g:message code="customer.city.label"/></label>
            <g:textField name="invoiceCity" value="${cmd?.invoiceCity}" class="span3"/>
        </div>
    </div>
    <div class="control-group ${hasErrors(bean:cmd, field:'invoiceTelephone', 'error')} ${hasErrors(bean:cmd, field:'invoiceEmail', 'error')}">
        <label class="control-label" for="invoiceTelephone"><g:message code="customer.invoiceTelephone.label"/></label>
        <div class="controls controls-row">
            <g:textField name="invoiceTelephone" value="${cmd?.invoiceTelephone}" class="span3"/>

            <label class="control-label span1" for="invoiceEmail"><g:message code="customer.invoiceEmail.label"/></label>
            <g:textField name="invoiceEmail" value="${cmd?.invoiceEmail}" class="span3"/>
        </div>
    </div>
</div>
<hr>
<div class="control-group ${hasErrors(bean:cmd, field:'notes', 'error')}">
    <label class="control-label" for="notes"><g:message code="facilityCustomer.form.notes"/>
        <g:inputHelp title="${message(code: 'facilityCustomer.form.notes.hint')}"/></label>
    <div class="controls">
        <g:textArea rows="5" cols="50" name="notes" value="${cmd?.notes}" class="span8"/>
    </div>
</div>
<g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_PERSONAL_ACCESS_CODE.name()}">
<div class="control-group ${hasErrors(bean:cmd, field:'accessCode', 'error')}">
    <label class="control-label" for="accessCode">
        <g:message code="facilityCustomer.form.accessCode"/>
        <g:inputHelp title="${message(code: 'facilityCustomer.form.accessCode.hint')}"/>
    </label>
    <div class="controls">
        <g:textField name="accessCode" value="${cmd?.accessCode}" class="span3"/>
    </div>
</div>
</g:ifFacilityPropertyEnabled>

<r:script>
    $(document).ready(function() {
        $personControl  = $(".person-control-fields");
        $companyControl = $(".company-control-fields");
        $clubControl = $("#club-control");
        $personNumberHelp = $("#person-number-help");
        $("[rel='tooltip']").tooltip();

        $("#type").on("change", function() {
            toggleTypeInputs();
        });

        function toggleTypeInputs() {
            if ( $("#type option:selected").val().toString() == "${g.forJavaScript(data: com.matchi.Customer.CustomerType.ORGANIZATION)}" ) {
                $personControl.hide();
                $personControl.find("input").val("")
                $companyControl.show();
                $clubControl.hide();
                $clubControl.find("input").val("")
                $personNumberHelp.attr("data-original-title", "${message(code: "facilityCustomer.form.message8", args: [message(code: "facilityCustomer.form.orgNumberFormat", locale: new Locale(facility.language))])}");
            } else {
                $personControl.show();
                $companyControl.hide();
                $companyControl.find("input").val("")
                $clubControl.show();
                $personNumberHelp.attr("data-original-title", "${message(code: "facilityCustomer.form.message7", args: [message(code: "facilityCustomer.form.personalNumberFormat", locale: new Locale(facility.language))])}");
            }
        }

        toggleTypeInputs();

        $("#guardian-control").on("click", function() {
            var toggleMarker = $("#guardian-marker");
            if(toggleMarker.hasClass('icon-chevron-right')) {
                toggleMarker.removeClass('icon-chevron-right')
                toggleMarker.addClass('icon-chevron-down')
            } else {
                toggleMarker.removeClass('icon-chevron-down')
                toggleMarker.addClass('icon-chevron-right')
            }

            $(".guardian-content").toggle();
        });

        $("#invoice-control").on("click", function() {
            var toggleMarker = $("#invoice-marker");
            if(toggleMarker.hasClass('icon-chevron-right')) {
                toggleMarker.removeClass('icon-chevron-right')
                toggleMarker.addClass('icon-chevron-down')
            } else {
                toggleMarker.removeClass('icon-chevron-down')
                toggleMarker.addClass('icon-chevron-right')
            }

            $("#invoice-content").toggle();
        });


    });
</r:script>