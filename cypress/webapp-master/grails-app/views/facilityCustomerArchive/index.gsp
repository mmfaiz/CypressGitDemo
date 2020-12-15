<%@ page import="com.matchi.FacilityProperty; com.matchi.Sport; org.joda.time.DateTime; com.matchi.Facility" %>
<g:set var="resetFilterParams" value="${params + [reset: true]}"/>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="customer.label.plural"/></title>
    <r:require modules="jquery-multiselect-widget, matchi-selectall"/>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerArchive.index.message9" args="[archived.getTotalCount()]"/></li>
</ul>

<ul id="customer-tab" class="nav nav-tabs">
    <li><g:link controller="facilityCustomer"><g:message code="customer.label.plural"/></g:link></li>
    <li><g:link controller="facilityCustomerMembers"><g:message code="default.member.label.plural"/></g:link></li>
    <li class="active"><g:link action="index"><g:message code="default.archive.label"/></g:link></li>

    <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_IDROTT_ONLINE.name()}">
        <g:if test="${facility.hasIdrottOnlineActivitySync()}">
            <li class="pull-right"><g:link action="iOSyncStatusActivities" controller="facilityCustomerMembers"><g:message code="facilityCustomer.iosync.activities.label"/></g:link></li>
        </g:if>
        <li class="pull-right"><g:link action="iOSyncStatusMembers" controller="facilityCustomerMembers"><g:message code="facilityCustomer.iosync.label"/></g:link></li>
    </g:ifFacilityPropertyEnabled>
</ul>

<g:render template="/facilityCustomer/listFilter"/>

<div class="action-bar">
    <div class="btn-toolbar-left">
        <div class="btn-group">
            <button class="btn btn-inverse dropdown-toggle" data-toggle="dropdown">
                <g:message code="button.actions.label"/>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu">
                <li><a href="javascript:void(0)" onclick="submitFormTo('#archivedcustomers', '<g:createLink action="unarchive" />');"><g:message code="reactivate.label"/></a></li>
                <li><a href="javascript:void(0)" onclick="submitFormTo('#archivedcustomers', '<g:createLink controller="facilityCustomerRemove" action="remove"
                                            params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams),'archived':true]"/>');"><g:message code="facilityCustomerRemove.remove.heading"/></a></li>
            </ul>
        </div>
    </div>
</div>
<g:form name="archivedcustomers" class="no-margin">
    <g:hiddenField name="q" value="${filter?.q}"/>
    <g:each in="${filter?.members}">
        <g:hiddenField name="members" value="${it}"/>
    </g:each>
    <g:each in="${filter?.group}">
        <g:hiddenField name="group" value="${it}"/>
    </g:each>
    <g:each in="${filter?.type}">
        <g:hiddenField name="type" value="${it}"/>
    </g:each>
    <g:each in="${filter?.status}">
        <g:hiddenField name="status" value="${it}"/>
    </g:each>
    <g:each in="${filter?.gender}">
        <g:hiddenField name="gender" value="${it}"/>
    </g:each>
    <g:each in="${filter?.birthyear}">
        <g:hiddenField name="birthyear" value="${it}"/>
    </g:each>
    <g:each in="${filter?.seasons}">
        <g:hiddenField name="seasons" value="${it}"/>
    </g:each>
    <g:each in="${filter?.invoiceStatus}">
        <g:hiddenField name="invoiceStatus" value="${it}"/>
    </g:each>
    <g:each in="${filter?.courses}">
        <g:hiddenField name="courses" value="${it}"/>
    </g:each>
    <g:each in="${filter?.lastActivity}">
        <g:hiddenField name="lastActivity" value="${it}"/>
    </g:each>

    <table id="customer-table" class="table table-striped table-bordered table-hover">
        <thead>
        <tr>
            <th class="center-text nolink" width="20">
                <g:checkBox class="checkbox-selector" name="" onclick="selector();" value="" checked="false"/>
            </th>
            <g:sortableColumn property="number" titleKey="customer.number.label" params="${resetFilterParams}" />
            <g:sortableColumn property="lastname,firstname,companyname" titleKey="default.name.label" params="${resetFilterParams}" />
            <g:sortableColumn property="email" titleKey="customer.email.label" params="${resetFilterParams}" />
            <th><g:message code="customer.telephone.label"/></th>
            <g:if test="${filter.lastActivity}">
                <th><g:message code="last.activity.label"/></th>
            </g:if>
        </tr>
        </thead>

        <g:if test="${archived.size() == 0}">
            <tr>
                <td colspan="5"><i><g:message code="facilityCustomerArchive.index.message8"/></i></td>
            </tr>
        </g:if>

        <tbody data-provides="rowlink">
            <g:each in="${archived}" var="customer">
                <tr>
                    <td class="center-text nolink">
                        <g:checkBox name="customerId" value="${customer.id}" checked="false" class="selector"/>
                    </td>
                    <td>
                        <g:link controller="facilityCustomer" action="show" id="${customer.id}" class="rowlink">${customer.number}</g:link>
                    </td>
                    <td>${customer.fullName()}</td>
                    <td>${customer.email}</td>
                    <td>${customer.telephone?:"-"}</td>
                    <g:if test="${filter.lastActivity}">
                        <td>
                            <g:formatDate date="${customer?.lastActivity}" format="yyyy-MM-dd"/>
                        </td>
                    </g:if>
                </tr>

            </g:each>
        </tbody>
    </table>
</g:form>
<div class="row">
    <div class="span12">
        <g:paginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered" params="${resetFilterParams}"
                                    maxsteps="10" max="100" action="index" total="${archived.getTotalCount()}" />
    </div>
</div>

<r:script>
    $(function() {
        $("#customer-table").selectAll({ max: "${g.forJavaScript(data: filter.max)}", count: "${g.forJavaScript(data: archived.getTotalCount())}", name: "${message(code: 'js.selectall.customers')}" });
    });
</r:script>

</body>
</html>
