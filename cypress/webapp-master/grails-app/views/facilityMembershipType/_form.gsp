<%@ page import="com.matchi.membership.TimeUnit; com.matchi.FacilityProperty.FacilityPropertyKey" %>

<div class="control-group ${hasErrors(bean:type, field:'name', 'error')}">
    <label class="control-label" for="name">
        <g:message code="membershipType.name.label" default="Namn" /> <g:inputHelp title="${message(code: 'default.mandatoryField.label')}"/>
    </label>
    <div class="controls">
        <g:textField name="name" value="${type?.name}" class="span8"/>
    </div>
</div>
<div class="control-group ${hasErrors(bean:type, field:'description', 'error')}">
    <label class="control-label" for="description"><g:message code="membershipType.description.label" default="Beskrivning" /></label>
    <div class="controls">
        <g:textArea rows="5" cols="30" name="description" value="${type?.description}" class="span8"/>
    </div>
</div>
<div class="control-group ${hasErrors(bean:type, field:'price', 'error')}">
    <label class="control-label" for="price"><g:message code="membershipType.price.label" default="Pris" /></label>
    <div class="controls">
        <g:textField name="price" value="${type?.price}" class="span1"/> <g:currentFacilityCurrency facility="${type?.facility}"/>
    </div>
</div>

<div class="control-group ${hasErrors(bean: type, field:'paidOnRenewal', 'error')}">
    <label class="control-label" for="paidOnRenewal">
        <g:if test="${getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_RECURRING_MEMBERSHIP)}">
            <g:message code="membershipType.paidOnRenewal.recurring.label"/>
            <g:inputHelp title="${message(code: 'membershipType.paidOnRenewal.recurring.hint')}"/>
        </g:if>
        <g:else>
            <g:message code="membershipType.paidOnRenewal.label"/>
            <g:inputHelp title="${message(code: 'membershipType.paidOnRenewal.hint')}"/>
        </g:else>
    </label>
    <div class="controls">
        <label class="checkbox">
            <g:checkBox name="paidOnRenewal" value="${type?.paidOnRenewal}"/>
        </label>
    </div>
</div>

<g:if test="${getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_RECURRING_MEMBERSHIP)}">
    <div class="control-group ${hasErrors(bean:type, field:'renewalStartingGraceNrOfDays', 'error')} starting-grace-period-wrap">
        <label class="control-label" for="renewalStartingGraceNrOfDays">
            <g:message code="membershipType.renewalStartingGraceNrOfDays.label"/>
        </label>
        <div class="controls">
            <g:field type="number" name="renewalStartingGraceNrOfDays"
                    value="${type?.renewalStartingGraceNrOfDays}" min="0" class="span1"/>
            <g:message code="membershipType.renewalStartingGraceNrOfDays.unit"/>
        </div>
    </div>

    <r:script>
        $(function() {
            $("#paidOnRenewal").on("change", function() {
                if ($(this).is(":checked")) {
                    $(".starting-grace-period-wrap").show();
                } else {
                    $(".starting-grace-period-wrap").hide().find(":input").val("");
                }
            }).trigger("change");
        });
    </r:script>
</g:if>

<g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT.name()}">
    <div class="control-group ${hasErrors(bean: type, field:'availableOnline', 'error')}">
        <label class="control-label" for="availableOnline"><g:message code="membershipType.availableOnline.label"/></label>
        <div class="controls">
            <label class="checkbox">
                <g:checkBox name="availableOnline" value="${type ? type.availableOnline : true}"/>
            </label>
        </div>
    </div>
</g:ifFacilityPropertyEnabled>
<div class="control-group ${hasErrors(bean: type, field:'organizationType', 'error')}">
    <label class="control-label" for="organizationType"><g:message code="membershipType.organizationType.label"/></label>
    <div class="controls">
        <label class="checkbox">
            <g:checkBox name="organizationType" value="${type?.organizationType}"/>
        </label>
    </div>
</div>

<hr/>

<div class="control-group">
    <label class="control-label" for="overrideSettings">
        <g:message code="membershipType.overrideSettings.label"/>
    </label>
    <div class="controls">
        <label class="checkbox">
            <g:checkBox name="overrideSettings"
                    value="${type?.validTimeAmount || type?.startDateYearly || type?.purchaseDaysInAdvanceYearly || (getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP) && type?.validTimeUnit && type?.validTimeAmount)}"/>
        </label>
    </div>
</div>

<div class="control-group validity-settings ${hasErrors(bean:type, field:'validTimeUnit', 'error')}">
    <label class="control-label" for="validTimeAmount">
        <g:message code="membershipType.validTimeAmount.label"/>
        <g:if test="${!getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP)}">
            <g:message code="membershipType.validTimeAmount.years.label"/>
        </g:if>
    </label>
    <div class="controls">
        <g:field type="number" name="validTimeAmount" min="1" class="span2"
                value="${type?.validTimeAmount}"/>
        <g:if test="${getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP)}">
            <g:select from="${TimeUnit.availableUnits()}" name="validTimeUnit" class="span2"
                    value="${type?.validTimeUnit}" valueMessagePrefix="timeUnit" noSelection="${['': '']}"/>
        </g:if>
        <g:else>
            <g:hiddenField name="validTimeUnit" value="${TimeUnit.YEAR.name()}"/>
        </g:else>
        <p class="help-block">
            <g:message code="membershipType.validTimeAmount.description"
                    args="[getUserFacility().membershipValidTimeAmount,
                            message(code: 'timeUnit.' + getUserFacility().membershipValidTimeUnit)]"/>
        </p>
    </div>
</div>
<div class="control-group ${getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP) ? 'yearly-membership-settings' : 'validity-settings'} ${hasErrors(bean:type, field:'startDateYearly', 'error')}">
    <label class="control-label" for="startDateYearly">
        <g:message code="membershipType.startDateYearly.label"/>
    </label>
    <div class="controls">
        <g:textField name="startDateYearly" class="span2 center-text"
                     value="${formatDate(date: type?.startDateYearly?.toDate(), formatName: 'date.format.readable')}"/>
        <p class="help-block">
            <g:message code="membershipType.startDateYearly.description1"/>
            <g:if test="${getUserFacility().yearlyMembershipStartDate}">
                <g:message code="membershipType.startDateYearly.description2"
                        args="[getUserFacility().yearlyMembershipStartDate.toDate()]"/>
            </g:if>
            <g:else>
                <g:message code="membershipType.startDateYearly.description3"/>
            </g:else>
        </p>
    </div>
</div>
<div class="control-group ${getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP) ? 'yearly-membership-settings' : 'validity-settings'} ${hasErrors(bean:type, field:'purchaseDaysInAdvanceYearly', 'error')}">
    <label class="control-label" for="purchaseDaysInAdvanceYearly">
        <g:message code="membershipType.purchaseDaysInAdvanceYearly.label"/>
    </label>
    <div class="controls">
        <g:field type="number" name="purchaseDaysInAdvanceYearly" min="1" max="365" class="span2"
               value="${type?.purchaseDaysInAdvanceYearly}"/>
        <p class="help-block">
            <g:message code="membershipType.purchaseDaysInAdvanceYearly.description1"/>
            <g:if test="${getUserFacility().yearlyMembershipPurchaseDaysInAdvance}">
                <g:message code="membershipType.purchaseDaysInAdvanceYearly.description2"
                        args="[getUserFacility().yearlyMembershipPurchaseDaysInAdvance]"/>
            </g:if>
            <g:else>
                <g:message code="membershipType.purchaseDaysInAdvanceYearly.description3"/>
            </g:else>
        </p>
    </div>
</div>

<g:if test="${facility?.isMasterFacility() && memberFacilities?.size() > 0}">
    <div class="control-group ${hasErrors(bean:type, field:'groupedSubFacility', 'error')}">
        <label class="control-label" for="groupedSubFacility"><g:message code="membershipType.groupedSubFacility.label" /></label>
        <div class="controls">
            <g:select from="${memberFacilities}" name="groupedSubFacility" class="span2"
                      value="${type?.groupedSubFacility?.id}" optionKey="id" optionValue="name" valueMessagePrefix="timeUnit" noSelection="${['': '']}"/>
            <p class="help-block"><g:message code="membershipType.groupedSubFacility.description" /></p>
        </div>
    </div>
</g:if>

<r:script>
    $(function() {
        $("#startDateYearly").datepicker({
            autoSize: true,
            dateFormat: 'dd MM'
        });

        <g:if test="${getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP)}">
        $("#validTimeUnit").on("change", function() {
            if ($("#overrideSettings").is(":checked") && ($(this).val() == "${TimeUnit.YEAR}"
                    ${getUserFacility().membershipValidTimeUnit == TimeUnit.YEAR ? '|| !$(this).val()' : ''})) {
                $(".yearly-membership-settings").show();
            } else {
                $(".yearly-membership-settings").hide();
                $(".yearly-membership-settings").find(":input").val("");
            }
        }).trigger("change");
        </g:if>

        $("#overrideSettings").on("change", function() {
            if ($(this).is(":checked")) {
                $(".validity-settings").show();
                $("#validTimeUnit").trigger("change");
            } else {
                $(".validity-settings").hide();
                $(".validity-settings").find(":input").val("");
                $("#validTimeUnit").trigger("change");
            }
        }).trigger("change");
    });
</r:script>