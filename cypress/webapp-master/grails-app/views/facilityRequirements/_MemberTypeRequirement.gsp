<%@ page import="com.matchi.requirements.MemberTypeRequirement; com.matchi.membership.MembershipType" %>
<g:set var="skip" value="${req.value.skip}" />
<div class="control-group">
    <label class="control-label" for="requirements[${req.key}].typeIds"><g:message code="facilityRequirements.membertypes.label"/> <g:inputHelp title="${message(code: 'facilityRequirements.membertypes.tooltip')}"/></label>
    <g:render template="skipButton" model="${[skip: skip]}" />
    <div class="controls">
        <g:hiddenField name="requirements[${req.key}].typeIds" value="${com.matchi.StringHelper.LIST_PLACE_HOLDER}" />
        <g:each in="${MembershipType.findAllByFacility(facility)}" var="type">
            <g:checkBox class="${req.key}-element" disabled="${skip}" name="requirements[${req.key}].typeIds" value="${type.id}" checked="${req.value.properties.typeIds?.contains(type.id)}" /> ${type.name} <br/>
        </g:each>
    </div>
</div>