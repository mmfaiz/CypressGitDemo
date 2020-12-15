<g:each in="${conditionGroups}">
    <g:each in="${it.slotConditionSets.sort { it.id }}" var="slotConditionSet">
        <li>
            <g:each in="${slotConditionSet.slotConditions.sort { it.id }}" var="condition">
                <g:slotConditionFormatted condition="${condition}"/><br>
            </g:each>
        </li>
    </g:each>
</g:each>