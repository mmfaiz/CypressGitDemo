<g:set var="eventReturnUrl" value="${createLink(absolute: false, controller: "facility", action: "show", params: params.subMap(params.keySet() - 'error' - 'message'))}"/>
<ul class="list-courses">
    <g:each in="${events}" var="event" status="i">
        <li class="vertical-padding20">
            <g:link controller="form" action="show"
                    params="[hash: event.form.hash,
                             returnUrl: eventReturnUrl]" class="btn btn-success btn-xs pull-right left-margin10">
                <g:message code="button.apply.label"/>
            </g:link>
            <h4 class="no-top-margin">
                <g:link controller="form" action="show"
                        params="[hash: event.form.hash,
                                 returnUrl: eventReturnUrl]">${event.name.encodeAsHTML()}</g:link>
            </h4>

            <div class="block text-sm">
                <ul class="list-inline">
                    <g:if test="${event.form.membershipRequired}">
                        <li>
                            <span class="label label-info">
                                <g:message code="course.membershipRequired.label"/>
                            </span>
                        </li>
                    </g:if>
                    <li>
                        <span class="text-muted"><i class="fa fa-calendar"></i> <g:message code="courseActivity.period.label"/>: </span>
                        <span>
                            <g:formatDate date="${event.startDate}" formatName="date.format.readable.year"/>
                            <g:if test="${event.endDate.after(event.startDate)}">
                                -
                                <g:formatDate date="${event.endDate}" formatName="date.format.readable.year"/>
                            </g:if>
                        </span>
                    </li>
                    <g:set var="eventPrice" value="${event.form?.price}"/>
                    <g:if test="${eventPrice}">
                        <li>
                            <span class="text-muted"><i class="fas fa-credit-card"></i> <g:message code="courseActivity.price.label"/>: </span>
                            <span><g:formatMoney value="${eventPrice}" facility="${event.facility}"/></span>
                        </li>
                    </g:if>
                </ul>
            </div>

            <g:if test="${event.description}">
                <div class="course-description bottom-margin20 text-sm">${g.toRichHTML(text: event.description)}</div>
            </g:if>

            <div class="block vertical-margin20 text-sm">
                <span class="text-muted"><i class="fa fa-calendar"></i> <g:message code="courseActivity.applicationDeadline.label"/>: </span>
                <span><g:formatDate date="${event.form.activeTo}" formatName="date.format.dateOnly"/></span>
            </div>
        </li>
    </g:each>
</ul>
