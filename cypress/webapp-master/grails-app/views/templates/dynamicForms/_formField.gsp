<%@ page import="com.matchi.dynamicforms.FormField" %>
<div id="${template ? 'form-field-template' : ''}" class="form-field" style="${template ? 'display: none' : ''}">

    <section class="form-module">

        <g:set var="formFieldEditable" value="${(formField?.isEditable && facilityEditing) || !facilityEditing}"/>

        <!-- REMOVE BTN -->
        <g:if test="${formFieldEditable}">
            <a href="javascript: void(0)" class="btn btn-link text-danger pull-right delete-form-field">
                <i class="ti-close"></i> <g:message code="button.delete.label"/>
            </a>
        </g:if>

        <a href="javascript: void(0)" class="move-down pull-right top-padding10"><span class="ti ti-arrow-down"></span></a>
        <a href="javascript: void(0)" class="move-up pull-right top-padding10 right-margin5"><span class="ti ti-arrow-up"></span></a>

        <div class="clearfix"></div>

        <div class="row">

            <!-- SELECT TYPE -->
            <div class="form-group col-sm-6">
                <label for="${fieldInputPrefix ?: ''}type"><g:message code="formField.type.label"/></label>
                <select class="field-type form-control" name="${fieldInputPrefix ?: ''}type" ${formFieldEditable ? '' : 'disabled'}>
                    <g:each in="${FormField.Type.values()*.name()}">
                        <option value="${it}" ${it.equals(formField?.type)?'selected':''}>
                            <g:message code="formField.type.${it}"/>
                        </option>
                    </g:each>
                </select>

                <g:if test="${!formFieldEditable}">
                    <g:hiddenField name="${fieldInputPrefix ?: ''}type" value="${formField?.type}"/>
                </g:if>
            </div>

            <!-- SELECT TYPE HELP TEXT -->
            <div class="col-sm-6">
                <h5 class="text-muted"><g:message code="formField.type.label"/></h5>
                <span class="text-muted"><g:message code="formField.type.description"/></span>
            </div>

        </div><!-- /.row -->

        <div class="row">

            <!-- LABEL HEADING -->
            <div class="form-group col-sm-6">
                <label for="${fieldInputPrefix ?: ''}label"><g:message code="formField.label.label"/>*</label>
                <input type="text" class="form-control" name="${fieldInputPrefix ?: ''}label" value="${formField?.label?.encodeAsHTML()}"
                            maxlength="255" class="span8" ${template ? '' : 'required'} ${formFieldEditable ? '' : 'readonly'}/>
            </div>

            <!-- LABEL HEADING HELP TEXT -->
            <div class="col-sm-6">
                <h5 class="text-muted"><g:message code="formField.label.label"/></h5>
                <span class="text-muted"><g:message code="formField.label.description"/></span>
            </div>

        </div><!-- /.row -->

        <div class="row">

            <!-- HELP TEXT -->
            <div class="form-group col-sm-6">
                <label for="${fieldInputPrefix ?: ''}helpText"><g:message code="formField.helpText.label"/></label>
                <input type="text" class="form-control" name="${fieldInputPrefix ?: ''}helpText" value="${formField?.helpText}"
                            maxlength="255" ${formFieldEditable ? '' : 'readonly'}/>
            </div>

            <!-- HELP TEXT HELP TEXT -->
            <div class="col-sm-6">
                <h5 class="text-muted"><g:message code="formField.helpText.label"/></h5>
                <span class="text-muted"><g:message code="formField.helpText.description"/></span>
            </div>

        </div><!-- /.row -->

        <div class="row field-text-wrapper" style="${formField?.type == FormField.Type.TERMS_OF_SERVICE.name() ? '' : 'display: none'}">

            <!-- FIELD TEXT HEADING -->
            <div class="form-group col-sm-6">
                <label for="${fieldInputPrefix ?: ''}fieldText"><g:message code="formField.fieldText.label"/></label>
                <g:textArea class="form-control" name="${fieldInputPrefix ?: ''}fieldText" value="${g.toRichHTML(text: formField?.fieldText)}" rows="5"/>
            </div>

            <!-- FIELD TEXT HELP TEXT -->
            <div class="col-sm-6">
                <h5 class="text-muted"><g:message code="formField.fieldText.label"/></h5>
                <span class="text-muted"><g:message code="formField.fieldText.description"/></span>
            </div>

        </div><!-- /.row -->

        <!-- CHECKBOX, RADIO OR SELECTBOX, ADD OPTION-FIELDS -->
        <div class="form-group field-values-wrapper" style="${formField?.predefinedValues ? '' : 'display: none'}">

            <div class="controls clearfix">
                <h5 class="title"><g:message code="formField.predefinedValues.label"/></h5>

                <div class="row field-options">
                    <div class="col-sm-8">
                        <div class="field-values">
                            <g:each in="${formField?.predefinedValues}" var="pv" status="pvi">
                                <g:render template="/templates/dynamicForms/formFieldValue"
                                        model="[formFieldValue: pv, type:formField?.type, fieldValueInputPrefix: 'predefinedValues[' + pvi + '].', formFieldEditable:formFieldEditable]"/>
                            </g:each>
                        </div>
                    </div>

                    <!-- ADD OPTIONS ROW BUTTON -->
                    <div class="col-sm-4 top-padding30">
                        <a href="javascript: void(0)" class="btn btn-sm btn-success add-form-field-value">
                            <i class="ti-plus"></i> <g:message code="adminFormTemplate.form.addFormFieldValue"/>
                        </a>
                    </div>
                </div>
            </div>
        </div>
        <div class="checkbox">
            <g:checkBox name="${fieldInputPrefix ?: ''}isRequired" value="${formField?.isRequired}" id="${fieldInputPrefix ?: ''}isRequired"/>
            <label for="${fieldInputPrefix ?: ''}isRequired"><g:message code="formField.isRequired.label"/></label>
        </div>
        <g:if test="${!facilityEditing}">
            <div class="checkbox">
                <g:checkBox name="${fieldInputPrefix ?: ''}isEditable" value="${formField?.isEditable}" id="${fieldInputPrefix ?: ''}isEditable"/>
                <label for="${fieldInputPrefix ?: ''}isEditable"><g:message code="formField.isEditable.label"/></label>
            </div>
        </g:if>
    </section><!-- /.form-module -->
</div>
