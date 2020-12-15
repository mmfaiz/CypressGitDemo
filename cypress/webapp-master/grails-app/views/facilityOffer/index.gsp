<%@ page import="com.matchi.Facility" %>
<g:set var="returnUrl" value="${createLink(action: 'index', mapping: params.type, absolute: true, params: params.subMap(params.keySet() - 'error' - 'message' - 'type'))}"/>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="offer.${params.type}.label"/></title>
    <r:require modules="matchi-selectall"/>
</head>
<body>

<ul class="breadcrumb">
    <li><g:message code="offer.${params.type}.index.title" args="[coupons?.size()]"/></li>
</ul>
<g:errorMessage bean="${coupon}"/>

<g:render template="/templates/messages/webflowMessage"/>

<ul id="customer-tab" class="nav nav-tabs">
    <li class="active"><g:link mapping="${params.type}" action="index"><g:message code="facilityCoupon.index.active"/></g:link></li>
    <li><g:link mapping="${params.type}" action="archive"><g:message code="default.archive.label"/></g:link></li>
</ul>
<div class="action-bar">
    <div class="btn-toolbar-left">
        <div class="btn-group">
            <button class="btn btn-inverse dropdown-toggle bulk-action" data-toggle="dropdown">
                <g:message code="button.actions.label"/>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu">
                <li><a href="javascript:void(0)" onclick="submitFormTo('#offers', '<g:createLink controller="facilityOfferFlow" action="copy"
                        params="['returnUrl': returnUrl]"/>');"><g:message code="button.copy.label"/></a></li>
            </ul>
        </div>
        %{--<g:form method="get">
            <div class="input-append">
                <g:textField id="coupon-search-input" class="search" name="q" value="${cmd?.q}" placeholder="${message(code: "offer.${params.type}.search")}"/>
                <span class="add-on"><i class="icon-search"></i></span>
            </div>
        </g:form>--}%
    </div>
    <div class="btn-toolbar-right">
        <g:link mapping="${params.type}" action="add" class="btn btn-inverse">
            <span><g:message code="offer.${params.type}.create"/></span>
        </g:link>
    </div>
</div>
<g:form name="offers" controller="facilityOfferFlow" action="copy" class="no-margin">
<table id="offers-table" class="table table-striped table-bordered" data-provides="rowlink">
    <thead>
    <tr>
        <th class="center-text" width="20">
            <g:checkBox class="checkbox-selector" name="" value="" checked="false"/>
        </th>
        <g:sortableColumn property="name" params="${params}" titleKey="coupon.name.label"/>
        <g:sortableColumn property="nrOfTickets" params="${params}" titleKey="offer.${params.type}.nrOfTickets.plural" class="center-text" width="80"/>
        <th width="150" class="center-text"><g:message code="coupon.customerCoupons.label"/></th>
        <th width="150" class="center-text"><g:message code="coupon.couponConditionGroups.label"/></th>
        <g:sortableColumn property="availableOnline" params="${params}" titleKey="coupon.availableOnline.label2" class="center-text" width="80"/>
        <g:sortableColumn property="nrOfDaysValid" params="${params}" titleKey="coupon.nrOfDaysValid.label" class="center-text" width="80"/>
    </tr>
    </thead>

    <g:if test="${coupons?.size() == 0}">
        <tr>
            <td colspan="7"><i><g:message code="offer.${params.type}.emptyList"/></i></td>
        </tr>
    </g:if>

    <g:each in="${coupons}" var="coupon">
        <tr>
            <td class="center-text nolink">
                <g:checkBox name="offerId" value="${coupon.id}" checked="false" class="selector"/>
            </td>
            <td>
                <g:link mapping="${params.type}" action="sold" id="${coupon.id}">${coupon.name}</g:link>
            </td>
            <td class="center-text"><b>${coupon.unlimited ? "-" : coupon.nrOfTickets}</b></td>
            <td class="center-text">
                <span class="label label-${coupon.customerCoupons?.size() > 0 ? 'success' : ''}">${coupon.customerCoupons?.size()}st</span>
            </td>
            <td class="center-text nolink">
                <span class="label">${coupon.couponConditionGroups?.size()}<g:message code="unit.st"/></span>
                - <g:link mapping="${params.type + 'Conditions'}" action="list" id="${coupon.id}"><g:message code="facilityCoupon.index.message6"/></g:link>
            </td>
            <td class="center-text">
                <g:if test="${coupon.availableOnline}">
                    <span class="label label-success"><g:message code="default.yes.label"/></span>
                </g:if>
                <g:else>
                    <span class="label"><g:message code="default.no.label"/></span>
                </g:else>
            </td>
            <td class="center-text">${coupon.nrOfDaysValid ? coupon.nrOfDaysValid + "d":""}</td>
        </tr>
    </g:each>
</table>
<g:if test="${globalCoupons?.size() > 0}">

    <ul class="breadcrumb" style="margin-top: 50px;">
        <li><g:message code="offer.global.${params.type}.index.title" args="[globalCoupons.size()]"/></li>
    </ul>
    <div class="alert alert-desc"><g:message code="offer.global.${params.type}.index.info"/></div>
    <table id="offers-table" class="table table-striped table-bordered" data-provides="rowlink">
        <thead>
            <tr class="additional-table">
                <g:sortableColumn property="name" params="${params}" titleKey="coupon.name.label"/>
                <g:sortableColumn property="nrOfTickets" params="${params}" titleKey="offer.${params.type}.nrOfTickets.plural" class="center-text" width="80"/>
                <th width="150" class="center-text"><g:message code="coupon.customerCoupons.label"/></th>
                <g:sortableColumn property="availableOnline" params="${params}" titleKey="coupon.availableOnline.label2" class="center-text" width="80"/>
                <g:sortableColumn property="nrOfDaysValid" params="${params}" titleKey="coupon.nrOfDaysValid.label" class="center-text" width="80"/>
            </tr>
        </thead>
        <g:each in="${globalCoupons}" var="coupon">
            <tr>
                <td>${coupon.name}</td>
                <td class="center-text"><b>${coupon.unlimited ? "-" : coupon.nrOfTickets}</b></td>
                <td class="center-text">
                    <span class="label label-${coupon.customerCoupons?.size() > 0 ? 'success' : ''}">${coupon.customerCoupons?.size()}st</span>
                </td>
                <td class="center-text">
                    <g:if test="${coupon.availableOnline}">
                        <span class="label label-success"><g:message code="default.yes.label"/></span>
                    </g:if>
                    <g:else>
                        <span class="label"><g:message code="default.no.label"/></span>
                    </g:else>
                </td>
                <td class="center-text">${coupon.nrOfDaysValid ? coupon.nrOfDaysValid + "d":""}</td>
            </tr>
        </g:each>
    </table>
</g:if>
</g:form>

<r:script>
    $(document).ready(function() {
        $(".search").focus();

        $("#offers-table").selectAll({max: "${g.forJavaScript(data: coupons.size())}", count: "${g.forJavaScript(data: coupons.size())}", name: "offer"});
    });
</r:script>
</body>
</html>
