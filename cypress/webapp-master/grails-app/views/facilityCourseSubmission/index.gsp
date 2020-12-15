<%@ page import="com.matchi.activities.Participant; com.matchi.Customer; com.matchi.excel.ExcelExportManager; com.matchi.facility.FilterSubmissionCommand; com.matchi.FacilityProperty; com.matchi.courses.EditParticipantService" %>
<g:set var="returnUrl" value="${createLink(absolute: true, action: 'index', params: params.subMap(params.keySet() - 'error' - 'message'))}"/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <g:set var="entityName" value="${message(code: 'courseSubmission.label')}"/>
    <title>MATCHi - <g:message code="courseSubmission.label"/></title>
    <r:require modules="matchi-selectpicker, matchi-selectall"/>
    <r:script>
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

        $("#memberStatuses").allselectpicker({
            selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
            countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.memberStatuses.selectedText')}",
            selectedTextFormat: 'count'
        });
        $('#pickup').selectpicker();
        $('#allergies').selectpicker();

        $("#submission-table").selectAll({ max: "${g.forJavaScript(data: cmd.max)}", count: "${g.forJavaScript(data: totalCount)}", name: "anmälningar" });
    </r:script>
</head>

<body>

<g:render template="/templates/messages/bootstrap3/webflowMessage"/>

<div class="container content-container">
    <ol class="breadcrumb">
        <li><i class="ti-list"></i> <g:message code="courseSubmission.label"/> (${totalCount}<g:message code="unit.st"/>)</li>
    </ol>

    <g:if test="${!facility?.isMasterFacility()}">
        <form method="GET" class="form well no-bottom-padding">
            <div class="row">
                <div class="form-group col-sm-2 no-top-margin">
                    <g:textField name="q" value="${cmd?.q}" class="form-control"
                                 placeholder="${message(code: 'facilityCourseParticipant.index.query.placeholder')}"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${courses}" name="courses" value="${cmd?.courses}"
                              optionKey="id" optionValue="${{ course -> course.isArchived() ? "${message(code: 'facilityActivity.tabs.archived.label.singular')} - ${course.name}" : "${course.name}" }}" multiple="multiple"
                              title="${message(code: 'facilityCourseParticipant.index.courses.noneSelectedText')}"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${[Customer.CustomerType.MALE, Customer.CustomerType.FEMALE]}" name="genders"
                              value="${cmd?.genders}" valueMessagePrefix="customer.type" multiple="multiple"
                              title="${message(code: 'facilityCourseParticipant.index.genders.noneSelectedText')}"/>
                </div>
                <div class="form-group col-sm-2 no-top-margin">
                    <g:select from="${FilterSubmissionCommand.MemberStatus.list()}" name="memberStatuses" value="${cmd?.memberStatuses}"
                              valueMessagePrefix="courseParticipant.memberStatus" multiple="multiple"
                              title="${message(code: 'facilityCourseParticipant.index.memberStatus.noneSelectedText')}"/>
                </div>
                <g:if test="${pickupCourse}">
                    <div class="form-group col-sm-2 no-top-margin">
                        <g:select from="[true, false]" name="pickup" value="${cmd?.pickup}"
                                  valueMessagePrefix="facilityCourseParticipant.index.filter" class="form-control"
                                  noSelection="['': message(code: 'facilityCourseParticipant.index.pickup.noneSelectedText')]"/>
                    </div>
                </g:if>
                <g:if test="${allergyCourse}">
                    <div class="form-group col-sm-2 no-top-margin">
                        <g:select from="[true, false]" name="allergies" value="${cmd?.allergies}"
                                  valueMessagePrefix="facilityCourseParticipant.index.filter" class="form-control"
                                  noSelection="['': message(code: 'facilityCourseParticipant.index.allergies.noneSelectedText')]"/>
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
                                    <li role="presentation">
                                        <a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="submitFormTo('#submissions',
                                            '${createLink(controller: 'facilityCourseSubmissionFlow', action: 'accept', params: [returnUrl: returnUrl])}')">
                                            <g:message code="facilityCourseSubmission.accept.process.label"/>
                                        </a>
                                    </li>
                                    <li role="presentation">
                                        <a role="menuitem" tabindex="0" href="javascript:void(0)" onclick="submitFormTo('#submissions',
                                            '${createLink(controller: 'facilityCourseSubmissionFlow', action: 'deny', params: [returnUrl: returnUrl])}')">
                                            <g:message code="facilityCourseSubmission.deny.process.label"/>
                                        </a>
                                    </li>
                                    <g:render template="dropDownOptions"/>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <g:form name="submissions" class="no-margin">
                <g:each in="${cmd?.courses}">
                    <g:hiddenField name="courses" value="${it}"/>
                </g:each>
                <g:each in="${cmd?.genders}">
                    <g:hiddenField name="genders" value="${it}"/>
                </g:each>
                <g:if test="${cmd?.pickup != null}">
                    <g:hiddenField name="pickup" value="${cmd.pickup}"/>
                </g:if>
                <g:if test="${cmd?.allergies != null}">
                    <g:hiddenField name="allergies" value="${cmd.allergies}"/>
                </g:if>
                <g:each in="${cmd?.memberStatuses}">
                    <g:hiddenField name="memberStatuses" value="${it}"/>
                </g:each>
                <g:if test="${params.sort != null}">
                    <g:hiddenField name="sort" value="${params.sort}"/>
                </g:if>
                <g:if test="${params.order != null}">
                    <g:hiddenField name="order" value="${params.order}"/>
                </g:if>
                <g:hiddenField name="processed" value="${Boolean.FALSE}"/>

                <table id="submission-table" class="table table-striped table-hover table-bordered">
                    <thead>
                    <tr>
                        <th width="5%" class="text-center">
                            <g:checkBox class="checkbox-selector" name="selectAll" value="0" checked="false"/>
                        </th>
                        <g:sortableColumn property="c.firstname" params="${params}" class="vertical-padding10" title="${message(code: 'courseParticipant.firstname.label')}"/>
                        <g:sortableColumn property="c.lastname" params="${params}" class="vertical-padding10" title="${message(code: 'courseParticipant.lastname.label')}"/>
                        <g:sortableColumn property="c.birthyear" params="${params}" titleKey="customer.birthyear.label"/>
                        <g:sortableColumn property="crs.name" params="${params}" titleKey="courseSubmission.course.label"/>
                        <g:sortableColumn property="dateCreated" params="${params}" titleKey="courseSubmission.submission.dateCreated.label" defaultOrder="desc"/>
                        <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                            <g:sortableColumn property="c.club" titleKey="default.club.label" params="${params}" class="center-text"/>
                        </g:if>
                        <th class="vertical-padding10 text-center" width="5%"><i class="ti-file"></i></th>
                    </tr>
                    </thead>
                    <tbody data-link="row" class="rowlink">
                    <g:each in="${submissions}" status="i" var="submissionInstance">
                        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                            <td class="text-center rowlink-skip">
                                <g:checkBox name="submissionIds" value="${submissionInstance.id}" checked="false" class="selector" />
                            </td>
                            <td>
                                <g:link controller="facilityCustomer" action="show" id="${submissionInstance.customer.id}">
                                    ${submissionInstance.customer.firstname}
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="facilityCustomer" action="show" id="${submissionInstance.customer.id}">
                                    ${submissionInstance.customer.lastname}
                                </g:link>
                            </td>
                            <td>
                                ${submissionInstance.customer.birthyear}
                            </td>
                            <td>
                                ${submissionInstance.form.course.name}
                            </td>
                            <td>
                                <g:if test="${submissionInstance.originalDate == null}">
                                    ${submissionInstance.dateCreated.format('YYYY-MM-dd')}
                                </g:if>
                                <g:else>
                                    ${submissionInstance.dateCreated.format('YYYY-MM-dd')} <strong>(${submissionInstance.originalDate.format('YYYY-MM-dd')})</strong>
                                </g:else>
                            </td>
                            <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                                <td>${submissionInstance.customer.club}</td>
                            </g:if>
                            <td class="text-center rowlink-skip">
                                <g:link action="show" id="${submissionInstance.id}">
                                    <span class="ti ti-search"></span>
                                </g:link>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                    <g:if test="${!submissions}">
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

        <g:if test="${totalCount > paginationStepSize}">
            <div class="text-center">
                <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                                              maxsteps="0" max="${paginationStepSize}" action="index" total="${totalCount}" params="${params}"/>
            </div>
        </g:if>
    </g:if>
    <g:else><g:message code="facility.onlyLocal"/></g:else>
</div>
</body>
</html>
