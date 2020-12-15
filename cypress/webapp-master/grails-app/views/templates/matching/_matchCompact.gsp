<li class="matching-item">
    <div class="media">
        <div class="media-left">
            <div class="avatar-circle-xs">
                <g:fileArchiveUserImage id="${match.user.id}" size="small"/>
            </div>
        </div>
        <div class="media-body full-width">
            <ul class="list-table weight400">
                <li class="no-left-padding top-padding5 no-bottom-padding right-padding5">
                    <span class="text-xs"><g:skillLevel id="${match?.user?.id}"/></span>
                </li>
                <li class="top-padding5 no-bottom-padding no-left-padding">
                    <g:link controller="userProfile" action="index" id="${match.user.id}">
                        ${match.user.fullName()}
                    </g:link>
                </li>
            </ul>
            <g:if test="${match.user.municipality}">
                <small class="text-muted">
                    <i class="fas fa-map-marker"></i> ${match.user?.municipality}${match.user?.city ? " (" + match.user.city + ")" : ""}
                </small>
            </g:if>
            <g:else>
                <small class="text-muted">
                    <i class="fas fa-map-marker"></i> <g:message code="templates.matching.matchCompact.message1"/>
                </small>
            </g:else>
        <!-- MATCHING PROCENTAGE PROGRESS BAR -->
            <div class="progress progress-thin top-margin10 bottom-margin5" rel="tooltip" data-original-title="${message(code: 'templates.matching.matchCompact.message2')}">
                <div class="progress-bar progress-bar-brand" role="progressbar" data-transitiongoal="${match.matchingValue}" aria-valuenow="${match.matchingValue}" aria-valuemin="0" aria-valuemax="100">
                </div>
            </div>
            <span class="block text-xs text-muted">${match.user.firstname} <g:message code="templates.matching.matchCompact.message3"/> ${match.matchingValue}%</span>
        </div>
        <div class="media-right top-margin5">
            <g:remoteLink class="" controller="message" update="messageModal" onError="handleAjaxError()" onSuccess="showLayer('messageModal')"
                          params="['id':match.user.id, 'returnUrl': g.createLink(absolute: true, controller: 'userProfile', action: 'home', params: [] )]"><i class="fas fa-envelope" rel="tooltip" data-original-title="${message(code: 'templates.matching.matchCompact.message4')} ${g.toRichHTML(text: match.user.firstname)}"></i> </g:remoteLink>
        </div>
    </div>
</li>
