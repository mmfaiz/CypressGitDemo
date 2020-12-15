<%@ page import="com.matchi.FacilityProperty; org.joda.time.DateTime; com.matchi.Facility; com.matchi.FacilityProperty.FacilityPropertyKey" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="membershipType.label.plural"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li class="active"><g:message code="facilityMembershipType.index.message5" args="[types.size()]"/></li>
</ul>

<div class="action-bar">
    <div class="btn-toolbar-right">
        <g:link action="create" class="btn btn-inverse"><g:message code="button.add.label"/></g:link>
    </div>
</div>

<table class="table table-striped table-bordered table-hover" data-provides="rowlink">
    <thead>
    <tr>
        <g:sortableColumn property="name" titleKey="membershipType.name.label"/>
        <th style="width: 150px">
            <g:message code="membershipType.validTimeAmount.label"/>
            <g:if test="${!getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP)}">
                <g:message code="membershipType.validTimeAmount.years.label"/>
            </g:if>
        </th>
        <g:sortableColumn property="price" titleKey="membershipType.price.label" style="width: 100px"/>
        <g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT.name()}">
            <g:sortableColumn property="availableOnline" titleKey="membershipType.availableOnline.label"
                    class="center-text" style="width: 150px"/>
        </g:ifFacilityPropertyEnabled>
    </tr>
    </thead>

    <g:if test="${types.size() == 0}">
        <tr>
            <td colspan="4"><i><g:message code="facilityMembershipType.index.message3"/></i></td>
        </tr>
    </g:if>

    <g:each in="${types}" var="type">
        <tr>
            <td>
                <g:link action="edit" id="${type.id}">${type.name}</g:link>
                <g:if test="type.description">
                    <br/>
                    <small>
                    ${type.description}
                    </small>
                </g:if>
            </td>
            <td>
                <g:if test="${type.validTimeAmount && type.validTimeUnit}">
                    ${type.validTimeAmount}
                    <g:if test="${getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP)}">
                        <g:message code="timeUnit.${type.validTimeUnit}"/>
                    </g:if>
                </g:if>
                <g:else>
                    ${type.facility.membershipValidTimeAmount}
                    <g:if test="${getUserFacility().isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_MONTHLY_MEMBERSHIP)}">
                        <g:message code="timeUnit.${type.facility.membershipValidTimeUnit}"/>
                    </g:if>
                </g:else>
            </td>
            <td>${type.price}</td>
            <g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT.name()}">
                <td class="center-text">
                    <g:if test="${type.availableOnline}">
                        <span class="label label-success"><g:message code="default.yes.label"/></span>
                    </g:if>
                    <g:else>
                        <span class="label"><g:message code="default.no.label"/></span>
                    </g:else>
                </td>
            </g:ifFacilityPropertyEnabled>
        </tr>
    </g:each>
</table>
<g:if test="${globalTypes.size() > 0}">
    <ul class="breadcrumb" style="margin-top: 50px;">
        <li class="active">
            <g:message code="facilityMembershipType.index.message6" args="[globalTypes.size()]"/>
        </li>
    </ul>
    <div class="alert alert-desc"><g:message code="facilityMembershipType.index.message7"/></div>
    <table class="table table-striped table-bordered table-hover" data-provides="rowlink">
        <thead>
            <tr class="additional-table">
                <g:sortableColumn property="name" titleKey="membershipType.name.label"/>
                <g:sortableColumn property="price" titleKey="membershipType.price.label" style="width: 100px"/>
            </tr>
        </thead>
        <g:each in="${globalTypes}" var="type">
            <tr>
                <td>
                    ${type.name}
                    <g:if test="type.description">
                        <br/>
                        <small>
                            ${type.description}
                        </small>
                    </g:if>
                </td>
                <td>${type.price}</td>
            </tr>
        </g:each>
    </table>
</g:if>
</body>
</html>
