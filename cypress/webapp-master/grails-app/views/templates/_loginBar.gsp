<div
        style="
        padding: 10px 20px 10px 20px;
        margin-bottom: 0;
        margin-top: 20px;
        border-bottom: 1px solid #ccc;

        overflow: auto">
    <sec:ifNotLoggedIn>

        <span class="pull-left">
            <g:link controller="login" action="index" params="${params + [returnUrl:returnUrl, loginUrl: loginUrl]}"><g:message code="default.navigation.login"/></g:link>
        </span>
        <span class="pull-right">
            <g:link controller="userRegistration" action="index" params="${params + [returnUrl:returnUrl, facilityId: facility?.id]}"><g:message code="templates.loginBar.message2"/></g:link>
        </span>

    </sec:ifNotLoggedIn>
    <sec:ifLoggedIn>

        <span class="left">
            <g:message code="default.loggedin.as.label"/> <b>${user.fullName()}</b>
        </span>

        <span class="right">
            <g:link controller="logout" action="index" params="[returnUrl: returnUrl]"><g:message code="default.navigation.logout"/></g:link>

        </span>
    </sec:ifLoggedIn>
</div>
