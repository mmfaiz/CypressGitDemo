<sec:ifLoggedIn>
    <g:ifFacilityAccessible>
        <g:render template="/templates/navigation/bootstrap3/menuFacility"/>
    </g:ifFacilityAccessible>
</sec:ifLoggedIn>
<sec:ifNotLoggedIn>
    <g:render template="/templates/navigation/bootstrap3/menuDefault" />
</sec:ifNotLoggedIn>