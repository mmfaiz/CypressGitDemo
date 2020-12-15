<g:if test="${!customer.archived && assignableActivities}">
    <div class="dropdown pull-right">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false"><i class="icon-plus"></i></a>
        <ul class="dropdown-menu" role="menu">
            <g:each in="${assignableActivities}" var="activity">
                <li role="presentation">
                    <g:link role="menuitem" tabindex="-1" action="redirectToForm"
                            id="${customer.id}" params="[hash: activity.form.hash]">
                        ${activity.name.encodeAsHTML()}
                    </g:link>
                </li>
            </g:each>
        </ul>
    </div>
</g:if>

<p class="lead header">
    <span class="${submissions ? '' : 'transparent-60'}">
        <g:message code="facilityCustomer.show.events" args="[submissions?.size()]"/>
    </span>
    <g:if test="${!submissions}">
        <br>
        <small class="empty"><g:message code="facilityCustomer.show.noEvents"/></small>
    </g:if>
</p>

<g:if test="${submissions}">
    <table class="table table-transparent table-condensed table-noborder table-striped no-bottom-margin">
        <thead class="table-header-transparent">
            <tr>
                <th><g:message code="eventActivity.label"/></th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${submissions?.findAll {it?.form?.event?.endDate >= new Date()}}" var="submission">
                <tr>
                    <td>
                        ${submission?.form?.event?.name?.encodeAsHTML()}
                    </td>
                    <td class="right-text">
                        <g:if test="${submission}">
                            <g:link controller="facilityForm" action="showSubmission"
                                    id="${submission.id}">
                                <i class="icon-search"></i>
                            </g:link>
                        </g:if>
                        <g:link controller="facilityEventActivity" action="deleteSubmission"
                                id="${submission?.form?.event?.id}" title="${message(code: 'button.delete.label')}"
                                onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"
                                params="[submissionId: submission?.id, returnUrl: createLink(absolute: true, action: 'show', id: customer.id)]">
                            <i class="icon-remove"></i>
                        </g:link>
                    </td>
                </tr>
            </g:each>

            <g:set var="finishedEventsSubmissions" value="${submissions?.findAll {it?.form?.event?.endDate < new Date()}}"/>
            <g:if test="${finishedEventsSubmissions}">
                <tr>
                    <td class="finishedEventsControl" style="cursor: pointer;" colspan="3">
                        <em><g:message code="templates.customer.customerEventActivity.finishedEvents"
                                args="[finishedEventsSubmissions.size()]"/> <i class="icon-chevron-right"></i></em>
                    </td>
                </tr>

                <g:each in="${finishedEventsSubmissions}" var="submission">
                    <tr class="finishedEvents" style="display: none">
                        <td>
                            ${submission?.form?.event?.name?.encodeAsHTML()}
                        </td>
                        <td class="right-text">
                            <g:if test="${submission}">
                                <g:link controller="facilityForm" action="showSubmission"
                                        id="${submission.id}">
                                    <i class="icon-search"></i>
                                </g:link>
                            </g:if>
                            <g:link controller="facilityEventActivity" action="deleteSubmission"
                                    id="${submission?.form?.event?.id}" title="${message(code: 'button.delete.label')}"
                                    onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"
                                    params="[submissionId: submission?.id, returnUrl: createLink(absolute: true, action: 'show', id: customer.id)]">
                                <i class="icon-remove"></i>
                            </g:link>
                        </td>
                    </tr>
                </g:each>

                <r:script>
                    $(function() {
                        $(".finishedEventsControl").click(function() {
                            var indicator = $(this).find("i");
                            if(indicator.hasClass("icon-chevron-right")) {
                                indicator.removeClass("icon-chevron-right");
                                indicator.addClass("icon-chevron-down");
                            } else {
                                indicator.removeClass("icon-chevron-down");
                                indicator.addClass("icon-chevron-right");
                            }

                            $(".finishedEvents").toggle();
                        });
                    });
                </r:script>
            </g:if>
        </tbody>
    </table>
</g:if>