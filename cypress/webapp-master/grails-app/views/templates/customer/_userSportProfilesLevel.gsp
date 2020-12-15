<p class="lead header">
    <g:message code="default.userSkillLevel" /> - <g:message code="default.average.short" />: ${Math.round(user.getAverageSkillLevel().toDouble() * 100d)/100d}
</p>

<g:each var="sportProfile" in="${user.sportProfiles}">
    <h5 class="no-bottom-margin normal-letter-spacing"><strong>${sportProfile.sport.name} - ${sportProfile.skillLevel}</strong></h5>
    <g:set var="attributesLength" value="${(sportProfile.sportProfileAttributes.size())}" />
    <p class="normal-letter-spacing"><g:each var="attribute" in="${sportProfile.sportProfileAttributes}" status="i">
        <g:message code="sportattribute.name.${attribute.sportAttribute}" />: ${attribute.skillLevel}<g:if test="${i < attributesLength-1}">, </g:if>
    </g:each></p>
</g:each>