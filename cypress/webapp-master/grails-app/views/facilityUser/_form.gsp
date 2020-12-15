<%@ page import="com.matchi.FacilityUserRole" %>

<div class="row">
    <div class="col-sm-6 form-group">
        <label for="email"><g:message code="facilityUser.user.label"/>*</label>
        <g:textField name="email" maxlength="255" required="required" class="form-control"
                value="${user?.email}" readonly="${user != null}"/>
    </div>
</div>

<div class="row">
    <div class="col-sm-12 form-group">
        <label class="control-label"><g:message code="facilityUser.facilityRoles.label"/>*</label>
        <g:set var="facilityUser" value="${user?.facilityUsers.find {it.facility.id == facility.id}}"/>
        <g:each in="${FacilityUserRole.AccessRight.list()}" var="ar" status="i">
            <div class="checkbox">
                <input type="checkbox" id="${ar}" name="facilityRoles[${i}].accessRight" value="${ar}"
                        ${facilityUser?.facilityRoles?.find {it.accessRight == ar} ? 'checked' : ''}/>
                <label for="${ar}"><g:message code="facilityUserRole.accessRight.${ar}"/>
                    <g:inputHelp title="${message(code: "facilityUserRole.accessRight.${ar}.help")}"/></label>
            </div>
        </g:each>
    </div>
</div>
<r:script>
    $(document).ready(function() {
        $('[rel=tooltip]').tooltip();

        $(":checkbox[name^=facilityRoles][value=${g.forJavaScript(data: FacilityUserRole.AccessRight.FACILITY_ADMIN.name())}]").change(function() {
            $(":checkbox[name^=facilityRoles][value!=${g.forJavaScript(data: FacilityUserRole.AccessRight.FACILITY_ADMIN.name())}]").prop("disabled", $(this).is(":checked"));
        }).trigger("change");
    });
</r:script>
