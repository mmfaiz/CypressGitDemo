<section class="block block-grey">
    <div class="container top-margin20">
        <g:if test="${baseMembership}">
            <h1 class="page-header"><g:message code="facility.membership.edit"/></h1>
        </g:if>
        <g:else>
            <h1 class="page-header"><g:message code="facility.membership.apply"/></h1>
        </g:else>
        <p class="lead"><g:message code="membershipRequest.header.message1"/></p>
    </div>
    <div class="space-40"></div>
</section>
