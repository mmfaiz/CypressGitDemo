<%@ page import="org.joda.time.DateTime; com.matchi.Sport; com.matchi.Court; com.matchi.PriceList"%>
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
    <li><g:link action="index"><g:message code="priceList.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityPriceList.create.message3"/></li>
</ul>
<g:if test="${!facility?.isMasterFacility()}">
    <g:errorMessage bean="${priceList}"/>

    <g:form action="save" id="priceListForm" name="priceListForm" class="form-horizontal form-well">
        <g:hiddenField name="id" value="${priceList?.id}" />
        <g:hiddenField name="version" value="${priceList?.version}" />

        <div class="form-header">
            <g:message code="facilityPriceList.create.message10"/><span class="ingress"><g:message code="facilityPriceList.create.message4"/></span>
        </div>
        <fieldset>
            <div class="control-group">
                <label class="control-label" for="type"><g:message code="priceList.type.label"/></label>
                <div class="controls">
                    <div class="input-append">
                    <g:radioGroup name="type" value="${priceList?.type}"
                            values="${PriceList.Type.values()}"
                            labels="[message(code: 'priceList.type.SLOT_BASED'), message(code: 'priceList.type.HOUR_BASED')]">
                        <label class="radio inline">
                            ${it.radio} ${it.label}
                        </label>
                    </g:radioGroup>
                    </div>
                    <p class="help-block"><g:message code="priceList.type.description"/></p>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label"for="sport.id"><g:message code="priceList.sport.label"/></label>
                <div class="controls">
                    <g:select name="sport.id" optionKey="id" optionValue="${ {g.message(code:'sport.name.'+it.id) } }" value="${priceList?.sport?.id}"
                            from="${com.matchi.Court.available(facility).list().collectAll { it.sport}.unique().sort { it.name }}"/>
                </div>
            </div>
            <hr>
            <div class="control-group form-inline ${hasErrors(bean:priceList, field:'startDate', 'error')} ${hasErrors(bean:priceList, field:'startDate', 'error')}">
                <label class="control-label"for="startDate"><g:message code="priceList.startDate.label"/></label>
                <div class="controls controls-row">
                    <input class="span2 center-text" type="text" name="startDate" id="startDate" value="<g:formatDate formatName="date.format.dateOnly" date="${priceList?.startDate}" />" />
                </div>
            </div>
            <hr>
            <div class="control-group ${hasErrors(bean:priceList, field:'name', 'error')}">
                <label class="control-label"for="name"><g:message code="priceList.name.label" default="Namn" />*</label>
                <div class="controls">
                    <g:textField name="name" value="${priceList?.name}" class="span8"/>
                </div>
            </div>
            <hr>
            <div class="control-group">
                <label class="control-label"for="description"><g:message code="priceList.description.label" default="Beskrivning" /></label>
                <div class="controls">
                    <g:textArea cols="30" rows="3" name="description" value="${priceList?.description}" class="span8"/>
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
                <g:actionSubmit action="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </div>
        </fieldset>
    </g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
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
