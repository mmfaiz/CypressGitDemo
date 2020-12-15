    <g:if test="${delta.leftOnly().size() > 0}">
        <g:message code="facilitySeason.openHoursChangeStatus.message1" args="[delta.leftOnly().size()]"/>
    </g:if>
    <g:if test="${delta.rightOnly().size() > 0}">
        <g:message code="facilitySeason.openHoursChangeStatus.message2" args="[delta.rightOnly().size()]"/>
    </g:if>
