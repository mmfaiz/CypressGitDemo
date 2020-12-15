<sec:ifLoggedIn>
    <sec:ifAllGranted roles="ROLE_USER">
        <g:render template="/templates/navigation/bootstrap3/menuUser" />
    </sec:ifAllGranted>
</sec:ifLoggedIn>
<sec:ifNotLoggedIn>
    <g:render template="/templates/navigation/bootstrap3/menuDefault" />
</sec:ifNotLoggedIn>