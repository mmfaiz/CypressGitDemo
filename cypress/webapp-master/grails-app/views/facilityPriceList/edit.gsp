<%@ page import="org.joda.time.DateTime; com.matchi.Sport; com.matchi.Court"%>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="priceList.label.plural"/></title>
    <jqval:resources/>
    <jqvalui:resources type="js"/>
    <jqvalui:resources type="css" />
</head>
<body>

<ul class="breadcrumb">
    <li><g:link action="${session[com.matchi.facility.FacilityPriceListController.LAST_LIST_ACTION_KEY] ?: 'index'}"><g:message code="priceList.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="button.edit.label"/> ${priceList?.name} <g:render template="/templates/pricelist/sportsubscriptionsettings" model="[priceList: priceList]" /></li>
</ul>

<ul id="invoice-tab" class="nav nav-tabs">
    <li><g:link controller="facilityPriceListCondition" action="index" params="[id: priceList.id]"><g:message code="default.price.label.plural"/></g:link></li>
    <li class="active"><g:link contronller="facilityPriceList" action="edit" params="[id: priceList.id]"><g:message code="button.edit.label"/></g:link></li>
</ul>


<g:errorMessage bean="${priceList}"/>


<g:form action="update" method="POST" name="priceListForm" class="form-horizontal form-well">
    <g:hiddenField name="id" value="${priceList?.id}" />
    <g:hiddenField name="version" value="${priceList?.version}" />

    <div class="form-header">
        <g:message code="facilityPriceList.edit.message12"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group">
            <label class="control-label" for="type"><g:message code="priceList.type.label"/></label>
            <div class="controls vertical-padding5">
                <g:message code="priceList.type.${priceList.type}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="description"><g:message code="priceList.sport.label"/></label>
            <div class="controls">
                <g:select name="sport.id" optionKey="id" optionValue="${ {g.message(code:'sport.name.'+it.id) } }" value="${priceList?.sport?.id}"
                        from="${com.matchi.Court.available(priceList.facility).list().collectAll {it.sport}.unique().sort { it.name }}"/>
            </div>
        </div>
        <hr>
        <div class="control-group form-inline">
            <label class="control-label" for="startDate"><g:message code="priceList.startDate.label"/></label>
            <div class="controls controls-row">
                <input class="span2 center-text" type="text" name="startDate" id="startDate" value="<g:formatDate formatName="date.format.dateOnly" date="${priceList?.startDate}" />" />
             </div>
        </div>
        <hr>
        <div class="control-group">
            <label class="control-label" for="name"><g:message code="priceList.name.label" default="Namn" />*</label>
            <div class="controls">
                <g:textField name="name" value="${priceList?.name}" class="span8" />
            </div>
        </div>
        <hr>
        <div class="control-group">
            <label class="control-label" for="description"><g:message code="priceList.description.label" default="Beskrivning" /></label>
            <div class="controls">
                <g:textArea cols="30" rows="4" name="description" value="${priceList?.description}" class="span8" />
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="subscriptions"><g:message code="priceList.subscriptions.label"/></label>
            <div class="controls">
                <label class="checkbox">
                    <g:checkBox name="subscriptions" value="1" checked="${priceList?.subscriptions}"/>
                    <g:message code="priceList.subscriptions.hint"/>
                </label>
            </div>
        </div>
        <div class="form-actions">
            <g:actionSubmit action="update" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:link action="delete" onclick="return confirm('${message(code: 'button.delete.confirm.message')}')" id="${priceList.id}" class="btn btn-inverse"><g:message code="button.delete.label"/></g:link>
            <g:link action="${session[com.matchi.facility.FacilityPriceListController.LAST_LIST_ACTION_KEY] ?: 'index'}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
<g:javascript>
    $(document).ready(function() {

        $("#startDate").datepicker({
            firstDay: 1,
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}"
        });

        $("#priceListForm").preventDoubleSubmission({});
    });

</g:javascript>
</body>
</html>