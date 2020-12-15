<%@ page import="com.matchi.dynamicforms.FormField" %>
<r:require modules="jquery-timepicker"/>

<h3 class="h4"><g:message code="formField.label.plural"/></h3>
<span class="block text-muted top-margin5"><g:message code="templates.customer.dynamicForms.message1"/></span>
<hr/>
<!-- LOAD FORM FIELDS -->
<div id="form-fields">
    <g:each in="${fields}" var="formField" status="i">
        <g:render template="/templates/dynamicForms/formField" model="[formField: formField, fieldInputPrefix: fieldsProperty + '[' + i + '].']"/>
    </g:each>
</div>

<g:render template="/templates/dynamicForms/formField" model="[template: true]"/>

<g:render template="/templates/dynamicForms/formFieldValue" model="[template: true]"/>

<div class="row well no-horizontal-margin">
    <div class="col-xs-6">
    </div><!-- /.col-sm-6 -->
    <div class="col-xs-6 text-right">
        <!-- ADD FIELD BTN -->
        <div class="clearfix">
            <a href="javascript: void(0)" class="btn btn-sm btn-success add-form-field">
                <i class="ti-plus"></i> <g:message code="adminFormTemplate.form.addFormField"/>
            </a>
        </div>
    </div><!-- /.col-sm-6 -->
</div><!-- /.row -->

<r:script>
    $(function() {
        // ADD NEW FORM FIELD FUNCTION
        $(".add-form-field").click(function() {
            var template = $("#form-field-template").clone();
            var index = $("#form-fields > div").length;
            template.attr("id", null);
            template.find(":input[name=label]").attr("required", "required");
            template.find(":input").each(function() {
                $(this).attr("name", "${g.forJavaScript(data: fieldsProperty)}[" + index + "]." + $(this).attr("name"));
            });
            template.find(":input[type=checkbox]").each(function() {
                var checkboxID = $(this).attr("id");
                $(this).attr("id", "${g.forJavaScript(data: fieldsProperty)}[" + index + "]." + checkboxID);
                $(this).next().attr("for", "${g.forJavaScript(data: fieldsProperty)}[" + index + "]." + checkboxID);
            });
            $("#form-fields").append(template);

            template.show();
        });

        // REMOVE ADDED FORM FIELD FUNCTION
        $("#form-fields").on("click", ".delete-form-field", function() {
            $(this).closest(".form-field").remove();
            updateFieldsIndexes();

            // WHEN THE NEW FORM FIELD IS CHANGED TO CHECKBOX, RADIO OR SELECTBOX, ADD OPTION-FIELDS
        }).on("change", ".field-type.form-control", function() {
            var val = $(this).val();
            var formField = $(this).closest(".form-field");
            var fieldValuesWrapper = formField.find(".field-values-wrapper");
            if (val == "${g.forJavaScript(data: FormField.Type.CHECKBOX.name())}" || val == "${g.forJavaScript(data: FormField.Type.RADIO.name())}"
                    || val == "${g.forJavaScript(data: FormField.Type.SELECTBOX.name())}" || val == "${g.forJavaScript(data: FormField.Type.TIMERANGE_CHECKBOX.name())}"
                    || val == "${g.forJavaScript(data: FormField.Type.NUMBER_OF_OCCASIONS.name())}" || val == "${g.forJavaScript(data: FormField.Type.PLAYER_STRENGTH.name())}") {

                if (!fieldValuesWrapper.find(".field-values > div").length) {
                    addFormFieldValue(formField);

                } else {
                    changeFormFieldValueType(formField);
                    fieldValuesWrapper.find(".field-values").empty();
                    addFormFieldValue(formField);

                }
            } else {
                fieldValuesWrapper.find(".field-values").empty();
                fieldValuesWrapper.hide();
            }

            var fieldTextWrapper = formField.find(".field-text-wrapper");
            if (val == "${g.forJavaScript(data: FormField.Type.TERMS_OF_SERVICE.name())}") {
                fieldTextWrapper.show();
            } else {
                fieldTextWrapper.hide();
                fieldTextWrapper.find("textarea").val("");
            }

            // ADD NEW OPTION FIELD ROW
        }).on("click", ".add-form-field-value", function() {
            addFormFieldValue($(this).closest(".form-field"));

            // REMOVE AN OPTION FIELD ROW
        }).on("click", ".delete-form-field-value", function() {
            var fieldValues = $(this).closest(".field-values");
            if (fieldValues.find(".form-field-value").length > 1) {
                //$(this).remove();
                $(this).closest(".form-field-value").remove();
                fieldValues.find("> div").each(function(index) {
                    $(this).find(":input").each(function() {
                        $(this).attr("name", $(this).attr("name").replace(
                                /predefinedValues\[\d+\]/, "predefinedValues[" + index + "]"));
                    });
                });
            } else {
                $(this).parent().find(":input").each(function() {
                    $(this).val("");
                });
            }
        }).on("click", ".move-up", function() {
            var currentRow = $(this).closest(".form-field");
            var prevRow = currentRow.prev();
            if (prevRow.length) {
                prevRow.before(currentRow);
                updateFieldsIndexes();
            }
        }).on("click", ".move-down", function() {
            var currentRow = $(this).closest(".form-field");
            var nextRow = currentRow.next();
            if (nextRow.length) {
                nextRow.after(currentRow);
                updateFieldsIndexes();
            }
        });

        $("#form-fields").find(".field-type").filter(function() {
            return $(this).val() == "${g.forJavaScript(data: FormField.Type.TIMERANGE_CHECKBOX.name())}";
        }).each(function() {
            $(this).closest(".form-field").find(".field-value-min-max-wrapper").find(":input").addTimePicker({
                hourText: '${message(code: 'default.timepicker.hour')}',
                minuteText: '${message(code: 'default.timepicker.minute')}'
            });
        });
    });

    function updateFieldsIndexes() {
        $("#form-fields > div").each(function(index) {
            $(this).find(":input").each(function() {
                $(this).attr("name", $(this).attr("name").replace(
                    /${g.forJavaScript(data: fieldsProperty)}\[\d+\]/, "${g.forJavaScript(data: fieldsProperty)}[" + index + "]"));
            });
        });
    }

    function addFormFieldValue(formField) {
        var fieldValuesWrapper = formField.find(".field-values-wrapper");
        var template = $("#form-field-value-template").clone(true, true);
        var index = fieldValuesWrapper.find(".field-values > div").length;
        template.attr("id", null);
        template.find(":input[name=value]").attr("required", "required");
        template.find(":input").each(function() {
            var name = "${g.forJavaScript(data: fieldsProperty)}[" + formField.index()
                    + "].predefinedValues[" + index + "]." + $(this).attr("name");
            $(this).attr("name", name).attr("id", name);
        });
        if (formField.find(".field-type").val() == "${g.forJavaScript(data: FormField.Type.TIMERANGE_CHECKBOX.name())}") {
            template.find(".field-value-min-max-wrapper").show();
        }
        if (formField.find(".field-type").val() == "${g.forJavaScript(data: FormField.Type.NUMBER_OF_OCCASIONS.name())}") {
            template.find(":input").each(function() {
                $(this).attr("type","number").attr("onkeydown","javascript: return event.keyCode != 69");
            });
        }
        template.find(".field-value-min-max-wrapper").find(":input").addTimePicker({
            hourText: '${message(code: 'default.timepicker.hour')}',
            minuteText: '${message(code: 'default.timepicker.minute')}'
        });
        fieldValuesWrapper.find(".field-values").append(template);
        template.show();
        fieldValuesWrapper.show();
    }

    function changeFormFieldValueType(formField) {
        if (formField.find(".field-type").val() == "${g.forJavaScript(data: FormField.Type.TIMERANGE_CHECKBOX.name())}") {
            formField.find(".field-value-min-max-wrapper").show();
        } else {
            formField.find(".field-value-min-max-wrapper").hide().find(":input").each(function() {
                $(this).val("");
            });
        }
    }
</r:script>
