<div class="controls">
    <label class="checkbox">
        <g:checkBox name="requirements[${req.key}].skip" value="skip" checked="${skip ? 'true' : 'false'}" title="Skip" onclick="toggleDisabled(${req.key});" /> <g:message code="facilityRequirements.edit.skip" />
    </label>
</div>