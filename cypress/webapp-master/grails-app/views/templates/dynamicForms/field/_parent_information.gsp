<%@ page import="com.matchi.dynamicforms.FormField" %>

<div class="block parent-information">
    <div class="col-sm-12">
        <h4>${formField.label.encodeAsHTML()}</h4>
        <g:if test="${formField.helpText}">
            <span class="help-block">${formField.helpText.encodeAsHTML()}</span>
        </g:if>
    </div>

    <g:each in="${formField.typeEnum.binder.inputs}" var="input">
        <div class="form-group col-sm-6">
            <label for="${formField.id}.${input}">
                <g:message code="formField.type.PARENT_INFORMATION.${input}"/><g:if test="${formField.isRequired}">*</g:if>
            </label>
            <input type="text" id="${formField.id}.${input}" name="${formField.id}.${input}" maxlength="255" class="form-control"
                        value="${params[formField.id + '.' + input] ?: ''}" ${formField.isRequired ? 'required' : ''}/>
        </div>
    </g:each>
</div>
