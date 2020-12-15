<g:if test="${occasions}">
    <div class="${hour}_hour panel panel-default no-bottom-margin panel-court">
        <div class="panel-heading court-heading">
            <h4 class="no-margin">
                <g:message code="facilityCourse.planning.message8"/> ${hour}
            </h4>
        </div>
        <ul class="occasion-grid">
            <g:each in="${occasions}" var="occasion">
                <g:extraPageBreakerWithClass extraOccasion="${occasion}" position="inside"/>
                <g:render template="/templates/printPlanning/occasion" model="[occasion:occasion]"/>
            </g:each>
        </ul>
    </div>
    <% def nextHour = allOccasions.keySet().find { it > hour } %>
    <g:if test="${nextHour}">
        <g:set var="pairGroupsOccasions" value="${[hour, nextHour].collectEntries { [(it): allOccasions[it]] }}"/>
        <g:set var="isBigSmallGroupsOccasions" value="${pairGroupsOccasions.count { it.value.size() <= 4 } == 1 && pairGroupsOccasions.count { it.value.size() > 4 } == 1}"/>
        <g:extraPageBreakerWithClass occasions="${pairGroupsOccasions}" position="between"
                            isPairSmallGroups="${pairGroupsOccasions.count { it.value.size() <= 4 } > 0 && groupIdx++ % 2 == 0}"/>
        <g:if test="${isBigSmallGroupsOccasions && groupIdx % 2 == 0}">
            <% groupIdx-- %>
        </g:if>
        <g:elseif test="${isBigSmallGroupsOccasions && groupIdx == 0}">
            <% groupIdx++ %>
        </g:elseif>
        <g:elseif test="${isBigSmallGroupsOccasions && groupIdx % 2 != 0}">
            <% groupIdx += 2 %>
        </g:elseif>
    </g:if>
</g:if>
