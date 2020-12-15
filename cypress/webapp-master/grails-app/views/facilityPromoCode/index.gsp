<%@ page import="com.matchi.Facility" %>
<g:set var="returnUrl" value="${createLink(action: 'index', absolute: true, params: params.subMap(params.keySet() - 'error' - 'message' - 'type'))}"/>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="offer.PromoCode.label"/></title>
    <r:require modules="matchi-selectall"/>
</head>
<body>

<ul class="breadcrumb">
    <li><g:message code="offer.PromoCode.index.title" args="[coupons?.size()]"/></li>
</ul>
<g:errorMessage bean="${coupon}"/>

<g:render template="/templates/messages/webflowMessage"/>

<ul id="customer-tab" class="nav nav-tabs">
    <li class="active"><g:link mapping="${params.type}" action="index"><g:message code="facilityCoupon.index.active"/></g:link></li>
    <li><g:link mapping="${params.type}" action="archive"><g:message code="default.archive.label"/></g:link></li>
</ul>
<div class="action-bar">
    <div class="btn-toolbar-right">
        <g:link mapping="${params.type}" action="add" class="btn btn-inverse">
            <span><g:message code="offer.PromoCode.create"/></span>
        </g:link>
    </div>
</div>
<g:form name="offers" controller="facilityOfferFlow" action="copy" class="no-margin">
    <table id="offers-table" class="table table-striped table-bordered" data-provides="rowlink">
        <thead>
        <tr>
            <g:sortableColumn property="name" params="${params}" titleKey="coupon.name.label"/>
            <g:sortableColumn property="code" params="${params}" titleKey="coupon.code.label"/>
            <th width="150" class="center-text"><g:message code="coupon.timesUsed.label"/></th>
            <g:sortableColumn property="startDate" params="${params}" titleKey="offer.startDate.label"/>
            <g:sortableColumn property="endDate" params="${params}" titleKey="offer.endDate.label"/>
        </tr>
        </thead>

        <g:if test="${coupons?.size() == 0}">
            <tr>
                <td colspan="7"><i><g:message code="offer.PromoCode.emptyList"/></i></td>
            </tr>
        </g:if>

        <g:each in="${coupons}" var="coupon">
            <tr>
                <td>
                    <g:link action="edit" id="${coupon.id}">${coupon.name}</g:link>
                </td>
                <td>
                    <g:link action="edit" id="${coupon.id}">${coupon.code}</g:link>
                </td>
                <td class="center-text">
                    <span class="label label-${coupon.customerCoupons?.size() > 0 ? 'success' : ''}">${coupon.customerCoupons?.size()}st</span>
                </td>
                <td><g:formatDate date="${coupon.startDate}" formatName="date.format.dateOnly"/></td>
                <td><g:formatDate date="${coupon.endDate}" formatName="date.format.dateOnly"/></td>
            </tr>
        </g:each>
    </table>
</g:form>

<r:script>
    $(document).ready(function() {
        $(".search").focus();

        $("#offers-table").selectAll({max: "${g.forJavaScript(data: coupons.size())}", count: "${g.forJavaScript(data: coupons.size())}", name: "offer"});
    });
</r:script>
</body>
</html>
