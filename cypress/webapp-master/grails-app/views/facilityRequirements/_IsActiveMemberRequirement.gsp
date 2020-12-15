<%@ page import="com.matchi.requirements.IsActiveMemberRequirement" %>
<g:set var="skip" value="${req.value.skip}" />
<div class="control-group">
    <label class="control-label" for="requirements[${req.key}].membershipRequired">
        <g:message code="facilityRequirements.isActiveMembership.label"/> <g:inputHelp title="${message(code: 'facilityRequirements.isActiveMembership.tooltip')}"/>
    </label>
    <g:render template="skipButton" model="${[skip: skip]}" />
    <div class="controls">
        <g:radioGroup name="requirements[${req.key}].membershipRequired" class="${req.key}-element" values="[true, false]"
                      value="${req.value.properties.membershipRequired ?: 'false'}" disabled="${skip}"
                      labels="[message(code: 'facilityRequirements.isActiveMembership.optionRequired'),
                               message(code: 'facilityRequirements.isActiveMembership.optionRequiredNegative')]">
            <label class="radio inline">
                ${it.radio} ${it.label}
            </label>
        </g:radioGroup>
    </div>
</div>