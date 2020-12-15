<%@ page import="com.matchi.activities.trainingplanner.CourseActivity; com.matchi.activities.ActivityOccasion" %>
<g:if test="${!customer.archived && assignableCourses}">
    <div class="dropdown pull-right">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false"><i class="icon-plus"></i></a>
        <ul class="dropdown-menu" role="menu">
            <g:each in="${assignableCourses}" var="course">
                <li role="presentation">
                    <g:link role="menuitem" tabindex="-1" action="addToCourse"
                            id="${customer.id}" params="[courseId: course.id]">
                        ${course.name.encodeAsHTML()}
                    </g:link>
                </li>
            </g:each>
        </ul>
    </div>
</g:if>

<p class="lead header">
    <span class="${courseParticipants ? "" : "transparent-60"}">
        <g:message code="facilityCustomer.show.courses" args="[courseParticipants?.size()]"/>
    </span>
    <g:if test="${!courseParticipants}">
        <br>
        <small class="empty"><g:message code="facilityCustomer.show.noCourses"/></small>
    </g:if>
</p>

<g:if test="${courseParticipants}">
    <table class="table table-transparent table-condensed table-noborder table-striped no-bottom-margin">
        <thead class="table-header-transparent">
            <tr>
                <th><g:message code="courseParticipant.course.label"/></th>
                <th><g:message code="courseParticipant.status.label"/></th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${courseParticipants.findAll {it.activity.endDate >= new Date()}}" var="cp">
                <g:set var="occasions" value="${ActivityOccasion.byParticipant(cp).list()}"/>

                <tr>
                    <td>
                        <g:if test="${occasions}">
                            <span class="occasions-toggle">
                                ${cp.activity.name.encodeAsHTML()}
                                <i class="icon-chevron-right"></i>
                            </span>
                        </g:if>
                        <g:else>
                            ${cp.activity.name.encodeAsHTML()}
                        </g:else>
                    </td>
                    <td>
                        <span class="label ${cp.status.cssClass}">
                            <g:message code="courseParticipant.status.${cp.status}"/>
                        </span>
                    </td>
                    <td class="right-text">
                        <g:if test="${cp.submission}">
                            <g:link controller="facilityCourseSubmission" action="show" id="${cp.submission.id}">
                                <i class="icon-search"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="form" action="show" params="${[hash: CourseActivity.get(cp.activity.id)?.form.hash]}">
                                <i class="icon-plus"></i>
                            </g:link>
                        </g:else>
                        <g:link controller="facilityCourseParticipant" action="remove"
                                title="${message(code: 'button.delete.label')}"
                                onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"
                                params="[participantId: cp.id, returnUrl: createLink(absolute: true, action: 'show', id: customer.id)]">
                            <i class="icon-remove"></i>
                        </g:link>
                    </td>
                </tr>

                <g:if test="${occasions}">
                    <tr style="display: none">
                        <td colspan="3" class="occasions">
                            <table class="table table-transparent table-condensed table-noborder no-bottom-margin">
                                <colgroup>
                                    <col width="20%">
                                    <col width="30%">
                                    <col width="50%">
                                </colgroup>
                                <g:each in="${occasions}" var="ocs">
                                    <tr class="rowlink" onclick="showOccasionModal('${ocs.id}')">
                                        <td><g:weekDay date="${ocs.date}"/></td>
                                        <td>
                                            ${ocs.startTime.toDateTime().toString("HH:mm")}
                                            -
                                            ${ocs.endTime.toDateTime().toString("HH:mm")}
                                        </td>
                                        <td>${ocs.court?.name?.encodeAsHTML()}</td>
                                    </tr>
                                    <g:render template="/templates/customer/modal/customerCourseOccasion"
                                              model="[occasion: ocs, customer: customer]"/>
                                </g:each>
                            </table>
                        </td>
                    </tr>
                    %{-- TODO: table-striped fix --}%
                    <tr style="display: none"><td colspan="3"></td></tr>
                </g:if>
            </g:each>

            <g:set var="finishedCoursesParticipants" value="${courseParticipants.findAll {it.activity.endDate < new Date()}}"/>
            <g:if test="${finishedCoursesParticipants}">
                <tr>
                    <td class="finishedCoursesControl" style="cursor: pointer;" colspan="3">
                        <em><g:message code="templates.customer.customerCourse.finishedCourses"
                                args="[finishedCoursesParticipants.size()]"/> <i class="icon-chevron-right"></i></em>
                    </td>
                </tr>

                <g:each in="${finishedCoursesParticipants}" var="cp">
                    <g:set var="occasions" value="${ActivityOccasion.byParticipant(cp).list()}"/>

                    <tr class="finishedCourses" style="display: none">
                        <td>
                            <g:if test="${occasions}">
                                <span class="occasions-toggle">
                                    ${cp.activity.name.encodeAsHTML()}
                                    <i class="icon-chevron-right"></i>
                                </span>
                            </g:if>
                            <g:else>
                                ${cp.activity.name.encodeAsHTML()}
                            </g:else>
                        </td>
                        <td>
                            <span class="label ${cp.status.cssClass}">
                                <g:message code="courseParticipant.status.${cp.status}"/>
                            </span>
                        </td>
                        <td class="right-text">
                            <g:if test="${cp.submission}">
                                <g:link controller="facilityCourseSubmission" action="show" id="${cp.submission.id}">
                                    <i class="icon-search"></i>
                                </g:link>
                            </g:if>
                            <g:link controller="facilityCourseParticipant" action="remove"
                                    title="${message(code: 'button.delete.label')}"
                                    onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"
                                    params="[participantId: cp.id, returnUrl: createLink(absolute: true, action: 'show', id: customer.id)]">
                                <i class="icon-remove"></i>
                            </g:link>
                        </td>
                    </tr>

                    <g:if test="${occasions}">
                        <tr class="finishedCoursesOccasions" style="display: none">
                            <td colspan="3" class="occasions">
                                <table class="table table-transparent table-condensed table-noborder no-bottom-margin">
                                    <colgroup>
                                        <col width="20%">
                                        <col width="30%">
                                        <col width="50%">
                                    </colgroup>
                                    <g:each in="${occasions}" var="ocs">
                                        <tr class="rowlink" onclick="showOccasionModal('${ocs.id}')">
                                            <td><g:weekDay date="${ocs.date}"/></td>
                                            <td>
                                                ${ocs.startTime.toDateTime().toString("HH:mm")}
                                                -
                                                ${ocs.endTime.toDateTime().toString("HH:mm")}
                                            </td>
                                            <td>${ocs.court?.name?.encodeAsHTML()}</td>
                                        </tr>
                                        <g:render template="/templates/customer/modal/customerCourseOccasion"
                                                  model="[occasion: ocs, customer: customer]"/>
                                    </g:each>
                                </table>
                            </td>
                        </tr>
                        %{-- TODO: table-striped fix --}%
                        <tr style="display: none"><td colspan="3"></td></tr>
                    </g:if>
                </g:each>

                <r:script>
                    $(function() {
                        $(".finishedCoursesControl").click(function() {
                            var indicator = $(this).find("i");
                            if(indicator.hasClass("icon-chevron-right")) {
                                indicator.removeClass("icon-chevron-right");
                                indicator.addClass("icon-chevron-down");
                            } else {
                                indicator.removeClass("icon-chevron-down");
                                indicator.addClass("icon-chevron-right");
                                $(".finishedCoursesOccasions").hide();
                            }

                            $(".finishedCourses").toggle();
                        });
                    });
                </r:script>
            </g:if>
        </tbody>
    </table>

    <r:script>
        $(function() {
            $(".occasions-toggle").click(function() {
                $(this).find("i").toggleClass("icon-chevron-right icon-chevron-down");
                $(this).closest("tr").next("tr").toggle();
            });
        });

        var showOccasionModal = function(id) {
            var $modal = $("#occasionModal-" + id);
            $modal.modal({show: true, dynamic: true});
        }
    </r:script>
</g:if>