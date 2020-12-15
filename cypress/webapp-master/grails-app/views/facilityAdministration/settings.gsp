<%@ page import="com.matchi.FacilityProperty; com.matchi.enums.MembershipRequestSetting; com.matchi.enums.RedeemAt; org.joda.time.DateTime; com.matchi.Facility; com.matchi.membership.TimeUnit; com.matchi.FacilityProperty.FacilityPropertyKey;" %>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="adminHome.index.title"/></title>
    <r:require modules="select2,matchi-customerselect,jquery-chosen,bootstrap-wysihtml5"/>
</head>
<body>
<ul class="breadcrumb">
    <li>
        <g:link action="index"><g:message code="facilityAdministration.index.title"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="adminFacility.adminFacilityMenu.settings"/></li>
</ul>

<g:errorMessage bean="${facility}"/>

<ul class="nav nav-tabs">
    <li><g:link action="index"><g:message code="facility.label2"/></g:link></li>
    <li class="active">
        <g:link action="settings"><g:message code="adminFacility.adminFacilityMenu.settings"/></g:link>
    </li>
    <!--
    <li>
        <g:link controller="facilityMessage" action="index"><g:message code="facilityAdministration.settings.message25"/></g:link>
    </li>
    -->
    <g:if test="${facility.hasMPC()}">
        <li>
            <g:link controller="facilityControlSystems" action="index"><g:message code="facilityControlSystems.label"/></g:link>
        </li>
    </g:if>
    <li>
        <g:link controller="facilityAccessCode" action="index"><g:message code="facilityAccessCode.label.plural"/></g:link>
    </li>
</ul>

<g:form action="saveSettings" method="post" class="form-horizontal form-well">
    <g:hiddenField name="id" value="${facility?.id}" />
    <g:hiddenField name="version" value="${facility?.version}" />

    <div class="form-header">
        <g:message code="facilityAdministration.settings.message26"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group">
            <label class="control-label" for="bookingNotificationNote"><g:message code="facilityAdministration.settings.message8"/></label>
            <div class="controls">
                <g:textArea rows="5" cols="40" name="bookingNotificationNote" class="styled span8" value="${facility?.bookingNotificationNote}" placeholder="${message(code: 'facilityAdministration.settings.message27')}" maxlength="1000"/>
                <p class="help-block"><g:message code="facilityAdministration.settings.message9"/></p>
            </div>

        </div>
        <div class="control-group">
            <label class="control-label" for="defaultBookingCustomerId"><g:message code="facilityAdministration.settings.message10"/></label>
            <div class="controls">
                <input type="hidden" id="defaultBookingCustomerId" name="defaultBookingCustomerId" value="${facility?.defaultBookingCustomer?.id}"/>
                <p class="help-block"><g:message code="facilityAdministration.settings.message11"/></p>
            </div>
        </div>
        <g:if test="${facility?.getFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER)}">
            <div class="control-group">
                <g:hiddenField name="bookingRestrictionsEnabled" value="true"/>
                <label class="control-label" for="maxBookingsFeature">
                    <g:message code="facilityAdministration.settings.message5"/>
                </label>
                <div class="controls">
                    <ul class="inline list-inline no-bottom-margin">
                        <li class="no-padding"><g:checkBox name="maxBookingsFeature" value="${facility?.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name())}"/></li>
                        <li><g:textField name="maxBookings" maxlength="5" class="span2"
                                value="${facility?.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name())}"/></li>
                    </ul>
                    <p class="help-block"><g:message code="facilityAdministration.settings.message6"/></p>
                </div>
            </div>
        </g:if>
        <div class="control-group">
            <label class="control-label" for="defaultBookingCustomerId"><g:message code="facility.whetherToSendEmailConfirmationByDefault.label"/></label>
            <div class="controls">
                <g:checkBox id="whetherToSendEmailConfirmationByDefault" name="whetherToSendEmailConfirmationByDefault" value="${facility.whetherToSendEmailConfirmationByDefault}"/>
            </div>
        </div>
        <hr>
        <div class="control-group" ${hasErrors(bean: facility, properties: "membershipRequestEmail")}>
            <label class="control-label" for="receiveMembershipRequests"><g:message code="facilityAdministration.settings.message12"/></label>
            <div class="controls">
                <label class="checkbox">
                    <g:checkBox id="receiveMembershipRequests" name="receiveMembershipRequests" value="${facility.recieveMembershipRequests}" onclick="\$('#membershipRequestSettings').toggle();"/>
                    <g:message code="facilityAdministration.settings.message28"/>
                </label>
                <div id="membershipRequestSettings" class="left-margin20" style="display: ${facility.recieveMembershipRequests ? "inline-block":"none"}">
                    <g:if test="${facility.getFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT)}">
                        <label class="checkbox" for="useFamilyMembersFeature">
                            <g:checkBox name="useFamilyMembersFeature"
                                    value="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_USE_FAMILY_MEMBERSHIPS.name())}"/>
                            <g:message code="facilityAdministration.settings.familyMembership"/>
                        </label>
                    </g:if>
                    <g:else>
                        <g:each in="${MembershipRequestSetting.list()}" status="i" var="setting">
                            <label class="radio">
                                <input type="radio" name="membershipRequestSetting" value="${setting}" ${!facility.membershipRequestSetting ? (i == 0 ? "checked" : "") : (setting == facility.membershipRequestSetting ? "checked" : "")}/>
                                <g:message code="membership.request.setting.${setting}"/>
                            </label>
                            <g:if test="${setting == MembershipRequestSetting.DIRECT}">
                                <div style="display: none" class="left-margin20 starting-grace-period">
                                    <label for="membershipStartingGraceNrOfDays" class="" style="display: inline-block">
                                        <g:message code="facility.membershipStartingGraceNrOfDays.label"/>
                                    </label>
                                    <g:field type="number" name="membershipStartingGraceNrOfDays" class="span2 left-margin5"
                                            value="${facility.membershipStartingGraceNrOfDays}" min="0"/>
                                </div>
                            </g:if>
                        </g:each>

                        <r:script>
                            $(function() {
                                $("input[type=radio][name=membershipRequestSetting]").on("change", function() {
                                    var inputWrapper = $(".starting-grace-period");
                                    if ($(this).val() == "${MembershipRequestSetting.DIRECT.name()}") {
                                        inputWrapper.show().find("input").val(
                                                ${facility.membershipRequestSetting == MembershipRequestSetting.DIRECT ? facility.membershipStartingGraceNrOfDays : Facility.DEFAULT_STARTING_GRACE_PERIOD});
                                    } else {
                                        inputWrapper.find("input").val("");
                                        inputWrapper.hide();
                                    }
                                });
                                $("input[type=radio][name=membershipRequestSetting]:checked").trigger("change");
                            });
                        </r:script>
                    </g:else>
                    <div class="space5"></div>
                    <g:textField name="membershipRequestEmail" class="span6" value="${facility.membershipRequestEmail}" placeholder="${message(code: 'facilityAdministration.settings.message29')}" maxlength="255"/>
                    <p class="help-block"><g:message code="facilityAdministration.settings.message13"/></p>
                    <div class="space15"></div>
                    <g:textArea name="membershipRequestDescription" rows="5" cols="40" class="styled span8" value="${g.toRichHTML(text: facility.membershipRequestDescription)}" placeholder="${message(code: 'facilityAdministration.settings.message30')}" maxlength="1000"/>
                    <p class="help-block"><g:message code="facilityAdministration.settings.message14"/></p>
                </div>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="membershipValidTimeAmount">
                <g:message code="facility.membershipValidTimeAmount.label"/>
                <g:if test="${!facility?.isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP)}">
                    <g:message code="facility.membershipValidTimeAmount.years.label"/>
                </g:if>
            </label>
            <div class="controls">
                <ul class="inline list-inline">
                    <li>
                        <g:field type="number" name="membershipValidTimeAmount" min="1" class="span2"
                                value="${facility?.membershipValidTimeAmount}" required="required"/>
                    </li>
                    <g:if test="${facility?.isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP)}">
                        <li>
                            <g:select from="${TimeUnit.availableUnits()}" name="membershipValidTimeUnit" class="span2"
                                    value="${facility?.membershipValidTimeUnit}"
                                    valueMessagePrefix="timeUnit"/>
                        </li>
                    </g:if>
                    <g:else>
                        <g:hiddenField name="membershipValidTimeUnit" value="${TimeUnit.YEAR.name()}"/>
                    </g:else>
                </ul>
                <p class="help-block"><g:message code="facility.membershipValidTimeAmount.description"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="membershipGraceNrOfDays">
                <g:message code="facility.membershipGraceNrOfDays.label"/>
            </label>
            <div class="controls">
                <g:field type="number" name="membershipGraceNrOfDays" min="0" class="span2"
                        value="${facility?.membershipGraceNrOfDays}" required="required"/>
                <p class="help-block"><g:message code="facility.membershipGraceNrOfDays.description"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="yearlyMembershipStartDate">
                <g:message code="facility.yearlyMembershipStartDate.label"/>
            </label>
            <div class="controls">
                <g:textField name="yearlyMembershipStartDate" class="span2 center-text"
                             value="${formatDate(date: facility?.yearlyMembershipStartDate?.toDate(), formatName: 'date.format.readable')}"/>
                <p class="help-block"><g:message code="facility.yearlyMembershipStartDate.description"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="yearlyMembershipPurchaseDaysInAdvance">
                <g:message code="facility.yearlyMembershipPurchaseDaysInAdvance.label"/>
            </label>
            <div class="controls">
                <g:field type="number" name="yearlyMembershipPurchaseDaysInAdvance" min="1" max="365" class="span2"
                       value="${facility?.yearlyMembershipPurchaseDaysInAdvance}"/>
                <p class="help-block"><g:message code="facility.yearlyMembershipPurchaseDaysInAdvance.description"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="membershipMaxAmount">
                <g:message code="facilityAdministration.settings.message7"/>
            </label>
            <div class="controls">
                <g:textField name="membershipMaxAmount" class="span2" maxlength="18"
                             value="${facility?.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT.name())}"/>
            </div>
        </div>
        <hr>
        <div class="control-group" ${hasErrors(bean: facility, properties: "subscriptionRedeem")}>
            <label class="control-label" for="subscriptionRedeem"><g:message code="facility.subscriptionRedeem.label" default="Återbetalning abonnemang" /></label>
            <div class="controls">
                <label class="checkbox">
                    <g:checkBox name="subscriptionRedeem" value="${facility?.subscriptionRedeem}" onclick="\$('#subscriptionRedeemSettings').toggle();"/>
                    <g:message code="facilityAdministration.settings.message31"/> <g:inputHelp title="${message(code: 'facilityAdministration.settings.message32')}"/>
                </label>
                <div id="subscriptionRedeemSettings" class="left-margin20" style="display: ${facility.subscriptionRedeem ? "inline-block":"none"}">
                    <g:each in="${RedeemAt.list()}" status="i" var="redeemAt">
                        <label class="radio">
                            <input type="radio" name="redeemAt" value="${redeemAt}"
                                ${!facility.subscriptionRedeem ? (i == 0 ? "checked" : "") : (redeemAt.equals(facility.subscriptionRedeem.redeemAt) ? "checked" : "")}/>
                            <g:message code="subscriptionRedeem.redeemAt.${redeemAt}"/> <g:inputHelp title="${message(code: "subscriptionRedeem.redeemAt.${redeemAt}.help")}"/>
                        </label>
                    </g:each>
                    <div class="space5"></div>
                    <g:each in="${redeemStrategies}" status="i" var="strategy">
                        <label class="radio">
                            <input type="radio" name="strategy" value="${strategy?.type}" onclick="showStrategy(this);"
                                ${!facility.subscriptionRedeem ? "" : (strategy?.type.equals(facility.subscriptionRedeem?.strategy?.type) ? "checked" : "")}/>
                            <g:message code="redeem.strategy.${strategy?.type}"/> <g:inputHelp title="${message(code: "redeem.strategy.${strategy?.type}.help")}" />
                        </label>
                        <div class="strategy-setting" style="display: none">
                            <g:strategyForm strategy="${strategy}" facility="${facility}" articles="${articles?.redeemInvoiceRowArticles}"/>
                        </div>
                    </g:each>
                </div>
            </div>
        </div>
        <g:if test="${facility.hasApplicationInvoice()}">
            <div class="control-group" ${hasErrors(bean: facility, properties: "bookingInvoiceRowExternalArticleId")} ${hasErrors(bean: facility, properties: "bookingInvoiceRowDescription")}>
                <label class="control-label"><g:message code="facilityAdministration.settings.message17"/></label>
                <div class="controls">
                    <g:render template="/templates/facility/listOrganizations"
                              model="${[facility: facility, name: 'bookingInvoiceRowOrganizationId', organizations: organizations, currentOrganizationId: facility.bookingInvoiceRowOrganizationId]}"/>
                    <ul class="inline list-inline no-bottom-margin">
                        <li>
                            <select name="bookingInvoiceRowExternalArticleId">
                                <option value=""><g:message code="default.article.multiselect.noneSelectedText"/></option>
                                <g:each in="${articles?.bookingInvoiceRowArticles}">
                                    <option value="${it.id}" ${facility?.bookingInvoiceRowExternalArticleId == it.id ? "selected":""}>${it.descr}</option>
                                </g:each>
                            </select>
                        </li>
                        <li>
                            <g:textField name="bookingInvoiceRowDescription" class="span5" value="${facility?.bookingInvoiceRowDescription}" placeholder="${message(code: 'facility.bookingInvoiceRowDescription.label')}" maxlength="255"/>
                        </li>
                    </ul>
                    <p class="help-block"><g:message code="facilityAdministration.settings.message19"/></p>
                    <div class="space5"></div>
                </div>
            </div>
            <div class="control-group" ${hasErrors(bean: facility, properties: "useInvoiceFees")} ${hasErrors(bean: facility, properties: "invoiceFeeArticles")}
                ${hasErrors(bean: facility, properties: "invoiceFeeExternalArticleId")} ${hasErrors(bean: facility, properties: "invoiceFeeDescription")}>
                <label class="control-label" for="useInvoiceFees"><g:message code="facility.useInvoiceFees.label" /></label>
                <div class="controls">
                    <label class="checkbox">
                        <g:checkBox name="useInvoiceFees" value="${facility?.useInvoiceFees}" onclick="\$('#invoiceFeeSettings').toggle();"/>
                        <g:message code="facilityAdministration.settings.message34"/> <g:inputHelp title="${message(code: 'facilityAdministration.settings.message35')}"/>
                    </label>
                    <div id="invoiceFeeSettings" class="left-margin20" style="display: ${facility.useInvoiceFees ? "inline-block":"none"}">
                        <div class="control-group">
                            <select id="invoiceFeeArticles" name="invoiceFeeArticles"
                                    optionKey="id" optionValue="descr" data-placeholder="${message(code: 'facilityAdministration.settings.message36')}" style="width:550px;" multiple class="chzn-select">
                                <option value="0" ${facility?.invoiceFeeArticles?.toList()?.contains("0")?"selected":""}><g:message code="facilityAdministration.settings.message20"/></option>
                                <g:each in="${articles?.invoiceFeeArticles}">
                                    <option value="${it.id}" ${facility?.invoiceFeeArticles?.contains(it.id)?"selected":""}>${it.descr}</option>
                                </g:each>
                            </select>
                            <p class="help-block"><g:message code="facilityAdministration.settings.message21"/></p>
                        </div>
                        <div class="space10"></div>
                        <g:render template="/templates/facility/listOrganizations"
                                  model="${[facility: facility, name: 'invoiceFeeOrganizationId', organizations: organizations, currentOrganizationId: facility.invoiceFeeOrganizationId]}"/>
                        <ul class="inline list-inline no-bottom-margin">
                            <li>
                                <select name="invoiceFeeExternalArticleId">
                                    <option value=""><g:message code="default.article.multiselect.noneSelectedText"/></option>
                                    <g:each in="${articles?.invoiceFeeExternalArticles}">
                                        <option value="${it.id}" ${facility?.invoiceFeeExternalArticleId == it.id ? "selected":""}>${it.descr}</option>
                                    </g:each>
                                </select>
                            </li>
                            <li>
                                <g:textField name="invoiceFeeDescription" class="span3" value="${facility?.invoiceFeeDescription}" placeholder="${message(code: 'facility.invoiceFeeDescription.label')}" maxlength="1000"/>
                            </li>
                            <li>
                                <g:textField name="invoiceFeeAmount" class="span1" value="${facility?.invoiceFeeAmount}" placeholder="${message(code: 'default.price.label')}" maxlength="18"/> <g:currentFacilityCurrency facility="${facility}"/>
                            </li>
                        </ul>
                        <p class="help-block"><g:message code="facilityAdministration.settings.message23"/></p>
                        <div class="space5"></div>
                    </div>
                </div>
            </div>
        </g:if>
        <hr>
        <div class="control-group">
            <label class="control-label" for="showBookingHolder"><g:message code="facility.showBookingHolder.label"/></label>
            <div class="controls">
                <label class="checkbox">
                    <g:checkBox name="showBookingHolder" value="${facility.showBookingHolder}"/>
                    <g:message code="facility.showBookingHolder.description"/>
                </label>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="isAllCourtsTabDefault">
                <g:message code="facility.isAllCourtsTabDefault.label"/>
            </label>
            <div class="controls">
                <label class="checkbox">
                    <g:checkBox name="isAllCourtsTabDefault" value="${facility.isAllCourtsTabDefault}"/>
                    <g:message code="facility.isAllCourtsTabDefault.description"/>
                </label>
            </div>
        </div>
        <hr>
        <div class="control-group">
            <label class="control-label" for="googleTagManagerContainerId">
                <g:message code="facilityAdministration.settings.googleTagManager"/>
            </label>
            <div class="controls">
                <g:textField name="googleTagManagerContainerId" class="span2" maxlength="30"
                             value="${facility?.googleTagManagerContainerId}"/>
                <p class="help-block"><g:message code="facilityAdministration.settings.googleTagManager.description"/></p>
            </div>
        </div>

        <div class="form-actions">
            <g:submitButton name="submit" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <a href="javascript:history.go(-1)" class="btn btn-danger"><g:message code="button.back.label"/></a>
        </div>
    </fieldset>
</g:form>
<r:script>
    $(document).ready(function() {
        $("[rel=tooltip]").tooltip();

        $("#defaultBookingCustomerId").matchiCustomerSelect({width:'250px', onchange: selectCustomer});

        $("#invoiceFeeArticles").chosen({});

        $("#TRAINING_REQUEST_DESCRIPTION_TEXT").wysihtml5({
            "image": false,
            parserRules: wysihtml5ParserRules,
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });

        $("#membershipRequestDescription").wysihtml5({
            "image": false,
            parserRules: wysihtml5ParserRules,
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });

        $("#maxBookingsFeature").change(function() {
            $("#maxBookings").prop("disabled", !$(this).is(":checked"));
        }).trigger("change");

        showStrategy($("input[name=strategy]:checked").get(0));

        $("#yearlyMembershipStartDate").datepicker({
            autoSize: true,
            dateFormat: 'dd MM'
        });
    });
    function showStrategy(origin) {
        $(".strategy-setting").hide().find(":input").prop("disabled", true);
        var showBlock = $(origin).parent().next();
        showBlock.find(":input").prop("disabled", false);
        showBlock.show();
    }
    function selectCustomer(customer) {
        if(!customer || !customer.id) {
            $("#defaultBookingCustomerId").val("");
        }
    }
</r:script>
</body>
</html>