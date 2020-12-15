<%@ page import="com.matchi.Sport; org.joda.time.DateTime; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="offer.PromoCode.label"/></title>
    <r:require modules="jquery-multiselect-widget"/>
</head>

<body>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="offer.PromoCode.label"/></g:link> <span class="divider">/</span></li>
    <li><g:message code="facilityCoupon.archive.message1"/> (${coupons?.size()} <g:message code="unit.st"/>)</li>
</ul>

<ul id="customer-tab" class="nav nav-tabs">
    <li><g:link action="index"><g:message code="facilityCoupon.index.active"/></g:link></li>
    <li class="active"><g:link action="archive"><g:message code="default.archive.label"/></g:link></li>
</ul>

<g:form name="archivedCoupons" action="getFromArchive" class="no-margin">
    <table class="table table-striped table-bordered" data-provides="rowlink">
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
                <td colspan="6"><i><g:message code="facilityCoupon.archive.message2"/></i></td>
            </tr>
        </g:if>

        <g:each in="${coupons}" var="coupon">
            <tr>
                <td>
                    <g:link mapping="${params.type}" action="edit" id="${coupon.id}">${coupon.name}</g:link>
                </td>
                <td>
                    <g:link mapping="${params.type}" action="edit" id="${coupon.id}">${coupon.code}</g:link>
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
<div class="row">
    <div class="span12">
        <g:paginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered" params="${params}"
                                    maxsteps="10" max="100" action="index" total="${coupons.size()}"/>
    </div>
</div>
</body>
</html>
