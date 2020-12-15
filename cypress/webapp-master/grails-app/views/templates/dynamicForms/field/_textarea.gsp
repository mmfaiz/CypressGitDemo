<div class="block">
    <div class="form-group col-sm-12">
        <label for="${formField.id}">
            ${formField.label.encodeAsHTML()}<g:if test="${formField.isRequired}">*</g:if>
        </label>
        <textarea id="${formField.id}" name="${formField.id}" rows="3" maxlength="255" class="form-control"
                    ${formField.isRequired ? 'required' : ''}>${params[formField.id.toString()] ?: ''}</textarea>
        <g:if test="${formField.helpText}">
            <span class="help-block">${formField.helpText.encodeAsHTML()}</span>
        </g:if>
    </div>
</div>
