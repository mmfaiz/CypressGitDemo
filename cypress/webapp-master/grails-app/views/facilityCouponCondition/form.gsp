<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCouponCondition.form.message1"/></title>
    <r:require module="jquery-timepicker"/>
</head>
<body>
<g:set var="cardType" value="${message(code: "offer.${params.type}.label2")}"/>
<ul class="breadcrumb">
    <li><g:link mapping="${params.type}" action="index"><g:message code="offer.${params.type}.label"/></g:link> <span class="divider">/</span></li>
    <li><g:link mapping="${params.type + 'Conditions'}" action="list" id="${params.id}"><g:message code="facilityCouponCondition.form.message14" args="[coupon.name]"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCouponCondition.form.message3"/></li>
</ul>
<g:errorMessage bean="${ priceInstance }" />

<ul class="nav nav-tabs">
    <g:if test="${params.type != com.matchi.facility.offers.CreateFacilityOfferCommand.Type.PromoCode.name()}">
        <li><g:link mapping="${params.type}" action="sold" id="${coupon?.id}"><g:message code="customer.label.plural"/></g:link></li>
    </g:if>
    <li>
        <g:link mapping="${params.type}" action="edit" id="${coupon?.id}"><g:message code="button.edit.label"/></g:link>
    </li>
    <li class="active"><g:link mapping="${params.type + 'Conditions'}" action="list" id="${coupon.id}"><g:message code="default.terms.label"/></g:link></li>
</ul>

<g:form id="${params.id}" mapping="${params.type + 'Conditions'}" action="addConditionSet" class="form-inline">
    <g:hiddenField name="couponType" value="${params.type}"/>
    <table class="table table-bordered">
        <thead>
        <tr height="34">
            <th width="50%" class="form-header"><g:message code="facilityCouponCondition.form.message15"/><span class="ingress"><g:message code="facilityCouponCondition.form.message7" args="[cardType]"/></span></th>
            <th class="form-header"><g:message code="facilityCouponCondition.form.message16"/><span class="ingress"><g:message code="facilityCouponCondition.form.message8"/></span></th>
        </tr>

        </thead>
        <tbody>
        <tr>
            <td valign="top" class="form-well">
                <g:hiddenField name="id" value="${params.id}"/>
                <g:hiddenField name="groupId" value="${params.groupId}"/>
                <g:hiddenField id="ruleId" name="ruleId" value=""/>
                <div class="control-group padding10">
                    <label class="control-label" for="name"><g:message code="facilityCouponCondition.form.message9"/></label>
                    <div class="controls">
                        <g:textField id="ruleName" style="width:90%" name="name" value="${group?.name?:params.name}"/>
                    </div>
                </div>

                <g:if test="${conditionSets.size() == 0}">
                    <div id="message" class="alert alert-info padding10">
                        <strong><g:message code="facilityCouponCondition.form.message10" args="[cardType]"/></strong>
                    </div>
                </g:if>

                <g:if test="${conditionSets.size() > 0}">
                    <div class="alert alert-info">
                        <strong><g:message code="facilityCouponCondition.form.message11"/></strong>
                    </div>
                </g:if>

                <g:each in="${conditionSets}"  var="conditionSet">
                    <div class="condition">
                        <g:each in="${conditionSet.slotConditions}" var="condition">
                            <g:slotConditionEntry condition="${condition}"/>
                        </g:each>

                        <g:actionSubmit action="removeConditionSet" id="${params.id}" value="${message(code: 'button.delete.label')}" class="btn btn-danger pull-right top-margin20"
                                        onclick="\$('#ruleId').val('${conditionSet.identifier()}');"><g:message code="button.delete.label"/></g:actionSubmit>

                    </div>
                    <div class="clearfix"></div>
                </g:each>
                <div class="btn-toolbar pull-right">
                    <g:actionSubmit value="${message(code: 'facilityCouponCondition.form.message17')}" action="saveConditionGroup" class="btn btn-success"/>
                    <g:actionSubmit value="${message(code: 'button.delete.label')}" onclick="return confirm('${message(code: 'facilityCouponCondition.form.message18')}')" action="removeConditionGroup" class="btn btn-inverse"/>
                    <g:link action="list" id="${params.id}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </div>
            </td>
            <td valign="top" class="form-well">
                <g:errorMessage bean="${conditionBean}"/>

                <g:each in="${availableConditions}" var="condition">
                    <g:slotConditionForm condition="${condition}"/>
                </g:each>

                <g:actionSubmit value="${message(code: 'button.add.label')}" action="addConditionSet" class="btn btn-success vertical-margin10"/>
            </td>
        </tr>

        </tbody>
    </table>
</g:form>

<g:javascript>

    $(document).ready(function() {
        $(".price-condition-date").each(function(){
           var altField = '#' + $(this).prop('id') + 'Date';

            $(this).datepicker({
                autoSize: true,
                dateFormat: '${message(code: 'date.format.dateOnly.small')}',
                altField: altField,
                altFormat: 'yy-mm-dd'
            });
        });

        $(".price-condition-time").addTimePicker({
            hourText: '${message(code: 'default.timepicker.hour')}',
            minuteText: '${message(code: 'default.timepicker.minute')}'
        });
    });

    function onConditionSubmit() {
        $('input[name="hiddenCategoryName"]').each(function() {
            $(this).val($("#categoryName").val())
        });
    }

    function onConditionRemove() {
        $('input[name="hiddenCategoryName"]').each(function() {
            $(this).val($("#categoryName").val())
        });
    }
</g:javascript>
</body>
</html>
