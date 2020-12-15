<%@ page import="com.matchi.FacilityContract" %>

<div class="form-group col-sm-12">
    <label for="name"><g:message code="facilityContract.name.label"/>*</label>
    <g:textField name="name" value="${contract.name}" class="form-control" required="required" maxlength="255" autofocus="autofocus"/>
</div>

<div class="form-group col-sm-12">
    <label for="description"><g:message code="facilityContract.description.label"/></label>
    <g:textArea name="description" rows="3" cols="30" value="${contract.description}" class="form-control"/>
</div>

<!-- COUPON -->
<div class="form-group col-sm-12">
    <div class="row">
        <div class="form-group col-sm-6">
            <label for="couponContractType"><g:message code="facilityContract.couponContractType.label"/></label>

            <div class="radio">
                <g:radio id="couponContractType_1"
                         name="couponContractType"
                         value="${FacilityContract.CouponContractType.PER_TICKET}"
                         checked="${contract.couponContractType == FacilityContract.CouponContractType.PER_TICKET}"/>
                <label for="couponContractType_1"><g:message code="facilityContract.couponContractType.PER_TICKET.label"/></label>
            </div>

            <div class="radio">
                <g:radio id="couponContractType_2"
                         name="couponContractType"
                         value="${FacilityContract.CouponContractType.PER_COUPON}"
                         checked="${contract.couponContractType == FacilityContract.CouponContractType.PER_COUPON}"/>
                <label for="couponContractType_2"><g:message code="facilityContract.couponContractType.PER_COUPON.label"/></label>
            </div>
        </div>

        <div class="form-group col-sm-6 ">
            <label for="variableCouponMediationFee"><g:message code="facilityContract.variableCouponMediationFee.label"/></label>
            <g:textField name="variableCouponMediationFee" class="form-control" value="${fieldValue(bean: contract, field: 'variableCouponMediationFee')}"/>
        </div>
    </div>
</div>

<!-- UNLIMITED COUPON -->
<div class="form-group col-sm-12">
    <div class="row">
        <div class="form-group col-sm-6">
            <label for="unlimitedCouponContractType"><g:message code="facilityContract.unlimitedCouponContractType.label"/></label>

            <div class="radio">
                <g:radio id="unlimitedCouponContractType_1"
                         name="unlimitedCouponContractType"
                         value="${FacilityContract.CouponContractType.PER_TICKET}"
                         checked="${contract.unlimitedCouponContractType == FacilityContract.CouponContractType.PER_TICKET}"/>
                <label for="unlimitedCouponContractType_1"><g:message code="facilityContract.couponContractType.PER_TICKET.label"/></label>
            </div>

            <div class="radio">
                <g:radio id="unlimitedCouponContractType_2"
                         name="unlimitedCouponContractType"
                         value="${FacilityContract.CouponContractType.PER_COUPON}"
                         checked="${contract.unlimitedCouponContractType == FacilityContract.CouponContractType.PER_COUPON}"/>
                <label for="unlimitedCouponContractType_2"><g:message code="facilityContract.couponContractType.PER_COUPON.label"/></label>
            </div>
        </div>

        <div class="form-group col-sm-6 ">
            <label for="variableUnlimitedCouponMediationFee"><g:message code="facilityContract.variableUnlimitedCouponMediationFee.label"/></label>
            <g:textField name="variableUnlimitedCouponMediationFee" class="form-control" value="${fieldValue(bean: contract, field: 'variableUnlimitedCouponMediationFee')}"/>
        </div>
    </div>
</div>

<!-- GIFT CARD -->
<div class="form-group col-sm-12">
    <div class="row">
        <div class="form-group col-sm-6">
            <label for="giftCardContractType"><g:message code="facilityContract.giftCardContractType.label"/></label>

            <div class="radio">
                <g:radio id="giftCardContractType_1"
                         name="giftCardContractType"
                         value="${FacilityContract.GiftCardContractType.PER_USE}"
                         checked="${contract.giftCardContractType == FacilityContract.GiftCardContractType.PER_USE}"/>
                <label for="giftCardContractType_1"><g:message code="facilityContract.giftCardContractType.PER_USE.label"/></label>
            </div>

            <div class="radio">
                <g:radio id="giftCardContractType_2"
                         name="giftCardContractType"
                         value="${FacilityContract.GiftCardContractType.PER_GIFT_CARD}"
                         checked="${contract.giftCardContractType == FacilityContract.GiftCardContractType.PER_GIFT_CARD}"/>
                <label for="giftCardContractType_2"><g:message code="facilityContract.giftCardContractType.PER_GIFT_CARD.label"/></label>
            </div>
        </div>

        <div class="form-group col-sm-6 ">
            <label for="variableGiftCardMediationFee"><g:message code="facilityContract.variableGiftCardMediationFee.label"/></label>
            <g:textField name="variableGiftCardMediationFee" class="form-control" value="${fieldValue(bean: contract, field: 'variableGiftCardMediationFee')}"/>
        </div>
    </div>
</div>

<div class="form-group col-sm-12">
    <label for="fixedMonthlyFee"><g:message code="facilityContract.fixedMonthlyFee.label"/>*</label>
    <g:textField name="fixedMonthlyFee" class="form-control" value="${fieldValue(bean: contract, field: 'fixedMonthlyFee')}" required="required"/>
</div>

<!-- VARIABLE FEE -->
<div class="form-group col-sm-12">
    <div class="row">
        <div class="form-group col-sm-5">
            <label for="variableMediationFee"><g:message code="facilityContract.variableMediationFee.label" args="[contract?.facility?.currency]"/>*</label>
            <g:textField name="variableMediationFee" class="form-control" value="${fieldValue(bean: contract, field: 'variableMediationFee')}" required="required"/>
        </div>

        <div class="form-group col-sm-2">
            <label for="mediationFeeMode">&nbsp;</label>
            <g:select name="mediationFeeMode" from="${FacilityContract.MediationFeeMode.values()}" class="form-control" value="${contract.mediationFeeMode}"/>
        </div>

        <div class="form-group col-sm-5">
            <label for="variableMediationFeePercentage"><g:message code="facilityContract.variableMediationFeePercentage.label"/>*</label>
            <g:textField name="variableMediationFeePercentage" class="form-control" value="${fieldValue(bean: contract, field: 'variableMediationFeePercentage')}" required="required"/>
        </div>
    </div>
</div>

<div class="form-group col-sm-12">
    <label for="variableTextMessageFee"><g:message code="facilityContract.variableTextMessageFee.label" args="[contract?.facility?.currency]"/>*</label>
    <g:textField name="variableTextMessageFee" class="form-control" value="${fieldValue(bean: contract, field: 'variableTextMessageFee')}" required="required"/>
</div>

<div class="form-group col-sm-12">
    <label for="dateValidFrom"><g:message code="facilityContract.dateValidFrom.label"/></label>
    <g:textField name="dateValidFrom" class="form-control" value="${formatDate(date: contract.dateValidFrom, format: 'yyyy-MM-dd')}" readonly="true"/>
</div>

<r:script>
    $(function() {
        $("#dateValidFrom").datepicker({
            autoSize: true,
            dateFormat: 'yy-mm-dd'
        });
    });
</r:script>
