<%@ page import="org.joda.time.DateTime; com.matchi.membership.Membership; com.matchi.Customer" %>
<div class="alert alert-info alert-membership">
    <button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only"><g:message code="button.close.label"/></span></button>
    <p>
        <span class="fa-stack fa-lg">
            <i class="fas fa-circle fa-stack-2x"></i>
            <i class="fas fa-user-plus fa-stack-1x fa-inverse"></i>
        </span>

        <g:if test="${customer?.type == Customer.CustomerType.ORGANIZATION}">
            <g:message code="membership.request.info.no.membership.isOrganization" />
        </g:if>
        <g:elseif test="${upcomingMembershipAvailable}">
            <g:message code="membership.request.info.upcomingMembership.purchase"/>
            <strong><g:link controller="membershipRequest" action="index" params="[name: facility.shortname]"><g:message code="membership.request.info.no.membership.part2"/></g:link></strong>.
        </g:elseif>
        <g:elseif test="${!membership}">
            <g:message code="membership.request.info.no.membership.part1.apply${facility.isMembershipRequestPaymentEnabled() ? 'AndPay' : ''}"/>
            <strong><g:link controller="membershipRequest" action="index" params="[name: facility.shortname]"><g:message code="membership.request.info.no.membership.part2"/></g:link></strong>.
        </g:elseif>
    </p>
</div>