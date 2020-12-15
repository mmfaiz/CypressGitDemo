<div class="block">
    <div class="col-sm-12">
        <h4>${formField.label.encodeAsHTML()}<g:if test="${formField.isRequired}">*</g:if></h4>
        <g:if test="${formField.helpText}">
            <span class="help-block">${formField.helpText.encodeAsHTML()}</span>
        </g:if>
    </div>

    <div class="form-group col-sm-12">
        <select id="${formField.id}" name="${formField.id}" class="form-control" ${formField.isRequired ? 'required' : ''}
                title="${message(code: 'default.multiselect.noneSelectedText')}">
            <g:if test="${!formField.isRequired}">
                <option value=""></option>
            </g:if>
            <g:each in="${formField.predefinedValues}" var="pv">
                <option value="${pv.value.encodeAsHTML()}" ${params[formField.id.toString()] == pv.value ? 'selected' : ''}>
                    <g:if test="${pv.value}">${pv.value.encodeAsHTML()}</g:if>
                    <g:else>${pv.value.encodeAsHTML()}</g:else>
                </option>
            </g:each>
        </select>
    </div>
</div>
<r:script>
    $(document).ready(function() {
        $('#${g.forJavaScript(data: formField.id)}').selectpicker();
    });
</r:script>
