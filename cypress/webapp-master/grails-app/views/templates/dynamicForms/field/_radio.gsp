<div class="block">
    <div class="col-sm-12">
        <h4>${formField.label.encodeAsHTML()}<g:if test="${formField.isRequired}">*</g:if></h4>
        <g:if test="${formField.helpText}">
            <span class="help-block">${formField.helpText.encodeAsHTML()}</span>
        </g:if>
    </div>

    <g:each in="${formField.predefinedValues}" var="pv" status="i">
        <div class="form-group col-sm-12">
            <div class="radio radio-info radio-lg">
                <input type="radio" id="${formField.id}_${i}" name="${formField.id}" value="${pv.value.encodeAsHTML()}"
                            ${params[formField.id.toString()] == pv.value ? 'checked' : ''}/>
                <label for="${formField.id}_${i}">
                    <g:if test="${pv.value}">${pv.value.encodeAsHTML()}</g:if>
                    <g:else>${pv.value.encodeAsHTML()}</g:else>
                </label>
            </div>
        </div>
    </g:each>
</div>
