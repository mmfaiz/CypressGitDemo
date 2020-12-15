<div class="block">
    <div class="col-sm-12">
        <h4>${formField.label.encodeAsHTML()}<g:if test="${formField.isRequired}">*</g:if></h4>
        <g:if test="${formField.helpText}">
            <span class="help-block">${formField.helpText.encodeAsHTML()}</span>
        </g:if>
    </div>

    <div class="form-group col-sm-12">
        <g:each in="${formField.predefinedValues}" var="pv" status="i">
            <g:if test="${pv.isActive}">
                <div class="row">
                    <div class="col-sm-2 col-xs-4 checkbox checkbox-success checkbox-lg">
                        <input type="checkbox" id="${formField.id}.${i}.checkmark" name="${formField.id}.${i}.checkmark"
                                value="true" ${params.boolean(formField.id + '.' + i + '.checkmark') ? 'checked' : ''}
                                             ${formField.isRequired ? 'required' : ''} data-type="${formField.type}"/>
                        <label for="${formField.id}.${i}.checkmark">${pv.value.encodeAsHTML()}</label>
                    </div>
                    <div class="col-sm-10 col-xs-8">
                        <span id="timeRangeLabel${formField.id}.${i}">
                            ${params[formField.id + '.' + i + '.from'] ?: (pv.minValue ?: '00:00')}
                            -
                            ${params[formField.id + '.' + i + '.to'] ?: (pv.maxValue ?: '24:00')}
                        </span>

                        <div id="timeRange${formField.id}.${i}" class="top-margin10"></div>
                    </div>
                </div>

                <g:hiddenField name="${g.forJavaScript(data: formField.id)}.${g.forJavaScript(data: i)}.from" value="${g.forJavaScript(data: params[formField.id+ '.' + i + '.from'] ?: (pv.minValue ?: '00:00'))}"/>
                <g:hiddenField name="${g.forJavaScript(data: formField.id)}.${g.forJavaScript(data: i)}.to" value="${g.forJavaScript(data: params[formField.id+ '.' + i + '.to'] ?: (pv.maxValue ?: '24:00'))}"/>

                <r:script>
                    $(function() {
                        $("#timeRange${g.forJavaScript(data: formField.id)}\\.${g.forJavaScript(data: i)}").slider({
                            range: true,
                            min: getMinutes("${g.forJavaScript(data: pv.minValue)}", 0),
                            max: getMinutes("${g.forJavaScript(data: pv.maxValue)}", 1440),
                            step: 30,
                            values: [
                                    getMinutes("${g.forJavaScript(data: params[formField.id + '.' + i + '.from'] ?: (pv.minValue ?: ''))}", 0),
                                    getMinutes("${g.forJavaScript(data: params[formField.id + '.' + i + '.to'] ?: (pv.maxValue ?: ''))}", 1440)
                            ],
                            slide: function(event, ui) {
                                var fromTime = getTime(ui.values[0]);
                                var toTime = getTime(ui.values[1]);
                                $("#${g.forJavaScript(data: formField.id)}\\.${g.forJavaScript(data: i)}\\.from").val(fromTime);
                                $("#${g.forJavaScript(data: formField.id)}\\.${g.forJavaScript(data: i)}\\.to").val(toTime);
                                $("#timeRangeLabel${g.forJavaScript(data: formField.id)}\\.${g.forJavaScript(data: i)}").html(fromTime + " - " + toTime);
                            }
                        });

                        $("#${g.forJavaScript(data: formField.id)}\\.${g.forJavaScript(data: i)}\\.checkmark").on("click", function() {
                            $("#timeRange${g.forJavaScript(data: formField.id)}\\.${g.forJavaScript(data: i)}").slider($(this).is(":checked") ? "enable" : "disable");
                        });

                        <g:if test="${!params.boolean(formField.id + '.' + i + '.checkmark')}">
                            $("#timeRange${g.forJavaScript(data: formField.id)}\\.${g.forJavaScript(data: i)}").slider("disable");
                        </g:if>
                    });
                </r:script>
            </g:if>
        </g:each>
    </div>
</div>
