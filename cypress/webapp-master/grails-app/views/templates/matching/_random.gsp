<%@ page import="com.matchi.sportprofile.SportProfile; com.matchi.User" %>
<g:if test="${!matches}">
    <ul class="list-matches horizontal-padding15"><li><p>
        <g:message code="templates.matching.random.message1" args="[createLink(controller: 'userProfile', action: 'index')]"/>
    </p></li></ul>
</g:if>
<g:else>
    <ul class="list-matches horizontal-padding15">
        <g:each in="${matches}" var="match" status="i">
            <g:render template="/templates/matching/matchCompact" model="[match:match]"/>
        </g:each>
    </ul>
</g:else>
