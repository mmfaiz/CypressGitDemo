<%@ page import="com.matchi.dynamicforms.FormField" %>
<div id="${template ? 'form-field-value-template' : ''}" class="row form-field-value" style="${template ? 'display: none' : ''}">

    <!-- FIELD VALUE -->
    <div class="col-xs-5">
        <div class="form-group">
            <label><span class="field-value-value-label"><g:message code="formFieldValue.value.label"/></span>*</label>
            <g:if test="${type == FormField.Type.NUMBER_OF_OCCASIONS.name()}">
                <input type="number" name="${fieldInputPrefix ?: ''}${fieldValueInputPrefix ?: ''}value"
                       value="${formFieldValue?.value}" maxlength="255" onkeydown="javascript: return event.keyCode != 69" class="form-control" ${template ? '' : 'required'}/>
            </g:if>
            <g:else>
                <input type="text" name="${fieldInputPrefix ?: ''}${fieldValueInputPrefix ?: ''}value"
                       value="${formFieldValue?.value}" maxlength="255" class="form-control" ${template ? '' : 'required'}/>
            </g:else>
        </div>
    </div>

    <!-- FIELD MIN VALUE -->
    <div class="col-xs-3 field-value-min-max-wrapper" style="${!template && formFieldValue?.field?.type == FormField.Type.TIMERANGE_CHECKBOX.name() ? '' : 'display: none'}">
        <div class="form-group">
            <label><g:message code="formField.minValue.label"/></label>
            <g:textField name="${fieldInputPrefix ?: ''}${fieldValueInputPrefix ?: ''}minValue"
                    value="${formFieldValue?.minValue}" maxlength="255" class="form-control"/>
        </div>
    </div>

    <!-- FIELD MAX VALUE -->
    <div class="col-xs-3 field-value-min-max-wrapper" style="${!template && formFieldValue?.field?.type == FormField.Type.TIMERANGE_CHECKBOX.name() ? '' : 'display: none'}">
        <div class="form-group">
            <label><g:message code="formField.maxValue.label"/></label>
            <g:textField name="${fieldInputPrefix ?: ''}${fieldValueInputPrefix ?: ''}maxValue"
                    value="${formFieldValue?.maxValue}" maxlength="255" class="form-control"/>
        </div>
    </div>

    <!-- REMOVE BTN -->
    <div class="col-xs-1 vertical-padding10 top-margin20">
        <a href="javascript: void(0)" class="btn btn-link text-danger delete-form-field-value">
            <i class="ti-close"></i>
        </a>
    </div>

</div>
