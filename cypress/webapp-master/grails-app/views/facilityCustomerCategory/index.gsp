<%@ page import="com.matchi.price.CustomerGroupPriceCondition; com.matchi.price.MemberTypePriceCondition; com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title>${ facility } - <g:message code="priceListCustomerCategory.label.plural"/></title>
</head>
<body>
<h2><g:message code="facilityCustomerCategory.index.message9" args="[categories?.size()]"/></h2>

<ul class="breadcrumb">
    <li><g:link controller="facilityPriceList" action="index"><g:message code="priceList.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="priceListCustomerCategory.label.plural"/></li>
</ul>

<div class="action-bar">
    <div class="btn-toolbar-right">
        <g:link action="create" class="btn btn-inverse">
            <span><g:message code="button.add.label"/></span>
        </g:link>
    </div>
    <div class="clearfix"></div>
</div>
<table class="table table-striped table-bordered" data-provides="rowlink">
    <thead>
    <tr>
        <th><g:message code="default.name.label"/></th>
        <th><g:message code="facilityCustomerCategory.index.message5"/></th>
        <th width="150" class="center-text">&nbsp;</th>
    </tr>
    </thead>

    <g:if test="${categories?.size() == 0}">
        <tr>
            <td colspan="2"><i><g:message code="facilityCustomerCategory.index.message6"/></i></td>
        </tr>
    </g:if>

    <g:each in="${categories}" var="category">
        <tr>
            <g:if test="${category.defaultCategory}">
                <td><i><b>${category.name}</b></i></td>
                <td><g:message code="facilityCustomerCategory.index.message7"/></td>
                <td class="center-text"><span class="help-inline"><g:message code="facilityCustomerCategory.index.message8"/></span> </td>
            </g:if>
            <g:else>
                <td><g:link action="edit" params="[id: category.id]"><b>${category.name}</b></g:link></td>
                <td>
                    ${category.conditions?.find { it instanceof CustomerGroupPriceCondition }?.groups?.size() ?: 0} grupper /
                    ${category.conditions?.find { it instanceof MemberTypePriceCondition }?.membershipTypes?.size() ?: 0} medlemstyper
                </td>
                <td></td>
            </g:else>

        </tr>
    </g:each>
</table>
</body>
</html>
