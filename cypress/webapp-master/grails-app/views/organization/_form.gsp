<div>
    <div class="col-sm-6 form-group ${hasErrors(bean: organizationInstance, field: 'name', 'error')} ">
        <label for="name">
            <g:message code="organization.name.label"/>*
        </label>

        <div class="controls">
            <g:textField class="form-control" name="name" value="${organizationInstance?.name}" required=""/>
        </div>
    </div>
</div>
<div>
    <div class="col-sm-6 form-group ${hasErrors(bean: organizationInstance, field: 'number', 'error')} ">
        <label for="number">
            <g:message code="organization.number.label"/>
        </label>

        <div class="controls">
            <g:textField class="form-control" name="number" value="${organizationInstance?.number}"/>
        </div>
    </div>
</div>
<g:if test="${getUserFacility()?.hasFortnox()}">
    <div>
        <div class="col-sm-6 form-group ${hasErrors(bean: organizationInstance, field: 'fortnoxCostCenter', 'error')} ">
            <label for="fortnoxCostCenter">
                <g:message code="organization.fortnoxCostCenter.label"/>
            </label>

            <div class="controls">
                <g:textField class="form-control" name="fortnoxCostCenter" value="${organizationInstance?.fortnoxCostCenter}" maxlength="6"/>
            </div>
        </div>
    </div>
</g:if>