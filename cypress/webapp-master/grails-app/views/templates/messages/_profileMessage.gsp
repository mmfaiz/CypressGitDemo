<g:if test="${!profileComplete || !sportProfileComplete || !enabled}">
    <div class="alert alert-block">
        <a class="close" data-dismiss="alert" href="#"><i class="fas fa-times"></i></a>

        <ul class="alert-list">
            <g:if test="${!profileComplete}">
                <li>
                    <span class="message_label">
                        - <g:link controller="userProfile" action="account">
                            <g:message code="templates.messages.profileMessage.message1"/>
                        </g:link>
                    </span>
                </li>
            </g:if>
            <g:if test="${!sportProfileComplete}">
                <li>
                    <span class="message_label">
                        - <g:link controller="userProfile" action="account">
                            <g:message code="templates.messages.profileMessage.message2"/>
                        </g:link>
                    </span>
                </li>
            </g:if>

            <g:if test="${!enabled}">
                <li>
                    - <span class="message_label"><g:message code="user.noconfirm.label"/></span>
                    <span class="message_text">
                        <g:message code="user.noconfirm.text" args="['länk1', 'länk2']"/>
                    </span>
                </li>
            </g:if>
        </ul>
    </div>
</g:if>
