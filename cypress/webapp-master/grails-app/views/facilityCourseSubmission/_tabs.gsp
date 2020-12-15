<div class="panel-heading no-padding">
    <div class="tabs tabs-style-underline" style="width: 70%;">
        <nav>
            <ul>
                <li class="${params.action == 'index' ? 'active tab-current' : ''}">
                    <g:link action="index">
                        <i class="fas fa-list"></i>
                        <span><g:message code="facilityCourseSubmission.tabs.waiting.label"/></span>
                    </g:link>
                </li>
                <li class="${params.action == 'processed' ? 'active tab-current' : ''}">
                    <g:link action="processed">
                        <i class="fas fa-list"></i>
                        <span><g:message code="facilityCourseSubmission.tabs.processed.label"/></span>
                    </g:link>
                </li>
            </ul>
        </nav>
    </div>
</div>