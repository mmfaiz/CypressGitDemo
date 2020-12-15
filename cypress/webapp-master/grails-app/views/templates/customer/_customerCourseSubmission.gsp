<%@ page import="com.matchi.activities.ActivityOccasion" %>
<g:if test="${!customer.archived && assignableCourses}">
    <div class="dropdown pull-right">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false"><i class="icon-plus"></i></a>
        <ul class="dropdown-menu" role="menu">
            <g:each in="${assignableCourses}" var="course">
                <li role="presentation">
                    <g:link role="menuitem" tabindex="-1" action="redirectToForm"
                            id="${customer.id}" params="[hash: course.form.hash]">
                        ${course.name.encodeAsHTML()}
                    </g:link>
                </li>
            </g:each>
        </ul>
    </div>
</g:if>

<p class="lead header">
    <span class="${submissions ? "" : "transparent-60"}">
        <g:message code="facilityCustomer.show.courseSubmissions" args="[submissions?.size()]"/>
    </span>
    <g:if test="${!submissions}">
        <br>
        <small class="empty"><g:message code="facilityCustomer.show.noCourseSubmissions"/></small>
    </g:if>
</p>

<g:if test="${submissions}">
    <table class="table table-transparent table-condensed table-noborder table-striped no-bottom-margin">
        <thead class="table-header-transparent">
        <tr>
            <th><g:message code="courseParticipant.course.label"/></th>
            <th><g:message code="courseParticipant.status.label"/></th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${submissions}" var="submission">

            <tr>
                <td>
                    <span>
                        ${submission.form.activity.name}
                    </span>
                </td>
                <td>
                    <span class="label ${message(code: "courseSubmission.cssClass.${submission.status}")}">
                        ${message(code: "courseSubmission.submission.status.${submission.status}")}
                    </span>
                </td>
                <td class="right-text">
                    <g:link controller="facilityCourseSubmission" action="show" id="${submission.id}">
                        <i class="icon-search"></i>
                    </g:link>
                </td>
            </tr>
        </g:each>

        </tbody>
    </table>
</g:if>