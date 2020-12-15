<li id="${occasion.id}_occasion" class="panel panel-occasion panel-occasion-${occasion.activity.hintColor} no-bottom-margin">
    <g:set var="startTime" value="${occasion?.startTime?.toDateTime()?.toString("HH:mm")}"/>
    <g:set var="endTime" value="${occasion?.endTime?.toDateTime()?.toString("HH:mm")}"/>

    <g:hiddenField name="weekDay" value="${occasion?.date?.getDayOfWeek()}"/>
    <g:hiddenField name="court" value="${occasion?.court?.id}"/>
    <g:hiddenField name="courtName" value="${occasion?.court?.name}"/>
    <g:hiddenField name="courseId" value="${occasion?.activity?.id}"/>
    <g:hiddenField name="trainerIds" value="${occasion?.trainers*.id?.join(',')}"/>
    <g:hiddenField name="participantCustomerIds" value="${occasion?.participants*.customer?.id?.join(',')}"/>
    <g:hiddenField name="startTime" value="${startTime}"/>
    <g:hiddenField name="startHour" value="${occasion?.startTime?.hourOfDay}"/>
    <g:hiddenField name="endTime" value="${endTime}"/>
    <g:hiddenField name="occasionDate" value="${occasion?.date}"/>
    <g:hiddenField name="message" value="${occasion?.message}"/>
    <g:hiddenField name="returnUrl" value="${createLink(controller: 'facilityCourse', action: 'planning', params: params)}"/>
    <div class="panel-heading">
        <div class="pull-right">
            <ul class="list-table">
                <g:if test="${occasion?.message}">
                    <li>
                        <span class="icon occasion-message" data-content="${occasion.message.encodeAsHTML()}"><i class="ti ti-comment"></i></span>
                    </li>
                </g:if>
                <li class="dropdown">
                    <a href="#"
                       class="dropdown-toggle"
                       data-toggle="dropdown">
                        <i class="ti ti-pencil-alt"></i>
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li>
                            <a href="javascript:void(0)" onclick="notifyParticipants('${occasion.id}','sms')" role="menuItem" tabindex="-1" rel="tooltip" title="${message(code:"facilityCustomer.index.message24")}">
                                <i class="ti ti-mobile"></i> <g:message code="facility.property.category.SMS.label"/>
                            </a>
                        </li>
                        <li>
                            <a href="javascript:void(0)" onclick="notifyParticipants('${occasion.id}','email')" role="menuItem" tabindex="-1" rel="tooltip" title="${message(code:"facilityCustomer.index.message25")}">
                                <i class="ti ti-email"></i> <g:message code="facilityActivity.create.message15"/>
                            </a>
                        </li>
                    </ul>
                </li>
                <li><a href="javascript:void(0)" class="icon" onclick="editOccasionModal('${occasion.id}')"><i class="ti ti-settings"></i></a></li>
            </ul>
        </div>
        <ul class="list-table">
            <li class="icon">
                <i class="ti ti-time" rel="tooltip" title="${g.toRichHTML(text: startTime)} - ${g.toRichHTML(text: endTime)}"></i>
            </li>
            <li class="text">
                <span class="text-sm">
                    ${occasion?.court?.name}
                </span>
            </li>
        </ul>
    </div>
    <ol id="${occasion?.id}" class="list-matches droppable-item horizontal-padding15">
        <g:each in="${occasion?.trainers}"><li id="${it.id}_trainer" data-trainer-id="${it.id}" class="text-xs draggable-item"><strong>${it.firstName + " " + it.lastName}</strong></li></g:each>
        <g:each in="${occasion?.participants}" var="participant">
            <li id="${participant?.id}" customerId="${participant?.customer?.id}" class="matching-item draggable-item">
                <div class="media">
                    <div class="media-left right-padding10">
                        <div class="avatar-circle-xxs">
                            <g:fileArchiveUserImage id="${participant?.customer?.user?.id}" size="small"/>
                        </div>
                    </div>
                    <div class="media-body weight400">
                        <g:link controller="facilityCustomer" action="show" id="${participant?.customer?.id}"
                                class="text-sm ellipsis none-draggable-item pull-left ${participant.status == com.matchi.activities.Participant.Status.CANCELLED ? 'text-strikethrough' : ''}"
                                style="width: 85%" target="_blank">
                            ${participant}
                        </g:link>
                        <g:if test="${participant.customer.birthyear}">
                            <div class="text-sm text-right pull-right" style="width: 15%">
                                -${participant.customer.birthyear.toString()[-2..-1]}
                            </div>
                        </g:if>
                    </div>
                </div>
            </li>
        </g:each>
    </ol>
</li>
