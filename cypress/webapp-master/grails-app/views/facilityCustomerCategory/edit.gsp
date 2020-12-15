<%@ page import="com.matchi.Sport; com.matchi.Court"%>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerCategory.edit.message1"/></title>
</head>
<body>
<g:errorMessage bean="${category}"/>

<h2><g:message code="facilityCustomerCategory.edit.message1"/></h2>

<ul class="breadcrumb">
    <li><g:link controller="facilityPriceList" action="index"><g:message code="priceList.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerCategory.edit.message1"/></li>
</ul>


<g:form action="update" class="form-horizontal">
    <g:hiddenField name="id" value="${category.id}"/>
    <fieldset>
        <div class="control-group">
            <label class="control-label" for="name"><g:message code="default.name.label"/></label>
            <div class="controls">
                <g:textField name="name" value="${category?.name}" class="span10"/>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <label class="checkbox">
                    <g:checkBox name="onlineSelect" value="${category?.onlineSelect}"/>
                    <g:message code="priceListCustomerCategory.onlineSelect.label"/>
                </label>
                <label class="checkbox">
                    <g:checkBox name="forceUseCategoryPrice" value="${category?.forceUseCategoryPrice}"/>
                    <g:message code="priceListCustomerCategory.forceUseCategoryPrice.label"/>
                </label>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="groupIds"><g:message code="group.label.plural"/></label><br>
            <div class="controls">
                <g:each var="group" in="${groups}">
                    <label class="checkbox">
                        <g:checkBox name="groupIds" class="styled" checked="${groupCondition?.groups?.contains(group)}" value="${group.id}" />
                        ${group.name}
                    </label>
                </g:each>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="typeIds"><g:message code="membershipType.label.plural"/></label><br>
            <div class="controls">
                <g:each in="${types}" var="type">
                    <label class="checkbox">
                        <g:checkBox name="typeIds" class="styled" checked="${typeCondition?.membershipTypes?.contains(type)}" value="${type.id}" />
                        ${type.name}
                    </label>
                </g:each>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="daysBookable"><g:message code="facilityCustomerCategory.bookBefore.label"/></label>
            <div class="controls">
                <input type="number" min="0" name="daysBookable" id="daysBookable" value="${category?.daysBookable}" class="span10"/>
                <p class="help-block"><g:message code="facilityCustomerCategory.bookBefore.tooltip" args="[facility?.bookingRuleNumDaysBookable]"/></p>
            </div>

        </div>

        <div class="form-actions">
            <g:actionSubmit action="update" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:actionSubmit onclick="return confirm('${message(code: 'facilityCustomerCategory.edit.message8')}')"
                            action="delete" name="btnSumbit" value="${message(code: 'button.delete.label')}" class="btn btn-inverse"/>
            <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>

        </div>
    </fieldset>
</g:form>
<r:script>
    $("#name").focus();
    $("[rel=tooltip]").tooltip();
</r:script>
</body>
</html>