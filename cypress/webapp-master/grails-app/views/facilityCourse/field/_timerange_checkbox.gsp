<div class="field-values">
    <g:each in="${field.predefinedValues}" var="pv" status="pvIdx">
        <g:hiddenField name="field.${fieldIdx}.predefinedValues[${pvIdx}].value" value="${pv.value}"/>

        <div class="row no-left-margin no-right-margin form-field-value">
            <div class="col-xs-2 checkbox checkbox-success checkbox-lg">
                <g:checkBox name="field.${fieldIdx}.predefinedValues[${pvIdx}].isActive"
                        value="${pv.isActive}" disabled="${!field.isEditable || !field.isActive}"/>
                <label for="field.${fieldIdx}.predefinedValues[${pvIdx}].isActive">${pv.value.encodeAsHTML()}</label>
            </div>
            <div class="col-xs-8">
                <span id="timeRangeLabel${fieldIdx}.${pvIdx}">
                    ${pv.minValue ?: (templateField?.predefinedValues?.get(pvIdx)?.minValue ?: '00:00')}
                    -
                    ${pv.maxValue ?: (templateField?.predefinedValues?.get(pvIdx)?.maxValue ?: '24:00')}
                </span>

                <div id="timeRangeSlider${fieldIdx}.${pvIdx}" class="top-margin10"></div>
            </div>

            <g:hiddenField name="field.${fieldIdx}.predefinedValues[${pvIdx}].minValue"
                value="${pv.minValue ?: (templateField?.predefinedValues?.get(pvIdx)?.minValue ?: '00:00')}"/>
            <g:hiddenField name="field.${fieldIdx}.predefinedValues[${pvIdx}].maxValue"
                    value="${pv.maxValue ?: (templateField?.predefinedValues?.get(pvIdx)?.maxValue ?: '24:00')}"/>
        </div>

        <script type="text/javascript">
            $(function() {
                $("#timeRangeSlider${g.forJavaScript(data: fieldIdx)}\\.${g.forJavaScript(data: pvIdx)}").slider({
                    range: true,
                    min: getMinutes("${g.forJavaScript(data: templateField?.predefinedValues?.get(pvIdx)?.minValue ?: '')}", 0),
                    max: getMinutes("${g.forJavaScript(data: templateField?.predefinedValues?.get(pvIdx)?.maxValue ?: '')}", 1440),
                    step: 30,
                    values: [
                            getMinutes("${g.forJavaScript(data: pv.minValue ?: '')}", 0),
                            getMinutes("${g.forJavaScript(data: pv.maxValue ?: '')}", 1440)
                    ],
                    slide: function(event, ui) {
                        var fromTime = getTime(ui.values[0]);
                        var toTime = getTime(ui.values[1]);
                        $("#field\\.${g.forJavaScript(data: fieldIdx)}\\.predefinedValues\\[${g.forJavaScript(data: pvIdx)}\\]\\.minValue").val(fromTime);
                        $("#field\\.${g.forJavaScript(data: fieldIdx)}\\.predefinedValues\\[${g.forJavaScript(data: pvIdx)}\\]\\.maxValue").val(toTime);
                        $("#timeRangeLabel${g.forJavaScript(data: fieldIdx)}\\.${g.forJavaScript(data: pvIdx)}").html(fromTime + " - " + toTime);
                    }
                });

                $("#field\\.${g.forJavaScript(data: fieldIdx)}\\.predefinedValues\\[${g.forJavaScript(data: pvIdx)}\\]\\.isActive").on("click", function() {
                    $("#timeRangeSlider${g.forJavaScript(data: fieldIdx)}\\.${g.forJavaScript(data: pvIdx)}").slider($(this).is(":checked") ? "enable" : "disable");
                });

                <g:if test="${!field.isEditable || !field.isActive || !pv.isActive}">
                    $("#timeRangeSlider${g.forJavaScript(data: fieldIdx)}\\.${g.forJavaScript(data: pvIdx)}").slider("disable");
                </g:if>
            });
        </script>
    </g:each>
</div>