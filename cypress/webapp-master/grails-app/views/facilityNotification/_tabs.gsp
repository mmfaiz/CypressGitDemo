<div class="panel-heading no-padding">
    <div class="tabs tabs-style-underline" style="width: 70%;">
        <nav>
            <ul>
                <li class="${params.action == 'index' ? 'active tab-current' : ''}">
                    <g:link action="index">
                        <i class="fa fa-list"></i>
                        <span><g:message code="facilityNotification.tabs.active.label"/></span>
                    </g:link>
                </li>
                <li class="${params.action == 'archived' ? 'active tab-current' : ''}">
                    <g:link action="archived">
                        <i class="fa fa-list"></i>
                        <span><g:message code="facilityNotification.tabs.archived.label"/></span>
                    </g:link>
                </li>
            </ul>
        </nav>
    </div>
</div>