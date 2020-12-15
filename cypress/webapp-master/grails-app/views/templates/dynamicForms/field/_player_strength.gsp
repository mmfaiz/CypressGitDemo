<div class="block">
    <div class="form-group col-sm-12">
        <div class="checkbox checkbox-success checkbox-lg">
            <input type="checkbox" id="${formField.id}.checkmark" name="${formField.id}.checkmark"
                    value="true" ${params.boolean(formField.id + '.checkmark') ? 'checked' : ''}/>
            <label for="${formField.id}.checkmark">${formField.label.encodeAsHTML()}<g:if test="${formField.isRequired}">*</g:if></label>
        </div>

        <g:if test="${formField.helpText}">
            <span class="help-block">${formField.helpText.encodeAsHTML()}</span>
        </g:if>
    </div>

    <div class="form-group col-sm-12">
        <g:textField name="${formField.id}.value" data-slider-min="1"
                data-slider-max="${formField.predefinedValues.size()}" data-slider-step="1"
                data-slider-value="${params[formField.id + '.value'] ?: 1}"/>
    </div>
    <div class="text-muted skill-level-text col-sm-12">
        <span id="playerStrengthDescription${formField.id}">
            <g:message code="sport.skillLevel.1"/>
        </span>
    </div>
</div>

<r:require modules="bootstrap-slider"/>
<r:script>
    var playerStrength${g.forJavaScript(data: formField.id)} = {};
    <g:each in="${formField.predefinedValues}" var="pv" status="i">
        playerStrength${g.forJavaScript(data: formField.id)}[${g.forJavaScript(data: i+1)}] = "${g.forJavaScript(data: pv.value)}";
    </g:each>

    $(function() {
        $("#${g.forJavaScript(data: formField.id)}\\.value").bootstrapSlider({
            tooltip: 'always'
        });

        $("#${g.forJavaScript(data: formField.id)}\\.value").on("change", function(slideEvt) {
            var value = $("#${g.forJavaScript(data: formField.id)}\\.value").bootstrapSlider('getValue');

            if (value == 1) {
                $('#playerStrengthDescription${g.forJavaScript(data: formField.id)}').text("<g:message code="sport.skillLevel.1"/>");
            }
            if (value == 2) {
                $('#playerStrengthDescription${g.forJavaScript(data: formField.id)}').text("<g:message code="sport.skillLevel.2"/>");
            }
            if (value == 3) {
                $('#playerStrengthDescription${g.forJavaScript(data: formField.id)}').text("<g:message code="sport.skillLevel.3"/>");
            }
            if (value == 4) {
                $('#playerStrengthDescription${g.forJavaScript(data: formField.id)}').text("<g:message code="sport.skillLevel.4"/>");
            }
            if (value == 5) {
                $('#playerStrengthDescription${g.forJavaScript(data: formField.id)}').text("<g:message code="sport.skillLevel.5"/>");
            }
            if (value == 6) {
                $('#playerStrengthDescription${g.forJavaScript(data: formField.id)}').text("<g:message code="sport.skillLevel.6"/>");
            }
            if (value == 7) {
                $('#playerStrengthDescription${g.forJavaScript(data: formField.id)}').text("<g:message code="sport.skillLevel.7"/>");
            }
            if (value == 8) {
                $('#playerStrengthDescription${g.forJavaScript(data: formField.id)}').text("<g:message code="sport.skillLevel.8"/>");
            }
            if (value == 9) {
                $('#playerStrengthDescription${g.forJavaScript(data: formField.id)}').text("<g:message code="sport.skillLevel.9"/>");
            }
            if (value == 10) {
                $('#playerStrengthDescription${g.forJavaScript(data: formField.id)}').text("<g:message code="sport.skillLevel.10"/>");
            }
        });

        $("#${g.forJavaScript(data: formField.id)}\\.checkmark").on("click", function() {
            $("#${g.forJavaScript(data: formField.id)}\\.value").bootstrapSlider($(this).is(":checked") ? "enable" : "disable");
        });

        <g:if test="${!params.boolean(formField.id + '.checkmark')}">
            $("#${g.forJavaScript(data: formField.id)}\\.value").bootstrapSlider("disable");
        </g:if>
    });
</r:script>