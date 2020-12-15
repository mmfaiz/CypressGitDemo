<%@ page import="com.matchi.courses.EditParticipantService; com.matchi.invoice.Invoice; com.matchi.excel.ExcelExportManager; org.joda.time.DateTime; com.matchi.facility.FilterCustomerCommand; com.matchi.Customer; com.matchi.membership.Membership; com.matchi.Sport; org.joda.time.DateTime; com.matchi.Facility; com.matchi.FacilityProperty; org.joda.time.LocalTime" %>
<g:set var="returnUrlParams" value="${params.subMap(params.keySet() - 'error' - 'message')}"/>
<g:set var="resetFilterParams" value="${filter.properties + [reset: true]}"/>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="customer.label.plural"/></title>
    <r:require modules="jquery-multiselect-widget, matchi-selectall"/>
</head>
<body>
<ul class="breadcrumb">
    <li><g:message code="facilityCustomer.index.message15" args="[customers.getTotalCount()]"/></li>
</ul>

<g:render template="/templates/messages/webflowMessage"/>

<ul class="nav nav-tabs">
    <li class="active"><g:link action="index"><g:message code="customer.select.all"/></g:link></li>
    <li><g:link controller="facilityCustomerMembers"><g:message code="default.member.label.plural"/></g:link></li>
    <li><g:link controller="facilityCustomerArchive" action="index"><g:message code="default.archive.label"/></g:link></li>

    <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_IDROTT_ONLINE.name()}">
        <g:if test="${facility.hasIdrottOnlineActivitySync()}">
            <li class="pull-right"><g:link action="iOSyncStatusActivities" controller="facilityCustomerMembers"><g:message code="facilityCustomer.iosync.activities.label"/></g:link></li>
        </g:if>
        <li class="pull-right"><g:link action="iOSyncStatusMembers" controller="facilityCustomerMembers"><g:message code="facilityCustomer.iosync.label"/></g:link></li>
    </g:ifFacilityPropertyEnabled>
</ul>

<g:render template="listFilter"/>

<div class="action-bar">
    <div class="btn-toolbar-left">
        <div class="btn-group">
            <button class="btn btn-inverse dropdown-toggle" data-toggle="dropdown">
                <g:message code="button.actions.label"/>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu">
                <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerArchive" action="archive" />');"><g:message code="button.archive.label"/></a></li>
                <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerInvite" action="invite"
                                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="button.invite.label"/></a></li>
                <li class="dropdown-submenu">
                    <a tabindex="-1" href="#"><g:message code="facility.customer.nav.groupMgmt"/></a>
                    <ul class="dropdown-menu">
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerGroup" action="add"
                                params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facility.customer.nav.groupMgmt.add"/></a></li>
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerGroup" action="remove"
                                params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facility.customer.nav.groupMgmt.remove"/></a></li>
                    </ul>
                </li>
                <g:if test="${facility.hasTrainingPlanner()}">
                    <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCourseParticipantFlow" action="editParticipant"
                                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams), actionTitle: EditParticipantService.ADD_PARTICIPANTS_TITLE]"/>');"><g:message code="facilityCourseParticipation.addParticipants.singleActionLabel"/></a></li>
                </g:if>
                <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityOfferFlow" action="bulkAdd"
                                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facilityOffer.bulkAdd.title"/></a></li>
                <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMembersFlow" action="addMembership"
                                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facilityCustomerMembers.addMembership.title"/></a></li>
                <g:if test="${facility.hasApplicationInvoice()}">
                    <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityInvoiceRowFlow" action="createInvoiceRow"
                                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facilityCustomer.index.message8"/></a></li>
                </g:if>
                <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMessage" action="message"
                                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facilityCustomer.index.message25"/></a></li>
                <g:if test="${facility.hasSMS()}">
                    <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerSMSMessage" action="message"
                                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facilityCustomer.index.message24"/></a></li>
                </g:if>
                <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMerge" action="merge" />');"><g:message code="facilityCustomerMerge.merge.heading"/></a></li>

                <li class="divider"></li>
                <li class="dropdown-submenu">
                    <a tabindex="-1" href="#"><g:message code="button.export.label"/></a>
                    <ul class="dropdown-menu">
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomer" action="export" params="[exportType: ExcelExportManager.ExportType.COMPLETE]" />');">
                            <g:message code="facilityCustomer.index.export.complete"/></a></li>
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomer" action="export" params="[exportType: ExcelExportManager.ExportType.IDROTT_ONLINE]" />');">
                            <g:message code="facilityCustomer.index.export.idrottOnline"/></a></li>
                        <g:if test="${facility.isNorwegian()}">
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomer" action="export" params="[exportType: ExcelExportManager.ExportType.NIF]" />');">
                            <g:message code="facilityCustomer.index.export.nif"/></a></li>
                        </g:if>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
    <div class="btn-toolbar-right">
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <g:link controller="facilityCustomerImport" action="index" class="btn btn-inverse">
                <span><g:message code="facilityCustomerImport.import.title"/></span>
            </g:link>
        </sec:ifAnyGranted>

        <g:link action="create" class="btn btn-inverse">
            <span><g:message code="button.add.label"/></span>
        </g:link>
    </div>
</div>
<g:form name="customers" class="no-margin">
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
    <g:each in="${filter?.clubs}">
        <g:hiddenField name="clubs" value="${it}"/>
    </g:each>
    <g:each in="${filter?.lastActivity}">
        <g:hiddenField name="lastActivity" value="${it}"/>
    </g:each>

    <table id="customer-table" class="table table-striped table-bordered table-hover">
        <thead>
        <tr>
            <th class="center-text nolink" width="20">
                <g:checkBox class="checkbox-selector" name="selectAll" value="0" checked="false"/>
            </th>
            <g:sortableColumn property="number" titleKey="customer.number.label" params="${resetFilterParams}" width="40"/>
            <g:sortableColumn property="firstname" titleKey="default.name.label" params="${resetFilterParams}"/>
            <g:sortableColumn property="birthyear" titleKey="customer.birthyear.label" params="${resetFilterParams}"/>
            <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                <g:sortableColumn property="club" titleKey="default.club.label" params="${resetFilterParams}" class="center-text"/>
            </g:if>
            <g:sortableColumn property="email" titleKey="customer.email.label" params="${resetFilterParams}"/>
            <g:sortableColumn property="telephone" titleKey="customer.telephone.label" params="${resetFilterParams}"/>
            <th class="center-text"><g:message code="membership.label"/></th>
            <g:if test="${filter.lastActivity}">
                <th><g:message code="last.activity.label"/></th>
            </g:if>
        </tr>
        </thead>

        <g:if test="${customers.size() == 0}">
            <tr>
                <td colspan="5"><i><g:message code="facilityCustomer.index.message14"/></i></td>
            </tr>
        </g:if>
        <tbody data-provides="rowlink">
        <g:each in="${customers}" var="customer">
            <tr>
                <td class="center-text nolink">
                    <g:checkBox name="customerId" value="${customer.id}" checked="false" class="selector"/>
                </td>
                <td>
                    <g:if test="${customer.facility?.isMasterFacility() && !facility?.isMasterFacility() && customer.user}">
                        <g:link action="show" id="${customer.id}" class="rowlink"><i rel="tooltip"
                                                                                     title="${message(code: 'facilityCustomer.edit.message2', args: [customer.facility.name])}"
                                                                                     class="icon-globe"></i></g:link>
                    </g:if>
                    <g:elseif test="${customer.getCountOfMasterFacilityCustomers() >= 1}">
                        <g:link action="show" id="${customer.id}" class="rowlink">${customer.number}&nbsp;<i
                            class="icon-globe"></i></g:link>
                    </g:elseif>
                    <g:elseif test="${customer.getCountOfMemberFacilityCustomers() >= 1}">
                        <g:link action="show" id="${customer.id}" class="rowlink">${customer.number}&nbsp;<i
                            class="fas fa-share-alt"></i></g:link>
                    </g:elseif>
                    <g:else>
                        <g:link action="show" id="${customer.id}" class="rowlink">${customer.number}</g:link>
                    </g:else>
                </td>
                <td>
                    <span class="align-middle">${customer.fullName()}</span>
                    <g:if test="${customer.user}">
                        <img src="${resource(dir: "images", file: "favicon.png")}" width="13" height="13"
                                rel="tooltip" title="${message(code: 'customer.user.connected.label')}" alt=""/>
                    </g:if>
                </td>
                <td>
                    ${customer.shortBirthYear()}
                </td>
                <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                    <td>${customer.club}</td>
                </g:if>
                <td>
                    ${customer.email}
                    <g:if test="${customer.clubMessagesDisabled}">
                        <i class="icon-ban-circle transparent-60"></i>
                    </g:if>
                </td>
                <td>${customer.telephone?:customer?.cellphone}</td>

                <td class="center-text">
                    <g:membershipStatus membership="${customer.getMembershipByFilter(filter)}"/>
                    <g:if test="${customer.getCountOfMasterFacilityMemberships() >= 1}">
                        &nbsp;<i rel="tooltip"
                                 title="${message(code: 'facilityCustomer.index.message26')}"
                                 class="icon-globe"></i>
                    </g:if>
                    <g:if test="${customer.getCountOfMemberFacilityMemberships() >= 1}">
                        &nbsp;<i rel="tooltip"
                                 title="${message(code: 'facilityCustomer.index.message26')}"
                                 class="fas fa-share-alt"></i>
                    </g:if>

                </td>

                <g:if test="${filter.lastActivity}">
                    <td>
                        <g:formatDate date="${customer?.lastActivity}" format="yyyy-MM-dd"/>
                    </td>
                </g:if>
            </tr>
        </g:each>
        </tbody>
    </table>
    <div class="row">
        <div class="span12">
            <g:paginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered" params="${resetFilterParams}"
                                        maxsteps="10" max="100" action="index" total="${customers.getTotalCount()}" />
        </div>
    </div>
</g:form>
<r:script>
    var $search = $('#customer-search-input');

    $(function() {
        $("#customer-table").selectAll({ max: "${g.forJavaScript(data: filter.max)}", count: "${g.forJavaScript(data: customers.getTotalCount())}", name: "${message(code: 'js.selectall.customers')}" });

        $("[rel='tooltip']").tooltip();
    });
</r:script>
</body>
</html>
