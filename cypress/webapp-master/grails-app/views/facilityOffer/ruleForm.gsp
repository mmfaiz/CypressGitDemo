<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCoupon.ruleForm.message1"/></title>
    <r:require module="jquery-timepicker"/>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link mapping="${params.type}" action="index"><g:message code="offer.${params.type}.label"/></g:link> <span class="divider">/</span></li>
    <li><g:link mapping="${params.type}" action="rules" id="${params.id}"><g:message code="facilityCoupon.ruleForm.message11" args="[coupon.name]"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCoupon.ruleForm.message1"/></li>
</ul>
<g:errorMessage bean="${ priceInstance }" />

<g:form id="${params.id}" class="form-inline" action="saveRuleSet">
    <div class="list-container">
        <div class="row">
            <div class="span12">
                <div class="list-header black">
                    <div class="list-header-label">
                        <div class="menu_icons menu_list"></div><g:message code="facilityCoupon.ruleForm.message12"/>
                    </div>
                </div>
            </div>
        </div>
        <table class="table table-bordered">
            <thead>
            <tr height="34">
                <td width="50%"><g:message code="facilityCoupon.ruleForm.message4"/></td>
                <td><g:message code="facilityCoupon.ruleForm.message5"/></td>
            </tr>

            </thead>
            <tbody>
            <tr>
                <td valign="top">
                    <ul class="form">
                        <li>
                            <div class="content" style="width: 100%">

                                <g:hiddenField name="id" value="${params.id}"/>
                                <g:hiddenField name="groupId" value="${params.groupId}"/>
                                <g:hiddenField id="ruleId" name="ruleId" value=""/>
                                <div class="control-group">
                                    <label class="control-label" for="name"><g:message code="facilityCoupon.ruleForm.message6"/></label>
                                    <div class="controls">
                                        <g:textField id="ruleName" style="width:95%" name="name" value="${group?.name?:params.name}" class="span5"/>
                                    </div>
                                </div>

                                <g:if test="${conditionGroup.slotConditionSets.size() == 0}">
                                    <div id="message" class="alert alert-info">
                                        <strong><g:message code="facilityCoupon.ruleForm.message7"/></strong>
                                    </div>
                                </g:if>

                                <g:if test="${conditionGroup.slotConditionSets.size() > 0}">
                                    <div class="alert alert-info">
                                        <strong><g:message code="facilityCoupon.ruleForm.message8"/></strong>
                                    </div>
                                </g:if>

                                <g:each in="${conditionGroup.slotConditionSets}" var="conditionSet">
                                    <g:each in="${conditionSet.slotConditions}" var="condition">

                                        <div class="condition">

                                            <g:slotConditionEntry condition="${condition}"/>

                                            <g:actionSubmit action="removeRuleSet" id="${params.id}" value="${message(code: 'button.delete.label')}" class="btn btn-danger right"
                                                            style="margin-top: 10px;" onclick="\$('#ruleId').val('${conditionSet.hashCode()}');"><g:message code="button.delete.label"/></g:actionSubmit>
                                            ${conditionSet.hashCode()}
                                            <div class="clear"></div>
                                        </div>


                                    </g:each>
                                </g:each>

                            </div>
                            <div class="pull-right" style="margin-top: 20px;margin-bottom: 15px">
                                <g:actionSubmit value="${message(code: 'facilityCoupon.ruleForm.message13')}" action="saveRule" class="btn btn-success"/>
                                <g:actionSubmit value="${message(code: 'button.delete.label')}" onclick="return confirm('${message(code: 'facilityCoupon.ruleForm.message14')}')" action="removeRule" class="btn btn-inverse"/>
                                <g:link mapping="${params.type}" action="rules" id="${params.id}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>

                            </div>
                        </li>
                    </ul>
                </td>
                <td valign="top">
                    <g:errorMessage bean="${conditionBean}"/>

                    <ul class="form">
                        <li>
                            <div class="content" style="width: 100%">
                                <g:each in="${availableConditions}" var="condition">
                                    <g:slotConditionForm condition="${condition}"/>
                                </g:each>
                            </div>
                            <div class="pull-right" style="margin-top: 20px">
                                <g:actionSubmit value="${message(code: 'button.add.label')}" action="saveRuleSet" class="btn btn-success"/>
                            </div>
                        </li>
                    </ul>


                </td>
            </tr>

            </tbody>
        </table>

    </div>
    <g:hiddenField name="type" value="${params.type}"/>
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
