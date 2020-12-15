<g:set var="sports" value="${facility.sports.sort {it.id}}"/>
<div class="form-group row">
    <g:each in="${sports}" var="sport">
        <g:set var="courtTypeEnum" value="${com.matchi.CourtTypeEnum.getBySport(sport).find {it.affectMultiplePlayersPrice} as com.matchi.CourtTypeEnum}" />
        <div class="col-xs-${Math.max(3, 12.intdiv(sports.size()))}">
            <label for="MULTIPLE_PLAYERS_NUMBER.${sport.id}"><g:message code="sport.name.${sport.id}"/></label>
            <g:field type="number" name="MULTIPLE_PLAYERS_NUMBER.${sport.id}" min="2" max="100"
                    value="${facility.getMultiplePlayersNumber()[sport.id.toString()]}" class="form-control"/>
            <g:if test="${courtTypeEnum?.affectMultiplePlayersPrice}">
                <hr style="margin: 10px 0;">
                <p><small><g:message code="facility.property.MULTIPLE_PLAYERS_NUMBER.additionalDescription" />:</small></p>
                <g:each in="${courtTypeEnum.options}" var="option">
                    <p>
                    <label for="MULTIPLE_PLAYERS_NUMBER.${sport.id}_${option}"><g:message code="court.type.${courtTypeEnum.name()}.${option}"/></label>
                    <g:field type="number" name="MULTIPLE_PLAYERS_NUMBER.${sport.id}_${option}" min="2" max="100"
                             value="${facility.getMultiplePlayersNumber()[sport.id.toString()+"_"+option]}" class="form-control"/>
                    </p>
                </g:each>
            </g:if>
        </div>
    </g:each>
</div>