<%@ page import="com.matchi.activities.Participant; com.matchi.Customer; com.matchi.excel.ExcelExportManager; com.matchi.facility.FilterCourseParticipantCommand; com.matchi.FacilityProperty; com.matchi.courses.EditParticipantService" %>
<g:set var="returnUrl" value="${createLink(absolute: true, action: 'index', params: params.subMap(params.keySet() - 'error' - 'message'))}"/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <g:set var="entityName" value="${message(code: 'courseParticipant.label')}"/>
    <title>MATCHi - <g:message code="courseParticipant.label"/></title>
    <r:require modules="matchi-selectpicker, matchi-selectall"/>
    <r:script>
        $(function() {
            $("#seasons").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.seasons.selectedText')}",
                selectedTextFormat: 'count'
            });
            $("#courses").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.courses.selectedText')}",
                selectedTextFormat: 'count'
            });
            $("#genders").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.genders.selectedText')}",
                selectedTextFormat: 'count'
            });
            $("#groups").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.groups.selectedText')}",
                selectedTextFormat: 'count'
            });
            $("#statuses").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.statuses.selectedText')}",
                selectedTextFormat: 'count'
            });
            $("#occasions").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.occasions.selectedText')}",
                selectedTextFormat: 'count'
            });
            $("#wantedOccasions").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.wantedOccasions.selectedText')}",
                selectedTextFormat: 'count'
            });
            $("#memberStatuses").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.memberStatuses.selectedText')}",
                selectedTextFormat: 'count'
            });
            $('#pickup').selectpicker();
            $('#allergies').selectpicker();
            $('#hasSubmission').selectpicker();


            $("#participant-table").selectAll({ max: "${g.forJavaScript(data: filter.max)}", count: "${g.forJavaScript(data: totalCount)}", name: "deltagare" });
        });

        function deleteParticipants() {
            var participantsWithOccasions = $("#participants").find("input[name=participantId]:checked")
                    .filter("[data-occasions=true]").length;
            if (!participantsWithOccasions || confirm("${message(code: 'facilityCourseParticipant.remove.confirm')}")) {
                submitFormTo("#participants", "${g.forJavaScript(data: createLink(controller: 'facilityCourseParticipant',
             action: 'remove', params: [returnUrl: returnUrl]))}");
            }
        }
    </r:script>
</head>

<body>

<g:render template="/templates/messages/bootstrap3/webflowMessage"/>

<div class="container content-container">
    <ol class="breadcrumb">
        <li><i class="ti-list"></i> <g:message code="courseParticipant.label"/> (${totalCount}<g:message code="unit.st"/>)</li>
    </ol>

    <g:if test="${!facility?.isMasterFacility()}">
        <form method="GET" class="form well no-bottom-padding">
            <g:hiddenField name="max" value="${filter.max}" />
            <div class="row">
                <div class="form-group col-sm-2 no-top-margin">
                    <g:textField name="q" value="${filter?.q}" class="form-control"
                                 placeholder="${message(code: 'facilityCourseParticipant.index.query.placeholder')}"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${seasons}" name="seasons" value="${filter?.seasons}"
                              optionKey="id" optionValue="name" multiple="multiple"
                              title="${message(code: 'season.multiselect.noneSelectedText')}"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${courses}" name="courses" value="${filter?.courses}"
                              optionKey="id" optionValue="${{ course -> course.isArchived() ? "${message(code: 'facilityActivity.tabs.archived.label.singular')} - ${course.name}" : "${course.name}" }}" multiple="multiple"
                              title="${message(code: 'facilityCourseParticipant.index.courses.noneSelectedText')}"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${[Customer.CustomerType.MALE, Customer.CustomerType.FEMALE]}" name="genders"
                              value="${filter?.genders}" valueMessagePrefix="customer.type" multiple="multiple"
                              title="${message(code: 'facilityCourseParticipant.index.genders.noneSelectedText')}"/>
                </div>
                <g:if test="${facilityGroups.size() > 0}">
                    <div class="form-group col-sm-2 no-top-margin">
                        <select id="groups" name="groups" multiple="true"
                                title="${message(code: 'group.multiselect.noneSelectedText')}">
                            <option value="0" ${filter.groups.contains(0L) ? "selected" : ""}>
                                <g:message code="facilityCustomer.index.noGroup"/>
                            </option>
                            <g:each in="${facilityGroups}">
                                <option value="${it.id}" ${filter.groups.contains(it.id) ? "selected":""}>${it.name.encodeAsHTML()}</option>
                            </g:each>
                        </select>
                    </div>
                </g:if>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${Participant.Status.listUsed()}" name="statuses" value="${filter?.statuses}"
                              valueMessagePrefix="courseParticipant.status" multiple="multiple"
                              title="${message(code: 'facilityCourseParticipant.index.statuses.noneSelectedText')}"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${0..7}" name="occasions" value="${filter?.occasions}" multiple="multiple"
                              title="${message(code: 'facilityCourseParticipant.index.occasions.noneSelectedText')}"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${0..7}" name="wantedOccasions" value="${filter?.wantedOccasions}" multiple="multiple"
                              title="${message(code: 'facilityCourseParticipant.index.wantedOccasions.noneSelectedText')}"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${FilterCourseParticipantCommand.MemberStatus.list()}" name="memberStatuses" value="${filter?.memberStatuses}"
                              valueMessagePrefix="courseParticipant.memberStatus" multiple="multiple"
                              title="${message(code: 'facilityCourseParticipant.index.memberStatus.noneSelectedText')}"/>
                </div>
                <g:if test="${pickupCourse}">
                    <div class="form-group col-sm-2 no-top-margin">
                        <g:select from="[true, false]" name="pickup" value="${filter?.pickup}"
                                  valueMessagePrefix="facilityCourseParticipant.index.filter" class="form-control"
                                  noSelection="['': message(code: 'facilityCourseParticipant.index.pickup.noneSelectedText')]"/>
                    </div>
                </g:if>
                <g:if test="${allergyCourse}">
                    <div class="form-group col-sm-2 no-top-margin">
                        <g:select from="[true, false]" name="allergies" value="${filter?.allergies}"
                                  valueMessagePrefix="facilityCourseParticipant.index.filter" class="form-control"
                                  noSelection="['': message(code: 'facilityCourseParticipant.index.allergies.noneSelectedText')]"/>
                    </div>
                </g:if>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="[true, false]" name="hasSubmission" value="${filter?.hasSubmission}"
                              valueMessagePrefix="facilityCourseParticipant.index.filter" class="form-control"
                              noSelection="['': message(code: 'facilityCourseParticipant.index.submission.noneSelectedText')]"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin pull-right">
                    <button type="submit" class="btn btn-block btn-info"><g:message code="button.filter.label"/></button>
                </div>
            </div>
        </form>

        <div class="panel panel-default panel-admin">
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
                                                <a href="javascript:void(0)" onclick="submitFormTo('#participants', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomer', targetAction: 'export', exportType: ExcelExportManager.ExportType.COMPLETE])}');">
                                                    <g:message code="facilityCustomer.index.export.complete"/>
                                                </a>
                                            </li>
                                            <li>
                                                <a href="javascript:void(0)" onclick="submitFormTo('#participants', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomer', targetAction: 'export', exportType: ExcelExportManager.ExportType.IDROTT_ONLINE])}');">
                                                    <g:message code="facilityCustomer.index.export.idrottOnline"/>
                                                </a>
                                            </li>
                                            <li>
                                                <a href="javascript:void(0)" onclick="submitFormTo('#participants', '${createLink(action: 'exportSubmissions', params: [returnUrl: returnUrl])}');">
                                                    <g:message code="facilityCustomer.index.export.submissionData"/>
                                                </a>
                                            </li>
                                        </ul>
                                    </li>
                                    <li role="presentation" class="dropdown-submenu">
                                        <a role="menuitem" tabindex="-1" href="#"><g:message code="facility.customer.nav.groupMgmt"/></a>
                                        <ul class="dropdown-menu">
                                            <li role="presentation">
                                                <a role="menuitem" tabindex="-1" href="javascript:void(0)"
                                                   onclick="submitFormTo('#participants', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomerGroup', targetAction: 'add'])}');">
                                                    <g:message code="facility.customer.nav.groupMgmt.add"/>
                                                </a>
                                            </li>
                                            <li role="presentation">
                                                <a role="menuitem" tabindex="-1" href="javascript:void(0)"
                                                   onclick="submitFormTo('#participants', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomerGroup', targetAction: 'remove'])}');">
                                                    <g:message code="facility.customer.nav.groupMgmt.remove"/>
                                                </a>
                                            </li>
                                        </ul>
                                    </li>
                                    <li class="dropdown-submenu">
                                        <a tabindex="-1" href="#"><g:message code="courseParticipant.status.action"/></a>
                                        <ul class="dropdown-menu">
                                            <li><a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="submitFormTo('#participants',
                                                '<g:createLink controller="facilityCourseParticipant" action="changeStatus"
                                                    params="[status: Participant.Status.ACTIVE, returnUrl: returnUrl]" />');">
                                                <g:message code="courseParticipant.status.ACTIVE"/></a></li>
                                            <li><a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="submitFormTo('#participants',
                                                '<g:createLink controller="facilityCourseParticipant" action="changeStatus"
                                                    params="[status: Participant.Status.PAUSED, returnUrl: returnUrl]" />');">
                                                <g:message code="courseParticipant.status.PAUSED"/></a></li>
                                            <li><a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="submitFormTo('#participants',
                                                '<g:createLink controller="facilityCourseParticipant" action="changeStatus"
                                                    params="[status: Participant.Status.CANCELLED, returnUrl: returnUrl]" />');">
                                                <g:message code="courseParticipant.status.CANCELLED"/></a></li>
                                            <li><a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="submitFormTo('#participants',
                                                '<g:createLink controller="facilityCourseParticipant" action="changeStatus"
                                                    params="[status: Participant.Status.RESERVED, returnUrl: returnUrl]" />');">
                                                <g:message code="courseParticipant.status.RESERVED"/></a></li>
                                        </ul>
                                    </li>
                                    <li role="presentation">
                                        <a role="menuitem" tabindex="-1" href="javascript:void(0)"
                                           onclick="submitFormTo('#participants', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomerMessage', targetAction: 'message'])}');">
                                            <g:message code="facilityCustomer.index.message25"/>
                                        </a>
                                    </li>
                                    <g:if test="${facility.hasSMS()}">
                                        <li class="presentation">
                                            <a role="menuitem" tabindex="-1" href="javascript:void(0)"
                                               onclick="submitFormTo('#participants', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomerSMSMessage', targetAction: 'message'])}');">
                                                <g:message code="facilityCustomer.index.message24"/>
                                            </a>
                                        </li>
                                    </g:if>
                                    <li role="presentation">
                                        <a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="submitFormTo('#participants',
                                                '${createLink(controller: 'facilityCourseParticipantFlow', action: 'sendSchedule', params: [returnUrl: returnUrl])}')">
                                            <g:message code="facilityCourseParticipation.sendSchedule.title"/>
                                        </a>
                                    </li>
                                    <li role="presentation" class="dropdown-submenu">
                                        <a role="menuitem" tabindex="-1" href="#"><g:message code="facilityCourseParticipant.nav.courseActions"/></a>
                                        <ul class="dropdown-menu">
                                            <li role="presentation">
                                                <a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="submitFormTo('#participants',
                                                    '${createLink(controller: 'facilityCourseParticipantFlow', action: 'editParticipant', params: [actionTitle: EditParticipantService.COPY_PARTICIPANTS_TITLE, returnUrl: returnUrl])}')">
                                                    <g:message code="facilityCourseParticipation.copyParticipants.title"/>
                                                </a>
                                            </li>
                                            <li role="presentation">
                                                <a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="submitFormTo('#participants',
                                                    '${createLink(controller: 'facilityCourseParticipantFlow', action: 'editParticipant', params: [actionTitle: EditParticipantService.MOVE_PARTICIPANTS_TITLE, returnUrl: returnUrl])}')">
                                                    <g:message code="facilityCourseParticipation.moveParticipants.title"/>
                                                </a>
                                            </li>
                                        </ul>
                                    </li>
                                    <g:if test="${facility.hasApplicationInvoice()}">
                                        <li role="presentation">
                                            <a role="menuitem" tabindex="-1" href="javascript:void(0)"
                                               onclick="submitFormTo('#participants', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityInvoiceRowFlow', targetAction: 'createInvoiceRow'])}');">
                                                <g:message code="facilityCourseParticipant.index.createInvoiceRow"/>
                                            </a>
                                        </li>
                                    </g:if>
                                    <li role="presentation">
                                        <a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="deleteParticipants()">
                                            <g:message code="button.delete.label"/>
                                        </a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <g:form name="participants" class="no-margin">
                <g:hiddenField name="q" value="${filter?.q}"/>
                <g:each in="${filter?.seasons}">
                    <g:hiddenField name="seasons" value="${it}"/>
                </g:each>
                <g:each in="${filter?.courses}">
                    <g:hiddenField name="courses" value="${it}"/>
                </g:each>
                <g:each in="${filter?.genders}">
                    <g:hiddenField name="genders" value="${it}"/>
                </g:each>
                <g:each in="${filter?.groups}">
                    <g:hiddenField name="groups" value="${it}"/>
                </g:each>
                <g:each in="${filter?.statuses}">
                    <g:hiddenField name="statuses" value="${it}"/>
                </g:each>
                <g:each in="${filter?.occasions}">
                    <g:hiddenField name="occasions" value="${it}"/>
                </g:each>
                <g:each in="${filter?.wantedOccasions}">
                    <g:hiddenField name="wantedOccasions" value="${it}"/>
                </g:each>
                <g:if test="${filter?.pickup != null}">
                    <g:hiddenField name="pickup" value="${filter.pickup}"/>
                </g:if>
                <g:if test="${filter?.allergies != null}">
                    <g:hiddenField name="allergies" value="${filter.allergies}"/>
                </g:if>
                <g:if test="${filter?.hasSubmission != null}">
                    <g:hiddenField name="hasSubmission" value="${filter.hasSubmission}"/>
                </g:if>
                <g:each in="${filter?.memberStatuses}">
                    <g:hiddenField name="memberStatuses" value="${it}"/>
                </g:each>
                <g:if test="${params.sort != null}">
                    <g:hiddenField name="sort" value="${params.sort}"/>
                </g:if>
                <g:if test="${params.order != null}">
                    <g:hiddenField name="order" value="${params.order}"/>
                </g:if>

                <table id="participant-table" class="table table-striped table-hover table-bordered">
                    <thead>
                    <tr>
                        <th width="3%" class="text-center">
                            <g:checkBox class="checkbox-selector" name="selectAll" value="0" checked="false"/>
                        </th>
                        <g:sortableColumn property="c.firstname" params="${params}" class="vertical-padding10" title="${message(code: 'courseParticipant.firstname.label')}"/>
                        <g:sortableColumn property="c.lastname" params="${params}" class="vertical-padding10" title="${message(code: 'courseParticipant.lastname.label')}"/>
                        <g:sortableColumn property="c.birthyear" params="${params}" titleKey="customer.birthyear.label"/>
                        <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                            <g:sortableColumn property="c.club" titleKey="default.club.label" params="${resetFilterParams}" class="center-text"/>
                        </g:if>
                        <g:sortableColumn property="crs.name" params="${params}" class="vertical-padding10" title="${message(code: 'courseParticipant.course.label')}"/>
                        <g:sortableColumn property="occasions" params="${params}"
                                          class="vertical-padding10" titleKey="courseParticipant.occasions.label"/>
                        <g:sortableColumn property="wantedOccasions" params="${params}"
                                          class="vertical-padding10" titleKey="courseParticipant.wantedOccasions.label"/>
                        <g:sortableColumn property="plannedMinutes" params="${params}"
                                          class="vertical-padding10" titleKey="courseParticipant.plannedMinutes.label"/>
                        <g:sortableColumn property="p.status" titleKey="courseParticipant.status.label"
                                          class="vertical-padding10 text-center" params="${params}"/>
                        <th class="vertical-padding10 text-center"><g:message code="membership.label"/></th>
                        <th class="vertical-padding10 text-center" width="3%"><i class="ti-file"></i></th>
                    </tr>
                    </thead>
                    <tbody data-link="row" class="rowlink">
                    <g:each in="${participants}" status="i" var="courseParticipantInstance">
                        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                            <td class="text-center rowlink-skip">
                                <g:checkBox name="participantId" value="${courseParticipantInstance.id}" checked="false" class="selector"
                                            data-occasions="${courseParticipantInstance.occasions ? 'true' : 'false'}"/>
                            </td>
                            <td>
                                <g:link controller="facilityCustomer" action="show" id="${courseParticipantInstance.customerId}">
                                    ${courseParticipantInstance.customerFirstName}
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="facilityCustomer" action="show" id="${courseParticipantInstance.customerId}">
                                    ${courseParticipantInstance.customerLastName}
                                </g:link>
                            </td>
                            <td>
                                ${courseParticipantInstance.birthYear}
                            </td>
                            <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                                <td>${courseParticipantInstance.club}</td>
                            </g:if>
                            <td>${courseParticipantInstance.activity.encodeAsHTML()}</td>
                            <td class="text-center">${courseParticipantInstance.occasions}</td>
                            <td class="text-center">${courseParticipantInstance.wantedOccasions != null ? courseParticipantInstance.wantedOccasions : message(code: "default.na.label") }</td>
                            <td class="text-center">${courseParticipantInstance.plannedMinutes}</td>
                            <td class="text-center">
                                <span class="label ${Participant.Status.valueOf(courseParticipantInstance.status).cssClass}">
                                    <g:message code="courseParticipant.status.${courseParticipantInstance.status}"/>
                                </span>
                            </td>
                            <td class="text-center">
                                <g:membershipStatus membership="${courseParticipantInstance.customerData.getMembershipByFilter(null)}"/>
                            </td>
                            <td class="text-center rowlink-skip">
                                <g:if test="${courseParticipantInstance.submission}">
                                    <g:link action="show" controller="facilityCourseSubmission" id="${courseParticipantInstance.submission}">
                                        <span class="ti ti-search"></span>
                                    </g:link>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                    <g:if test="${!participants}">
                        <tfoot>
                        <tr>
                            <td colspan="6" class="vertical-padding20">
                                <span class="text-muted text-md"><i class="ti-info-alt"></i><g:message code="default.noElements"/></span>
                            </td>
                        </tr>
                        </tfoot>
                    </g:if>
                </table>
            </g:form>
        </div>


        <div class="text-center">
            <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered" showMaxDropdown="${true}" maxDropdownValues="${[10,50,100,250,500]}"
                                          maxsteps="0" max="50" action="index" total="${totalCount}" params="${params}"/>
        </div>

    </g:if>
    <g:else><g:message code="facility.onlyLocal"/></g:else>
</div>
</body>
</html>
