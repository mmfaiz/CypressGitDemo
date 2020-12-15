<%@ page import="com.matchi.coupon.Coupon; org.joda.time.DateTime; com.matchi.User; com.matchi.Facility; com.matchi.facility.offers.CreateFacilityOfferCommand" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="offer.PromoCode.label"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="offer.PromoCode.label"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="button.edit.label"/> ${coupon?.name ?: cmd?.name}</li>
</ul>
<g:errorMessage bean="${cmd}"/>

<ul class="nav nav-tabs">
    <li class="active">
        <g:link action="edit" id="${coupon?.id ?: cmd?.couponId}"><g:message code="button.edit.label"/></g:link>
    </li>
    <li><g:link mapping="PromoCodeConditions" controller="facilityCouponCondition" action="list" id="${coupon?.id ?: cmd?.couponId}"><g:message code="default.terms.label"/></g:link></li>
</ul>


<g:form action="update" name="memberForm" class="form-horizontal form-well">
    <g:hiddenField name="couponId" id="couponId" value="${cmd?.couponId ?: coupon?.id}"/>
    <div class="form-header">
        <g:message code="facilityCoupon.edit.message14"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group ${hasErrors(bean: cmd, field:'name', 'error')}">
            <label class="control-label" for="name"><g:message code="coupon.name.label" default="Name" />*</label>
            <div class="controls">
                <g:textField name="name" value="${cmd?.name ?: coupon?.name }" class="span8"/>
            </div>
        </div>
        <div class="control-group ${hasErrors(bean: cmd, field:'code', 'error')}">
            <label class="control-label" for="code"><g:message code="coupon.promocode.label" default="Promo Code" />*</label>
            <div class="controls">
                <g:textField name="code" value="${cmd?.code ?: coupon?.code }" class="span8"/>
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
                        <g:field type="number" min="1" max="100" name="discountPercent" value="${cmd?.discountPercent ?: coupon?.discountPercent}"
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
                        <g:field type="number" min="${amountStep}" step="${amountStep}" name="discountAmount" value="${cmd?.discountAmount ?: coupon?.discountAmount}"
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
                                     value="${formatDate(date: cmd?.startDate ?: coupon?.startDate, formatName: 'date.format.dateOnly')}"/>
                    </li>
                    <li>
                        <label style="font-weight: bold"  for="endDate"><g:message code="offer.endDate.label"/>*</label>
                    </li>
                    <li>
                        <g:textField name="endDate" class="span2 center-text" readonly="true"
                                     value="${formatDate(date: cmd?.endDate ?: coupon?.endDate, formatName: 'date.format.dateOnly')}"/>
                    </li>
                </ul>
            </div>

        <div class="form-actions">
            <g:submitButton name="submit" value="${message(code: "button.save.label", default: "Spara")}" class="btn btn-success"/>
            <g:if test="${coupon?.customerCoupons?.size() == 0}">
                <g:actionSubmit action="delete" name="delete" value="${message(code: "button.delete.label", default: "Ta bort")}" class="btn btn-danger"/>
            </g:if>
            <g:link mapping="${params.type}" action="index" class="btn btn-inverse"><g:message code="button.cancel.label" default="Avbryt" /></g:link>
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
        }).filter("[value=${g.forJavaScript(data: cmd?.discountPercent ?: coupon?.discountPercent ? 'discountPercent' : 'discountAmount')}]")
                .prop("checked", true).trigger("change");
    });

    $(document).ready(function () {
        $("#memberForm").preventDoubleSubmission({});
    });
</r:script>
</body>
</html>
