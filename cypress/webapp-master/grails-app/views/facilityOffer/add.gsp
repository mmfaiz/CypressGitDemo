<%@ page import="com.matchi.facility.offers.CreateFacilityOfferCommand; com.matchi.coupon.Coupon; org.joda.time.DateTime; com.matchi.User; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="offer.${params.type}.label"/></title>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link mapping="${params.type}" action="index"><g:message code="offer.${params.type}.label"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="offer.${params.type}.create"/></li>
</ul>
<g:errorMessage bean="${cmd}"/>


<g:form action="save" mapping="${params.type}" name="memberForm" class="form-horizontal form-well">
    <div class="form-header">
        <g:message code="offer.${params.type}.add.label"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group ${hasErrors(bean: cmd, field:'name', 'error')}">
            <label class="control-label" for="name"><g:message code="coupon.name.label" default="Namn" />*</label>
            <div class="controls">
                <g:textField name="name" value="${ cmd?.name }" class="span8"/>
            </div>
        </div>
        <div class="control-group ${hasErrors(bean: cmd, field:'description', 'error')}">
            <label class="control-label" for="description"><g:message code="coupon.description.label" default="Beskrivning" /></label>
            <div class="controls">
                <g:textArea rows="10" cols="50" name="description" value="${ cmd?.description }" class="span8"/>
                <p class="help-block"><g:message code="coupon.description.hint"/></p>
            </div>
        </div>
        <div class="control-group ${hasErrors(bean: cmd, field:'nrOfTickets', 'error')} ${hasErrors(bean: cmd, field:'unlimited', 'error')}">
            <label class="control-label" for="nrOfTickets">
              <g:message code="offer.${params.type}.nrOfTickets.label" default="Antal bokningar" />*
              <g:if test="${params.type == com.matchi.facility.offers.CreateFacilityOfferCommand.Type.GiftCard.name()}">
                <g:inputHelp title="${message(code: "offer.${params.type}.nrOfTickets.help", args: [currentFacilityCurrency()])}"/>
              </g:if>
            </label>
            <div class="controls">
                <g:textField name="nrOfTickets" value="${ cmd?.nrOfTickets }" class="span1 center-text"/>

                <g:if test="${params.type == CreateFacilityOfferCommand.Type.Coupon.name()}">
                    <label class="checkbox inline left-margin10">
                        <g:checkBox name="unlimited" id="unlimited" value="${cmd?.unlimited}" onclick="toggleDisableNrOfTickets();"/><g:message code="facilityCoupon.add.message11"/>
                    </label>
                </g:if>
            </div>
        </div>

        <div class="control-group ${hasErrors(bean: cmd, field:'nrOfDaysValid', 'error')}">
            <label class="control-label">
                <g:message code="offer.validity.label"/>
            </label>
            <div class="controls validity">
                <ul class="inline no-bottom-margin">
                    <li>
                        <label class="radio">
                            <input type="radio" name="validity" value="nrOfDaysValid"/>
                            <g:message code="coupon.nrOfDaysValid.label" default="Giltighetstid" />
                        </label>
                    </li>
                    <li>
                        <g:textField name="nrOfDaysValid" value="${cmd ? cmd.nrOfDaysValid : 365}"
                                class="span1 center-text"/>
                    </li>
                </ul>
                <p class="help-block"><g:message code="coupon.nrOfDaysValid.hint"/></p>

                <ul class="inline no-bottom-margin">
                    <li>
                        <label class="radio">
                            <input type="radio" name="validity" value="endDate"/>
                            <g:message code="offer.endDate.label"/>
                        </label>
                    </li>
                    <li>
                        <g:textField name="endDate" class="span2 center-text" readonly="true"
                                value="${formatDate(date: cmd?.endDate, formatName: 'date.format.dateOnly')}"/>
                    </li>
                </ul>
                <p class="help-block"><g:message code="offer.endDate.hint"/></p>
            </div>
        </div>

        <g:if test="${params.type == com.matchi.facility.offers.CreateFacilityOfferCommand.Type.Coupon.name()}">
            <div class="control-group" ${hasErrors(bean: cmd, field: 'conditionPeriod', 'error')}>
                <label class="control-label" for="periodConditionNrOfBookings"><g:message
                    code="facilityCoupon.add.maxUpcomingBookings"/></label>

                <div class="controls">
                    <ul class="inline">
                        <li style="padding-left: 0;">
                            <input class="span1" type="text" name="nrOfBookingsInPeriod"
                                   id="periodConditionNrOfBookings" value="${cmd?.nrOfBookingsInPeriod}"
                                   style="text-align: right;"/>
                        </li>
                        <li>
                            <g:message code="facilityCoupon.add.message8"/>
                        </li>
                        <li>
                            <input class="span1" type="text" name="nrOfPeriods" id="periodConditionNrOfPeriods"
                                   value="${cmd?.nrOfPeriods}" style="text-align: right;"/>
                        </li>
                        <li>
                            <select id="periodConditionPeriod" name="conditionPeriod" class="span2">
                                <g:each in="${Coupon.ConditionPeriod.list()}">
                                    <option value="${it}" ${cmd?.conditionPeriod?.equals(it) ? "selected" : ""}>
                                        <g:message code="coupon.conditionPeriod.${it}"/>
                                    </option>
                                </g:each>
                            </select>
                        </li>
                    </ul>
                    <label class="checkbox">
                        <g:checkBox name="totalBookingsInPeriod" id="totalBookingsInPeriod"
                                    value="${cmd?.totalBookingsInPeriod}"/><g:message
                            code="coupon.totalBookingsInPeriod.label"/>
                    </label>

                    <p class="help-block">
                        <g:message code="facilityCoupon.add.message13"/>
                    </p>
                </div>
            </div>
        </g:if>

        <div class="control-group ${hasErrors(bean: cmd, field:'availableOnline', 'error')}">
            <label class="control-label" for="availableOnline"><g:message code="coupon.availableOnline.label" default="Tillgängligt online" /></label>
            <div class="controls">
                <g:checkBox name="availableOnline" value="${ cmd?.availableOnline }"/>
            </div>
        </div>
        <div class="form-actions">
            <g:submitButton name="submit" value="${message(code: "button.save.label", default: "Spara")}" class="btn btn-success"/>
            <g:link mapping="${params.type}" action="index" class="btn btn-danger"><g:message code="button.cancel.label" default="Avbryt" /></g:link>
        </div>
    </fieldset>
    <g:hiddenField name="type" value="${params.type}"/>
</g:form>
<r:script>
    $(function() {
        $("[rel='tooltip']").tooltip();

        toggleDisableNrOfTickets();

        $("#endDate").datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}"
        });

        $(".validity").find(":radio").on("change", function() {
            $(".validity").find(":input:not(:radio)").prop("disabled", true).end()
                    .find(":input[name=" + $(this).val() + "]").prop("disabled", false);
        }).filter("[value=${g.forJavaScript(data: cmd?.endDate ? 'endDate' : 'nrOfDaysValid')}]")
                .prop("checked", true).trigger("change");
    });

    $("#name").focus();

    function toggleDisableNrOfTickets() {
        var $unlimited = $("#unlimited");
        var $nrOfTickets = $("#nrOfTickets");

        if($unlimited.is(":checked")) {
            $nrOfTickets.attr("disabled", "disabled");
        } else {
            if($nrOfTickets.attr("disabled")) { $nrOfTickets.removeAttr("disabled"); }
        }
    }

    $(document).ready(function () {
        $("#memberForm").preventDoubleSubmission({});
    });
</r:script>
</body>
</html>
