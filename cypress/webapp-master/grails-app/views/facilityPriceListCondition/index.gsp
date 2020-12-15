<%@ page import="java.sql.Timestamp; org.joda.time.Interval; org.joda.time.Period; org.joda.time.DateTime; com.matchi.Facility; com.matchi.PriceList" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="default.price.label.plural"/></title>
</head>
<body>
<g:errorMessage bean="${ pricelist }" />

<ul class="breadcrumb">
    <li><g:link controller="facilityPriceList" action="${session[com.matchi.facility.FacilityPriceListController.LAST_LIST_ACTION_KEY] ?: 'index'}"><g:message code="priceList.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityPriceListCondition.index.message12" args="[pricelist?.name]"/> <g:render template="/templates/pricelist/sportsubscriptionsettings" model="[priceList: pricelist]" /></li>
</ul>

<ul id="invoice-tab" class="nav nav-tabs">
    <li class="active"><g:link controller="facilityPriceListCondition" action="index" params="${params}"><g:message code="default.price.label.plural"/></g:link></li>
    <li><g:link controller="facilityPriceList" action="edit" params="${params}"><g:message code="button.edit.label"/></g:link></li>
</ul>

<g:if test="${pricelist?.type == PriceList.Type.HOUR_BASED}">
    <div class="alert alert-info">
        <g:message code="priceList.type.HOUR_BASED.note"/>
    </div>
</g:if>

<div class="action-bar">
    <div class="btn-toolbar-left">
        <p>${pricelist.sport} :
        <g:formatDate format="yyyy-MM-dd" date="${pricelist.startDate}"/> -
    </div>
    <div class="btn-toolbar-right">
        <g:link action="form" id="${pricelist.id}" class="btn btn-inverse">
            <span><g:message code="facilityPriceListCondition.index.message5"/></span>
        </g:link>
    </div>
</div>

<g:form action="savePrices">
    <g:hiddenField name="id" value="${params.id}"/>
    <table class="table table-striped table-bordered">
        <thead>
        <tr>
            <th width="100"><g:message code="facilityPriceListCondition.index.message6"/></th>
            <th width="40"><g:message code="facilityPriceListCondition.index.message7"/></th>

            <g:each in="${customerCategories}" var="category">
                <th>${category.name}</th>
            </g:each>

            <th class="center-text" width="40"><g:message code="button.edit.label"/></th>
        </tr>

        </thead>
        <tbody>
        <g:each in="${pricelist.priceListConditionCategories}" var="priceCategory">
            <tr>
                <td>
                    <g:if test="${!priceCategory.defaultCategory}">
                        <g:link action="form" params="[id:pricelist.id, categoryId:priceCategory.id]">${priceCategory.name}</g:link>
                    </g:if>
                    <g:else>
                        <b>${priceCategory.name}</b><br>
                    </g:else>
                </td>
                <td>
                    <g:each in="${priceCategory.conditions}">
                        <g:message code="pricelist.conditions.${it.getClass().getSimpleName()}"/>&nbsp;
                    </g:each>
                </td>
                <g:each in="${customerCategories}" var="category">
                    <td width="250" style="color: #999">
                        <label><g:currentFacilityCurrency facility="${facility}"/></label>
                        <g:textField style="width: 30px" value="${prices['price_'+category.id + '_' +priceCategory.id]?.price}" name="price_${category.id}_${priceCategory.id}"/>
                        <g:if test="${facility.hasApplicationInvoice() && !facility.hasFortnox()}">
                        <label><g:message code="facilityPriceListCondition.index.message10"/></label>
                        <g:textField style="width: 30px" value="${prices['price_'+category.id + '_' +priceCategory.id]?.account}" name="account_${category.id}_${priceCategory.id}"/>
                        </g:if>
                    </td>
                </g:each>

                <td class="center-text">
                    <g:if test="${!priceCategory.defaultCategory}">
                        <g:link action="form" params="[id: pricelist.id, categoryId: priceCategory.id]"><img src="${resource(dir:'images', file:'edit_btn.png')}"/></g:link>
                    </g:if>
                    <g:else>-</g:else>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <div class="form-actions">
        <g:actionSubmit value="${message(code: 'facilityPriceListCondition.index.message11')}" action="savePrices" class="btn btn-success"/>
        <g:link controller="facilityPriceList" action="${session[com.matchi.facility.FacilityPriceListController.LAST_LIST_ACTION_KEY] ?: 'index'}" id="${params.id}"
                class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
    </div>
</g:form>
<g:javascript>
    function showForm() {
        $('tbody form').parent().show();
    }
</g:javascript>
</body>
</html>
