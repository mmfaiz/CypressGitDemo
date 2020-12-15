<g:each in="${condition.courts}" var="court" status="index">
    ${court.name}<g:if test="${index < condition.courts.size()-1}">,</g:if>
</g:each>


