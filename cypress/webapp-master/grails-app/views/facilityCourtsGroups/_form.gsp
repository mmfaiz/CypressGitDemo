<%@ page import="com.matchi.Facility"%>
<div class="control-group">
    <label class="control-label" for="name"><g:message code="courtGroup.name.label"/></label>
    <div class="controls">
        <g:textField name="name" value="${courtGroupInstance?.name}" class="span8"/>
    </div>
</div>
<g:if test="${facility.hasBookingLimitPerCourtGroup()}">
    <div class="control-group">
        <div class="controls">
            <p class="help-block"><g:message code="courtGroup.label.name.explanation"/></p>
        </div>
    </div>
</g:if>
<g:if test="${facility.hasBookingLimitPerCourtGroup()}">
    <div class="control-group">
        <label class="control-label" for="name"><g:message code="courtGroup.label.visible"/></label>

        <div class="controls">
            <label class="checkbox">
                <g:checkBox name="visible" value="${courtGroupInstance?.visible ?: false}"/>
            </label>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="name"><g:message code="courtGroup.label.restriction"/></label>

        <div class="controls">
            <g:field type="number" name="maxNumberOfBookings"
                     value="${courtGroupInstance?.maxNumberOfBookings}" min="0" max="${Integer.MAX_VALUE}"
                     class="form-control"/>
        </div>
    </div>
</g:if>

<hr/>

<div class="control-group">
    <label class="control-label" for="name"><g:message code="courtGroup.courts.label"/></label>
    <div class="controls">
        <g:each in="${com.matchi.Court.available(getUserFacility()).list()}" var="court" status="i">
            <label class="checkbox">
                <g:checkBox name="courts" value="${court.id}"
                        checked="${courtGroupInstance?.courts.find {it.id == court.id}}"/>
                ${court.name}
            <g:if test="${facility.hasBookingLimitPerCourtGroup()}">
                <g:if test="${court.belongsToOtherCourtGroup(courtGroupInstance)}">
                    <i class="fas fa-exclamation-circle text-warning" rel="tooltip" title="${message(code: 'courtGroup.label.courtGroupBelongs', args: [court.getCourtGroupNames(courtGroupInstance)])}"></i>
                </g:if>
            </g:if>
            </label>
        </g:each>
    </div>
</div>