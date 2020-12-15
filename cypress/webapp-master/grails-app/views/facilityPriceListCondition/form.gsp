<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityPriceListCondition.form.message1"/></title>
    <r:require module="jquery-timepicker"/>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link controller="facilityPriceList" action="index"><g:message code="priceList.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:link controller="facilityPriceListCondition" action="index" params="[id: params.id]"><g:message code="facilityPriceListCondition.form.message13" args="[pricelist?.name]"/> <g:render template="/templates/pricelist/sportsubscriptionsettings" model="[priceList: pricelist]" /></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityPriceListCondition.form.message3"/></li>
</ul>

<ul class="nav nav-tabs">
    <li class="active"><g:link controller="facilityPriceListCondition" action="index" params="${params}"><g:message code="default.price.label.plural"/></g:link></li>
    <li><g:link controller="facilityPriceList" action="edit" params="${params}"><g:message code="facilityPriceListCondition.form.message5"/></g:link></li>
</ul>

<g:errorMessage bean="${ priceInstance }" />

<table class="table table-bordered">
    <thead>
    <tr>
        <th width="50%" class="form-header">
            <g:message code="facilityPriceListCondition.form.message14"/><span class="ingress"><g:message code="facilityPriceListCondition.form.message6"/></span>
        </th>
        <th class="form-header">
            <g:message code="facilityPriceListCondition.form.message15"/><span class="ingress"><g:message code="facilityPriceListCondition.form.message7"/></span>
        </th>
    </tr>

    </thead>
    <tbody>
    <tr>
        <td valign="top" class="form-well">
            <g:form action="save" name="categoryForm" method="GET">
                <g:hiddenField name="categoryId" value="${category?.id}"/>
                <g:hiddenField name="id" value="${params.id}"/>
                <div class="control-group">
                    <label class="control-label" for="name"><g:message code="facilityPriceListCondition.form.message8"/></label>
                    <div class="controls">
                        <g:textField id="categoryName" name="name" value="${category?.name}" class="span5"/>
                    </div>
                </div>
            </g:form>
            <g:if test="${addedConditions.size() == 0}">
                <div id="message" class="alert alert-info">
                    <strong><g:message code="facilityPriceListCondition.form.message9"/></strong>
                </div>
            </g:if>
            <g:if test="${addedConditions.size() > 0}">
                <div class="alert alert-info">
                    <strong><g:message code="facilityPriceListCondition.form.message10"/></strong>
                </div>
            </g:if>

            <g:each in="${category.conditions}" var="condition">
              <g:if test="${!removedConditionIds?.contains(condition.id)}">
                <g:priceListConditionEntry pricelist="${pricelist}" condition="${condition}"/>
              </g:if>
            </g:each>

            <g:each in="${addedConditions}" var="condition">
                <g:priceListConditionEntry pricelist="${pricelist}" condition="${condition}"/>
            </g:each>

            <div class="btn-toolbar pull-right">
                <input type="button" value="${message(code: 'button.save.label')}" onclick="$('#categoryForm').submit()" class="btn btn-success"/>
                <g:if test="${category.id && !category.isDefaultCategory()}">
                    <g:link onclick="return confirm('${message(code: 'button.delete.confirm.message')}')" action="delete" params="[id:params.id, categoryId: category.id]" class="btn btn-inverse"><g:message code="button.delete.label"/></g:link>
                </g:if>
                <g:link action="index" id="${params.id}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </div>

        </td>
        <td valign="top" class="form-well">
            <g:errorMessage bean="${conditionBean}"/>
            <g:each in="${availableConditions}" var="condition">
                <g:priceListConditionForm pricelist="${pricelist}" condition="${condition}"/>
            </g:each>
        </td>
    </tr>

    </tbody>
</table>
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
