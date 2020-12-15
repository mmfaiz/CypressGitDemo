<%@ page import="com.matchi.requirements.GroupRequirement; com.matchi.Group" %>
<g:set var="skip" value="${req.value.skip}" />
<div class="control-group">
    <label class="control-label" for="requirements[${req.key}].allSelectedGroups"><g:message code="facilityRequirements.group.allOrSome.label"/> <g:inputHelp title="${message(code: 'facilityRequirements.group.allOrSome.tooltip')}"/></label>
    <g:render template="skipButton" model="${[skip: skip]}" />
    <div class="controls">
        <g:radioGroup name="requirements[${req.key}].allSelectedGroups" values="[false, true]"
                      value="${req.value.properties.allSelectedGroups ?: 'false'}"
                      disabled="${skip}" class="${req.key}-element"
                      labels="[message(code: 'facilityRequirements.group.allOrSome.any.label'),
                               message(code: 'facilityRequirements.group.allOrSome.all.label')]">
            <label class="radio inline">
                ${it.radio} ${it.label}
            </label>
        </g:radioGroup>
    </div>
</div>
<div class="control-group">
    <label class="control-label" for="requirements[${req.key}].groupIds"><g:message code="facilityRequirements.group.chose.label"/> <g:inputHelp title="${message(code: 'facilityRequirements.group.chose.tooltip')}"/></label>
    <div class="controls">
        <g:hiddenField name="requirements[${req.key}].groupIds" value="${com.matchi.StringHelper.LIST_PLACE_HOLDER}" />
        <g:each in="${Group.findAllByFacility(facility)}" var="group">
            <g:checkBox class="${req.key}-element" disabled="${skip}" name="requirements[${req.key}].groupIds" value="${group.id}" checked="${req.value.properties.groupIds.contains(group.id)}" /> ${group.name} <br/>
        </g:each>
    </div>
</div>