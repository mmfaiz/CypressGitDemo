<%@ page import="org.joda.time.DateTime" %>
<% def now = new DateTime().getMillis() %>

<div class="block">
    <div class="form-group col-sm-12">
        <label for="${formField.id}_${now}">
            ${formField.label.encodeAsHTML()}<g:if test="${formField.isRequired}">*</g:if>
        </label>
        <input type="text" id="${formField.id}_${now}" class="form-control" name="${formField.id}" value="${params[formField.id.toString()] ?: ''}"
                maxlength="255" ${formField.isRequired ? 'required' : ''}/>
        <g:if test="${formField.helpText}">
            <span class="help-block">${formField.helpText.encodeAsHTML()}</span>
        </g:if>
    </div>
</div>
