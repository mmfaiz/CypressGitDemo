<div class="block">
    <div class="form-group col-sm-12">
        <div class="checkbox checkbox-success checkbox-lg">
            <input type="checkbox" id="${formField.id}.checkmark" name="${formField.id}.checkmark"
                    value="true" ${params.boolean(formField.id + '.checkmark') ? 'checked' : ''}
                                 ${formField.isRequired ? 'required' : ''} data-type="${formField.type}"/>
            <label for="${formField.id}.checkmark">${formField.label.encodeAsHTML()}<g:if test="${formField.isRequired}">*</g:if></label>
        </div>

        <g:textField name="${formField.id}.value" class="form-control" maxlength="255"
                value="${params[formField.id + '.value']}"/>

        <g:if test="${formField.helpText}">
            <span class="help-block">${formField.helpText.encodeAsHTML()}</span>
        </g:if>
    </div>
</div>

<r:script>
    $(function() {
        $("#${g.forJavaScript(data: formField.id)}\\.checkmark").on("click", function() {
            $("#${g.forJavaScript(data: formField.id)}\\.value").prop("disabled", !$(this).is(":checked"));
        });

        <g:if test="${!params.boolean(formField.id + '.checkmark')}">
            $("#${g.forJavaScript(data: formField.id)}\\.value").prop("disabled", true);
        </g:if>

        <g:if test="${formField.isRequired}">
            $("#${g.forJavaScript(data: formField.id)}\\.value").prop("required", true);
        </g:if>
    });
</r:script>
