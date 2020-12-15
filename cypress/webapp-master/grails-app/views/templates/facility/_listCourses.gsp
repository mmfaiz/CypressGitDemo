<g:set var="courseReturnUrl" value="${createLink(absolute: false, controller: "facility", action: "show", params: params.subMap(params.keySet() - 'error' - 'message'))}"/>
<ul class="list-courses">
    <g:each in="${courses}" var="course" status="i">
        <li class="vertical-padding20">
            <g:link controller="form" action="show"
                    params="[hash: course.form.hash,
                             returnUrl: courseReturnUrl]" class="btn btn-success btn-xs pull-right left-margin10">
                <g:message code="button.apply.label"/>
            </g:link>
            <h4 class="no-top-margin">
                <g:link controller="form" action="show"
                        params="[hash: course.form.hash,
                                 returnUrl: courseReturnUrl]">${course.name.encodeAsHTML()}</g:link>
            </h4>

            <div class="block text-sm">
                <ul class="list-inline bottom-margin10">
                    <g:if test="${course.form.membershipRequired}">
                        <li>
                            <span class="label label-info">
                                <g:message code="course.membershipRequired.label"/>
                            </span>
                        </li>
                    </g:if>
                    <li>
                        <span class="text-muted"><i class="fa fa-calendar"></i> <g:message code="courseActivity.period.label"/>: </span>
                        <span>
                            <g:formatDate date="${course.startDate}" formatName="date.format.readable.year"/>
                            <g:if test="${course.endDate.after(course.startDate)}">
                                -
                                <g:formatDate date="${course.endDate}" formatName="date.format.readable.year"/>
                            </g:if>
                        </span>
                    </li>
                    <li><g:render template="/templates/activity/activityLevel" model="[activity: course]"/></li>
                    <g:set var="coursePrice" value="${course.form?.price}"/>
                    <g:if test="${coursePrice}">
                        <li>
                            <span class="text-muted"><i class="fas fa-credit-card"></i> <g:message code="courseActivity.price.label"/>: </span>
                            <span><g:formatMoney value="${coursePrice}" facility="${course.facility}"/></span>
                        </li>
                    </g:if>
                </ul>
            </div>

            <g:if test="${course.description}">
                <div class="course-description bottom-margin20 text-sm">${g.toRichHTML(text: course.description)}</div>
            </g:if>

            <div class="block vertical-margin20 text-sm">
                <span class="text-muted"><i class="fa fa-calendar"></i> <g:message code="courseActivity.applicationDeadline.label"/>: </span>
                <span><g:formatDate date="${course.form.activeTo}" formatName="date.format.dateOnly"/></span>
            </div>

            <div class="clearfix"></div>

            <div class="block text-sm">
                <g:each in="${course.trainers}" var="trainer">
                    <div class="media">
                        <div class="media-left">
                            <div class="avatar-circle-xs">
                                <img class="img-responsive" src="${trainer.profileImage?.thumbnailAbsoluteURL ?: resource(dir: 'images', file: 'avatar_default.png')}"/>
                            </div>
                        </div>
                        <div class="media-body">
                            <h5 class="media-heading text-muted no-bottom-margin"><i class="fas fa-user"></i> <g:message code="course.trainers.label"/></h5>
                            <span>${trainer.toString().encodeAsHTML()}</span>
                        </div>
                    </div>
                </g:each>
            </div>

        </li>
    </g:each>
</ul>
