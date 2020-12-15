<%@ page import="com.matchi.Sport; org.joda.time.DateTime; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="offer.${params.type}.label"/></title>
    <r:require modules="jquery-multiselect-widget"/>
</head>

<body>

<ul class="breadcrumb">
    <li><g:link mapping="${params.type}" action="index"><g:message code="offer.${params.type}.label"/></g:link> <span class="divider">/</span></li>
    <li><g:message code="facilityCoupon.archive.message1"/> (${coupons?.size()} <g:message code="unit.st"/>)</li>
</ul>

<ul id="customer-tab" class="nav nav-tabs">
    <li><g:link action="index" mapping="${params.type}" ><g:message code="facilityCoupon.index.active"/></g:link></li>
    <li class="active"><g:link mapping="${params.type}" action="archive"><g:message code="default.archive.label"/></g:link></li>
</ul>

<div class="action-bar">
    <div class="btn-toolbar-left">
        <g:submitButton form="archivedCoupons" class="btn btn-inverse" name="unarchivedCoupons" value="${message(code: 'reactivate.label')}"/>
    </div>
</div>
<g:form name="archivedCoupons" action="getFromArchive" class="no-margin">
    <table class="table table-striped table-bordered" data-provides="rowlink">
        <thead>
        <tr>
            <th class="center-text nolink" width="20">
                <g:checkBox class="checkbox-selector" name="" onclick="selector();" value="" checked="false"/>
            </th>
            <g:sortableColumn property="name" params="${params}" titleKey="coupon.name.label"/>
            <g:sortableColumn property="nrOfTickets" params="${params}" titleKey="offer.${params.type}.nrOfTickets.plural" class="center-text" width="80"/>
            <th width="150" class="center-text"><g:message code="coupon.customerCoupons.label"/></th>
            <th width="150" class="center-text"><g:message code="coupon.couponConditionGroups.label"/></th>
            <g:sortableColumn property="availableOnline" params="${params}" titleKey="coupon.availableOnline.label2" class="center-text" width="80"/>
            <g:sortableColumn property="nrOfDaysValid" params="${params}" titleKey="coupon.nrOfDaysValid.label" class="center-text"
                              width="80"/>
            <td width="140" class="center-text"><g:message code="facilityCoupon.index.priceStatus"/></td>
        </tr>
        </thead>

        <g:if test="${coupons?.size() == 0}">
            <tr>
                <td colspan="6"><i><g:message code="facilityCoupon.archive.message2"/></i></td>
            </tr>
        </g:if>

        <g:each in="${coupons}" var="coupon">
            <tr>
                <td class="center-text nolink">
                    <g:checkBox name="couponsId" value="${coupon.id}" checked="false" class="selector"/>
                </td>
                <td>
                    <g:link mapping="${params.type}" action="sold" id="${coupon.id}">${coupon.name}</g:link>
                </td>
                <td class="center-text"><b>${coupon.unlimited ? "-" : coupon.nrOfTickets}</b></td>
                <td class="center-text">
                    <span
                            class="label label-${coupon.customerCoupons?.size() > 0 ? 'success' : ''}">${coupon.customerCoupons?.size()}<g:message code="unit.st"/></span>
                </td>
                <td class="center-text nolink">
                    <span class="label">${coupon.couponConditionGroups?.size()}<g:message code="unit.st"/></span>
                    - <g:link mapping="${params.type}Conditions" action="list" id="${coupon.id}" ><g:message code="facilityCoupon.archive.message3"/></g:link>
                </td>
                <td class="center-text">
                    <g:if test="${coupon.availableOnline}">
                        <span class="label label-success"><g:message code="default.yes.label"/></span>
                    </g:if>
                    <g:else>
                        <span class="label"><g:message code="default.no.label"/></span>
                    </g:else>
                </td>
                <td class="center-text">${coupon.nrOfDaysValid ? coupon.nrOfDaysValid + "d" : ""}</td>
                <td class="center-text">
                    <g:set var="missingPricesCount" value="${coupon.numMissingPrices()}"/>
                    <g:if test="${missingPricesCount}">
                        <span class="label label-important">
                            <g:message code="facilityCoupon.index.priceStatus.missing" args="[missingPricesCount]"/>
                        </span>
                    </g:if>
                    <g:else>
                        <span class="label label-success">OK</span>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </table>
</g:form>
<div class="row">
    <div class="span12">
        <g:paginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered" params="${params}"
                                    maxsteps="10" max="100" action="index" total="${coupons.size()}"/>
    </div>
</div>
</body>
</html>
