<%@ page import="com.matchi.facility.offers.CreateFacilityOfferCommand; com.matchi.coupon.Coupon; org.joda.time.DateTime; com.matchi.User; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="offer.PromoCode.label"/></title>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="offer.PromoCode.label"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="offer.PromoCode.create"/></li>
</ul>
<g:errorMessage bean="${cmd}"/>


<g:form action="save" name="memberForm" class="form-horizontal form-well">
    <div class="form-header">
        <g:message code="offer.PromoCode.add.label"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="alert alert-info padding10"><g:message code="offer.PromoCode.description.text"/></div>
        <div class="control-group ${hasErrors(bean: cmd, field:'name', 'error')}">
            <label class="control-label" for="name"><g:message code="coupon.name.label" default="Name" />*</label>
            <div class="controls">
                <g:textField name="name" value="${ cmd?.name }" class="span8"/>
            </div>
        </div>
        <div class="control-group ${hasErrors(bean: cmd, field:'code', 'error')}">
            <label class="control-label" for="code"><g:message code="coupon.promocode.label" default="Promo Code" />*</label>
            <div class="controls">
                <g:textField name="code" value="${ cmd?.code }" class="span8"/>
            </div>
        </div>

        <div class="control-group ${hasErrors(bean: cmd, field:'nrOfDaysValid', 'error')}">
            <label class="control-label">
                <g:message code="offer.discountType.label"/>*
            </label>
            <div class="controls discountType">
                <div class="control-group">
                    <div class="control-label">
                        <label class="radio" style="text-align: left">
                            <input type="radio" name="discountType" value="discountPercent"/>
                            <g:message code="offer.percentage.label" default="Percentage" />
                        </label>
                    </div>
                    <div class="controls">
                        <g:field type="number" min="1" max="100" name="discountPercent" value="${cmd?.discountPercent}"
                                 class="span1 center-text"/>%
                    </div>
                </div>

                <div class="control-group">
                    <div class="control-label">
                        <label class="radio" style="text-align: left">
                            <input type="radio" name="discountType" value="discountAmount"/>
                            <g:message code="offer.amount.label" default="Amount" />
                        </label>
                    </div>
                    <div class="controls">
                        <g:field type="number" min="${amountStep}" step="${amountStep}" name="discountAmount" value="${cmd?.discountAmount}"
                                 class="span1 center-text"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="control-group ${hasErrors(bean: cmd, field:'startDate', 'error')}">
            <label class="control-label" for="startDate"><g:message code="offer.startDate.label" default="Start date" />*</label>
            <div class="controls">
                <ul class="inline">
                    <li style="padding-left: 0;">
                        <g:textField name="startDate" class="span2 center-text" readonly="true"
                                     value="${formatDate(date: cmd?.startDate, formatName: 'date.format.dateOnly')}"/>
                    </li>
                    <li>
                        <label style="font-weight: bold"  for="endDate"><g:message code="offer.endDate.label"/>*</label>
                    </li>
                    <li>
                        <g:textField name="endDate" class="span2 center-text" readonly="true"
                                     value="${formatDate(date: cmd?.endDate, formatName: 'date.format.dateOnly')}"/>
                    </li>
                </ul>
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

        $("#startDate").datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}"
        });

        $("#endDate").datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}"
        });

        $("#startDate").on("change", function() {
            var date = $(this).val();
            $("#endDate").datepicker("option", "minDate", date);
            $("#endDate").trigger("change");
        }).trigger("change");

        $(".discountType").find(":radio").on("change", function() {
            $(".discountType").find(":input:not(:radio)").prop("disabled", true).end()
                    .find(":input[name=" + $(this).val() + "]").prop("disabled", false);
        }).filter("[value=${g.forJavaScript(data: cmd?.discountPercent ? 'discountPercent' : 'discountAmount')}]")
                .prop("checked", true).trigger("change");
    });

    $("#name").focus();


    $(document).ready(function () {
        $("#memberForm").preventDoubleSubmission({});
    });
</r:script>
</body>
</html>
