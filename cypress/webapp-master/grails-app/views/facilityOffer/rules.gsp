<%@ page import="com.matchi.Facility" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="offer.${params.type}.label"/></title>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link mapping="${params.type}" action="index"><g:message code="offer.${params.type}.label"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCoupon.rules.message7" args="[coupon.name]"/></li>
</ul>

<g:errorMessage bean="${coupon}"/>

<div class="action-bar">
    <div class="btn-toolbar-right">
        <g:link mapping="${params.type}" action="ruleForm" id="${coupon.id}" class="btn btn-inverse">
            <span><g:message code="facilityCoupon.rules.message3"/></span>
        </g:link>
    </div>
</div>

<table class="table table-striped table-bordered">
    <thead>
    <tr height="34">
        <th><g:message code="default.name.label"/></th>
        <th width="130" class="center-text"><g:message code="facilityCoupon.rules.message5"/></th>
        <th width="100" class="center-text"><g:message code="default.price.label"/></th>
        <th width="30" class="center-text">&nbsp;</th>
    </tr>
    </thead>

    <tbody>
    <g:each in="${coupon.couponConditionGroups}">
        <tr>
            <td>${it.name?:"-"}</td>
            <td>${it.slotConditionSets.size()}<g:message code="unit.st"/></td>
            <td></td>
            <td class="center-text"><g:link mapping="${params.type}" action="ruleForm" id="${coupon.id}" params="[groupId: it.id]"><img src="${resource(dir:'images', file:'edit_btn.png')}"/></g:link></td>
        </tr>
    </g:each>
    </tbody>

</table>
</body>
</html>
