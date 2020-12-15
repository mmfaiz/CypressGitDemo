<%@ page import="com.matchi.activities.Participant; com.matchi.Customer; com.matchi.excel.ExcelExportManager; com.matchi.FacilityProperty; com.matchi.facility.EventSubmissionCommand;" %>
<g:set var="returnUrl" value="${createLink(absolute: true, action: 'submissions', id: eventActivityInstance.id, params: params.subMap(params.keySet() - 'error' - 'message'))}"/>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="eventActivity.label.plural"/></title>
    <r:require modules="matchi-selectpicker, matchi-selectall"/>
    <r:script>
        $(function() {
            $("#submission-table").selectAll({ max: "50", count: "${g.forJavaScript(data: totalCount)}", name: "deltagare" });

            $("#birthYears").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityEventParticipant.birthyear.selectedText')}",
                selectedTextFormat: 'count'
            });

            $("#memberStatuses").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityEventParticipant.memberstatus.selectedText')}",
                selectedTextFormat: 'count'
            });

            $("#groups").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityEventParticipant.groups.selectedText')}",
                selectedTextFormat: 'count'
            });

            $("#clubs").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityEventParticipant.clubs.selectedText')}",
                selectedTextFormat: 'count'
            });
        });

    </r:script>
</head>

<body>
    <div class="container content-container">
        <ol class="breadcrumb">
            <li><i class="ti-list"></i><g:link action="index"><g:message code="eventActivity.label.plural"/></g:link></li>
            <li class="active">${eventActivityInstance.name}</li>
        </ol>

        <form method="GET" class="form well no-bottom-padding">
            <div class="row">
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${birthYears}" name="birthYears" multiple="multiple"
                              value="${filter?.birthYears}"
                              title="${message(code: 'facilityEventParticipant.birthyear.noneSelectedText')}">
                        <option value="0">
                            <g:message code="facilityEventParticipant.notSpecified" />
                        </option>
                    </g:select>
                </div>
                <div class="form-group col-sm-3 no-top-margin">
                    <g:select from="${EventSubmissionCommand.MemberStatus.list()}" name="memberStatuses" multiple="multiple"
                              valueMessagePrefix="courseParticipant.memberStatus"
                              value="${filter?.memberStatuses}" title="${message(code: 'facilityEventParticipant.memberstatus.noneSelectedText')}"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${groups}" name="groups" multiple="multiple"
                              optionKey="id" optionValue="name"
                              value="${filter?.groups}" title="${message(code: 'facilityEventParticipant.groups.noneSelectedText')}"/>
                </div>
                <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                    <div class="form-group col-sm-2 no-top-margin">
                        <g:select from="${clubs}" name="clubs" multiple="multiple"
                                  value="${filter?.clubs}" title="${message(code: 'facilityEventParticipant.clubs.noneSelectedText')}"/>
                    </div>
                </g:if>
                <div class="form-group col-sm-2 no-top-margin pull-right">
                    <button type="submit" class="btn btn-block btn-info"><g:message code="button.filter.label"/></button>
                </div>
            </div>
        </form>

        <div class="panel panel-default panel-admin">
            <g:render template="tabs"/>

            <div class="panel-header">
                <div class="panel-heading table-header">
                    <div class="row">
                        <div class="col-sm-6">
                            <div class="dropdown">
                                <button class="btn btn-xs btn-white dropdown-toggle" type="button" id="action-btn" data-toggle="dropdown" aria-expanded="true">
                                    <g:message code="button.actions.label"/>
                                    <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" role="menu" aria-labelledby="action-btn">
                                    <li role="presentation" class="dropdown-submenu">
                                        <a role="menuitem" tabindex="-1" href="#"><g:message code="button.export.label"/></a>
                                        <ul class="dropdown-menu">
                                            <li>
                                                <a href="javascript:void(0)" onclick="submitFormTo('#submissions', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomer', targetAction: 'export', exportType: ExcelExportManager.ExportType.COMPLETE])}');">
                                                    <g:message code="facilityCustomer.index.export.complete"/>
                                                </a>
                                            </li>
                                            <li>
                                                <a href="javascript:void(0)" onclick="submitFormTo('#submissions', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomer', targetAction: 'export', exportType: ExcelExportManager.ExportType.IDROTT_ONLINE])}');">
                                                    <g:message code="facilityCustomer.index.export.idrottOnline"/>
                                                </a>
                                            </li>
                                            <li>
                                                <a href="javascript:void(0)" onclick="submitFormTo('#submissions', '${createLink(action: 'exportSubmissions', params: [returnUrl: returnUrl])}');">
                                                    <g:message code="facilityCustomer.index.export.submissionData"/>
                                                </a>
                                            </li>
                                        </ul>
                                    </li>
                                    <li role="presentation">
                                        <a role="menuitem" tabindex="-1" href="javascript:void(0)"
                                           onclick="submitFormTo('#submissions', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomerGroup', targetAction: 'add'])}');">
                                            <g:message code="facility.customer.addToGroup"/>
                                        </a>
                                    </li>
                                    <li role="presentation">
                                        <a role="menuitem" tabindex="-1" href="javascript:void(0)"
                                           onclick="submitFormTo('#submissions', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomerMessage', targetAction: 'message'])}');">
                                            <g:message code="facilityCustomer.index.message25"/>
                                        </a>
                                    </li>
                                    <g:if test="${eventActivityInstance.facility.hasSMS()}">
                                        <li class="presentation">
                                            <a role="menuitem" tabindex="-1" href="javascript:void(0)"
                                               onclick="submitFormTo('#submissions', '${createLink(action: 'customerAction', params: [returnUrl: createLink(absolute: true, params: returnUrl), targetController: 'facilityCustomerSMSMessage', targetAction: 'message'])}');">
                                                <g:message code="facilityCustomer.index.message24"/>
                                            </a>
                                        </li>
                                    </g:if>
                                    <g:if test="${eventActivityInstance.facility.hasApplicationInvoice()}">
                                        <li role="presentation">
                                            <a role="menuitem" tabindex="-1" href="javascript:void(0)"
                                               onclick="submitFormTo('#submissions', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityInvoiceRowFlow', targetAction: 'createInvoiceRow'])}');">
                                                <g:message code="facilityCourseParticipant.index.createInvoiceRow"/>
                                            </a>
                                        </li>
                                    </g:if>
                                    <li role="presentation">
                                        <a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="if (confirm('${message(code: 'button.delete.confirm.message')}')) {submitFormTo('#submissions', '${createLink(action: 'deleteSubmission')}')}">
                                            <g:message code="button.delete.label"/>
                                        </a></li>
                                </ul>
                            </div>
                        </div>
                        <div class="col-sm-6">
                            <div class="text-right">
                                <g:if test="${assignableCourses}">
                                    <div class="dropdown">
                                        <button class="btn btn-xs btn-white dropdown-toggle" type="button"
                                                id="add-participant-btn" data-toggle="dropdown" aria-expanded="true">
                                            <g:message code="facilityCourseParticipant.index.addParticipantTo"/>
                                            <span class="caret"></span>
                                        </button>
                                        <ul class="dropdown-menu dropdown-menu-right" role="menu" aria-labelledby="add-participant-btn">
                                            <g:each in="${assignableCourses}" var="course">
                                                <li role="presentation">
                                                    <g:link role="menuitem" tabindex="-1" controller="form" action="show"
                                                            params="[hash: course.form.hash, returnUrl: returnUrl]">
                                                        ${course.name.encodeAsHTML()}
                                                    </g:link>
                                                </li>
                                            </g:each>
                                        </ul>
                                    </div>
                                </g:if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <g:form name="submissions" class="no-margin">
                <g:hiddenField name="id" value="${eventActivityInstance.id}"/>
                <g:each in="${filter?.birthYears}">
                    <g:hiddenField name="birthYears" value="${it}"/>
                </g:each>
                <g:each in="${filter?.memberStatuses}">
                    <g:hiddenField name="memberStatuses" value="${it}"/>
                </g:each>
                <g:each in="${filter?.groups}">
                    <g:hiddenField name="groups" value="${it}"/>
                </g:each>
                <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                    <g:each in="${filter?.clubs}">
                        <g:hiddenField name="clubs" value="${it}"/>
                    </g:each>
                </g:if>

                <table id="submission-table" class="table table-striped table-hover table-bordered">
                    <thead>
                        <tr>
                            <th width="5%" class="text-center">
                                <g:checkBox class="checkbox-selector" name="selectAll" value="0" checked="false"/>
                            </th>
                            <g:sortableColumn property="c.firstname,c.lastname"
                                    class="vertical-padding10" titleKey="default.name.label"/>
                            <g:sortableColumn property="c.birthyear" titleKey="customer.birthyear.label"/>
                            <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                                <g:sortableColumn property="c.club" titleKey="default.club.label" params="${resetFilterParams}" class="center-text"/>
                            </g:if>
                            <g:sortableColumn property="dateCreated" titleKey="facilityEventParticipant.submissionDate.label"/>
                            <th class="vertical-padding10 text-center" width="5%">
                                <g:message code="membership.label" />
                            </th>
                            <th class="vertical-padding10 text-center" width="5%"><i class="ti-file"></i></th>
                        </tr>
                    </thead>
                    <tbody data-link="row" class="rowlink">
                        <g:each in="${submissions}" status="i" var="submission">
                            <tr>
                                <td class="text-center rowlink-skip">
                                    <g:checkBox name="submissionId" value="${submission.id}"
                                            checked="false" class="selector"/>
                                </td>
                                <td>
                                    <g:link controller="facilityCustomer" action="show"
                                            id="${submission.customer.id}">
                                        ${submission.customer.fullName().encodeAsHTML()}
                                    </g:link>
                                </td>
                                <td>
                                    ${submission.customer.shortBirthYear()}
                                </td>
                                <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                                    <td>${submission.customer.club}</td>
                                </g:if>
                                <td>
                                    ${submission.dateCreated.format('YYYY-MM-dd')}
                                </td>
                                <td>
                                    <g:message code="facilityEventParticipant.memberStatus.${submission.customer.hasActiveMembership() || submission.customer.membership?.inStartingGracePeriod}" />
                                </td>
                                <td class="text-center rowlink-skip">
                                    <g:link controller="facilityForm" action="showSubmission"
                                            id="${submission.id}">
                                        <span class="ti ti-search"></span>
                                    </g:link>
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                    <g:if test="${!submissions}">
                        <tfoot>
                            <tr>
                                <td colspan="3" class="vertical-padding20">
                                    <span class="text-muted text-md"><g:message code="default.noElements"/></span>
                                </td>
                            </tr>
                        </tfoot>
                    </g:if>
                </table>
            </g:form>
        </div>

        <g:if test="${totalCount > resultsPerPage}">
            <div class="text-center">
                <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                        maxsteps="0" max="50" total="${totalCount}"
                        action="submissions" id="${eventActivityInstance.id}"/>
            </div>
        </g:if>
    </div>
</body>
</html>
