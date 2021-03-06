<div class="panel-heading no-padding">
    <div class="tabs tabs-style-underline" style="width: 70%;">
        <nav>
            <ul>
                <li class="${params.action == 'index' ? 'active tab-current' : ''}">
                    <g:link action="index">
                        <i class="fas fa-list"></i>
                        <span><g:message code="facilityActivity.tabs.active.label"/></span>
                    </g:link>
                </li>
                <li class="${params.action == 'archivedFacilities' ? 'active tab-current' : ''}">
                    <g:link action="archivedFacilities">
                        <i class="fas fa-list"></i>
                        <span><g:message code="default.disabled.label"/></span>
                    </g:link>
                </li>
            </ul>
        </nav>
    </div>
</div>
