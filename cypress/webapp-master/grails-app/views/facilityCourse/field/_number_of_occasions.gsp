<div class="row no-left-margin no-right-margin top-margin20 field-values-wrapper-${fieldIdx}">
    <div class="col-sm-4 field-values">
        <g:each in="${field.predefinedValues}" var="pv" status="pvIdx">
            <div class="row form-field-value">
                <div class="col-xs-8">
                    <div class="form-group">
                        <g:field type="number" name="field.${fieldIdx}.predefinedValues[${pvIdx}].value"
                                value="${pv.value}" maxlength="255" required="required" class="form-control"
                                 onkeydown="javascript: return event.keyCode != 69"/>
                    </div>
                </div>

                <div class="col-xs-4">
                    <a href="javascript: void(0)" class="btn btn-link text-danger delete-form-field-value">
                        <i class="ti-close"></i>
                    </a>
                </div>
            </div>
        </g:each>
    </div>

    <div class="col-sm-8">
        <a href="javascript: void(0)" class="btn btn-sm btn-success add-form-field-value">
            <i class="ti-plus"></i> <g:message code="adminFormTemplate.form.addFormFieldValue"/>
        </a>
    </div>
</div>

<g:if test="${!field.isEditable || !field.isActive}">
    <script type="text/javascript">
        $(function() {
            var wrapper = $(".field-values-wrapper-${g.forJavaScript(data: fieldIdx)}");
            wrapper.find(".field-values").find("input").prop("disabled", true);
            wrapper.find("a.btn").hide();
        });
    </script>
</g:if>