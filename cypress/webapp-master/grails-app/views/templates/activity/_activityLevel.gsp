<div class="activity-level" title="${message(code: "activity.level.tooltip")}">
    <g:if test="${activity.levelMin != null}">
        <g:set var="activitySport" value="${activity.guessSport()}" />
        <g:if test="${activitySport}">
            <g:set var="levelHelps" value="${activitySport ? (activity.levelMin..activity.levelMax).collect { it.toString() + ": " + message(code: "sport.skillLevel.${activitySport.id}.${it}")} : []}" />
        </g:if>
        <span title="${activitySport ? activitySport.name + "\n" + levelHelps?.join("\n") : ''}">
            <g:message code="default.activityLevel.label" />:
            <g:if test="${activity.levelMin == activity.levelMax}" >
                ${activity.levelMin}
            </g:if>
            <g:else>
                ${activity.levelMin} - ${activity.levelMax}
            </g:else>
        </span>
    </g:if>
</div>