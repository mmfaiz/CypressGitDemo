<a href="#" class="dropdown-toggle" data-toggle="dropdown">
    <span><i class="fas fa-envelope"></i>
    <g:if test="${messages.size() > 0}">
        <span class="badge">${messages.size()}</span>
    </g:if>
    <b class="caret"></b></span>
</a>
<ul class="dropdown-menu">
    <g:if test="${messages.size() > 0}">
        <g:each in="${messages}" var="message" status="i">
            <li style="width:360px;">
                <g:link action="conversation" controller="userMessage" params="[id: message.from.id]">
                    <div class="media">
                        <div class="media-left">
                            <div class="avatar-circle-xs">
                                <g:fileArchiveUserImage id="${message.from.id}"/>
                            </div>
                        </div>
                        <div class="media-body">
                            <h5 class="media-heading">${message.from.fullName()}</h5>
                            <div style="width:185px;">
                                <span class="ellipsis block text-sm">${message.message.encodeAsHTML()}</span>
                            </div>
                        </div>
                        <div class="media-right">
                            <g:formatDate format="dd/MM HH:mm" date="${message.dateCreated}" />
                        </div>
                    </div>
                </g:link>
            </li>
        </g:each>
        <li class="divider"></li>
    </g:if>
    <li><g:link controller="userMessage" action="index"><g:message code="userMessage.label.showall"/></g:link></li>
</ul>
