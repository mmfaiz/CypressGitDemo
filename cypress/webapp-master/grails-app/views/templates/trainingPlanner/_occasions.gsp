<g:if test="${occasions}">
    <div id="${hour}_hour" class="panel panel-default no-bottom-margin panel-court">
        <div class="panel-heading court-heading">
            <h4 class="no-margin">
                <g:message code="facilityCourse.planning.message8"/> ${hour}
            </h4>
        </div>
        <ul class="occasion-grid">
            <g:each in="${occasions}" var="occasion">
                <g:render template="/templates/trainingPlanner/occasion" model="[occasion:occasion]"/>
            </g:each>
        </ul>
    </div>
</g:if>
