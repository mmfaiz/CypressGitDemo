<div class="profile-notification">
    <div class="top-padding10">
        <p>
            <g:message code="templates.messages.completeMatchingMessage.message2"/>

            <g:if test="${!sportProfileComplete}">
                <g:link class="btn btn-primary btn-sm" controller="userProfile" action="index"><g:message code="templates.messages.completeMatchingMessage.message1"/></g:link>
            </g:if>
            <g:else>
                <g:link class="btn btn-primary btn-sm" controller="userProfile" action="edit"><g:message code="templates.messages.completeMatchingMessage.message1"/></g:link>
            </g:else>
        </p>
    </div>
    <a class="close" href="javascript:void(0)" onclick="$('.profile-notification').hide();$('body').removeClass('user-profile-popup');"><i class="fa fa-times-circle"></i></a>
</div>
