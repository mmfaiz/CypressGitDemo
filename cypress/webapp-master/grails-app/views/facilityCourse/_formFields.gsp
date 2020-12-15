<%@ page import="com.matchi.dynamicforms.FormField" %>
<g:each in="${formFields}" status="index" var="field">
    <div class="form-field-row">
    <section class="row optionSection ${field?.isEditable && field?.isActive ? '' : 'disabled'}">
        <div class="clearfix">
            <div class="isActive-switch" style="float: left;margin-right: 20px">
                <input type="checkbox" name="field.isActive${field.id}" class="checkbox-fields"
                        value="${field?.isActive}" ${field?.isEditable ? '' : 'disabled'} ${field?.isActive ? 'checked' : ''}/>
            </div>

            <div class="pull-right no-vertical-margin left-margin10">
                <a href="javascript: void(0)" class="move-up right-margin5"><span class="ti ti-arrow-up"></span></a>
                <a href="javascript: void(0)" class="move-down"><span class="ti ti-arrow-down"></span></a>
            </div>

            <div class="pull-right checkbox no-vertical-margin horizontal-margin10" >
                <g:checkBox name="field.isRequired${field.id}" value="${field?.isRequired}" disabled="${!field?.isEditable || !field.isActive}" checked="${field?.isRequired}" class="isRequired"/>
                <label for="field.isRequired${field.id}" class="no-horizontal-padding"><g:message code="formField.isRequired.label"/></label>
            </div>

            <div class="form-field-text">
                ${field.label}
                <span class="field-help-text">${field.helpText}</span>
            </div>

        </div>

        <g:if test="${field.typeEnum.customizable}">
            <g:render template="/facilityCourse/field/${field.type.toLowerCase()}" model="[field: field, fieldIdx: field.id,
                    templateField: courseInstance?.form?.relatedFormTemplate?.templateFields?.find {it.type == field.type && it.label == field.label && it.helpText == field.helpText}]"/>
        </g:if>
    </section>
    <g:hiddenField name="formFieldId" value="${field.id}"/>
    <g:hiddenField name="fieldPosition${field.id}" value="${index}"/>
    </div>
</g:each>
