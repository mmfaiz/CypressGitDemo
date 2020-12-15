<%@ page import="com.matchi.Facility" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="offer.${params.type}.label"/></title>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link mapping="${params.type}" action="index"><g:message code="offer.${params.type}.label"/></g:link><span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCouponCondition.list.message10" args="[coupon.name]"/></li>
</ul>
<g:errorMessage bean="${coupon}"/>

<ul class="nav nav-tabs">
    <g:if test="${params.type != com.matchi.facility.offers.CreateFacilityOfferCommand.Type.PromoCode.name()}">
        <li><g:link mapping="${params.type}" action="sold" id="${coupon?.id}"><g:message code="customer.label.plural"/></g:link></li>
        <li>
            <g:link mapping="${params.type + 'Prices'}" action="index" id="${coupon?.id}">
                <g:message code="couponPrice.label.plural"/>
            </g:link>
        </li>
    </g:if>
    <li>
        <g:link mapping="${params.type}" action="edit" id="${coupon?.id}"><g:message code="button.edit.label"/></g:link>
    </li>
    <li class="active"><g:link mapping="${params.type + 'Conditions'}" action="list" id="${coupon.id}"><g:message code="default.terms.label"/></g:link></li>
</ul>

<div class="action-bar">
    <div class="btn-toolbar-right">
        <g:link mapping="${params.type + 'Conditions'}" action="form" id="${coupon.id}" class="btn btn-inverse pull-right">
            <span><g:message code="facilityCouponCondition.list.message6"/></span>
        </g:link>
    </div>
</div>

<table class="table table-striped table-bordered" data-provides="rowlink">
    <thead>
    <tr>
        <th><g:message code="default.name.label"/></th>
        <th width="130" class="center-text"><g:message code="facilityCouponCondition.list.message8"/></th>
    </tr>
    </thead>

    <tbody>
    <g:if test="${coupon.couponConditionGroups.size() < 1}">
        <tr>
            <td colspan="3"><i><g:message code="facilityCouponCondition.list.message9"/></i></td>
        </tr>
    </g:if>
    <g:each in="${coupon.couponConditionGroups}">
        <tr>
            <td><g:link mapping="${params.type + 'Conditions'}" action="form" id="${coupon.id}" params="[groupId: it.id]">${it.name?:"-"}</g:link></td>
            <td class="center-text">${it.slotConditionSets.size()}<g:message code="unit.st"/></td>
        </tr>
    </g:each>
    </tbody>

</table>
</body>
</html>
