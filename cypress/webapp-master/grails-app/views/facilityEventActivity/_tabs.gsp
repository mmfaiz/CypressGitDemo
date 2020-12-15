<div class="panel-heading no-padding">
    <div class="tabs tabs-style-underline">
        <nav>
            <ul>
                <li class="${params.action == 'participants' ? 'active tab-current' : ''}">
                    <g:link action="submissions" id="${eventActivityInstance.id}">
                        <i class="fas fa-list"></i>
                        <span><g:message code="eventActivity.label.plural"/></span>
                    </g:link>
                </li>
                <li class="${params.action == 'edit' || params.action == 'update' ? 'active tab-current' : ''}">
                    <g:link action="edit" id="${eventActivityInstance.id}">
                        <i class="fa fa-pencil"></i>
                        <span><g:message code="button.edit.label"/></span>
                    </g:link>
                </li>
            </ul>
        </nav>
    </div>
</div>
