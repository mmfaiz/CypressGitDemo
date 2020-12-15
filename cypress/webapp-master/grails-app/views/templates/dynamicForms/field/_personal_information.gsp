<%@ page import="com.matchi.dynamicforms.FieldBinder; com.matchi.dynamicforms.FormField; com.matchi.Customer" %>

<div class="block personal-information">
    <div class="col-sm-12">
        <h4>${formField.label.encodeAsHTML()}</h4>
        <g:if test="${formField.helpText}">
            <span class="help-block">${formField.helpText.encodeAsHTML()}</span>
        </g:if>
    </div>

    <g:each in="${formField.typeEnum.binder.inputs}" var="input">
        <div class="form-group col-sm-6">
            <label for="${formField.id}.${input}">
                <g:if test="${input == 'security_number'}">
                    <g:message code="${formInstance?.facility?.requireSecurityNumber ? "membershipRequest.index.message19" : "membershipRequest.index.message23"}"
                               args="[message(
                                       code: (formInstance?.facility?.requireSecurityNumber ?
                                               'membershipRequestCommand.securitynumber.format' :
                                               'membershipRequestCommand.birthday.format'),
                                       locale: new Locale(formInstance?.facility?.language))]"
                    />${(formInstance?.facility?.requireSecurityNumber &&
                        !formField.typeEnum.binder.canIgnoreValidation(input, hasAccessToFacility(facility: formInstance?.facility)))
                         ? '*' : ''}
                </g:if>
                <g:else>
                    <g:message code="formField.type.PERSONAL_INFORMATION.${input}"/>
                    <g:if test="${formField.isRequired &&
                            !formField.typeEnum.binder.optionalInputs.contains(input)}">
                        *
                    </g:if>
                </g:else>
            </label>
            <g:if test="${input == 'gender'}">
                <g:select name="${formField.id}.${input}" from="${Customer.CustomerType.listGender()*.name()}"
                          value="${params[formField.id + '.' + input]}" valueMessagePrefix="customer.type" class="form-control"
                          noSelection="${['': message(code: 'facilityCourseParticipant.index.genders.noneSelectedText')]}"
                          required="${formField.isRequired && !formField.typeEnum.binder.optionalInputs.contains(input)}"/>
            </g:if>
            <g:else>
                <input type="text" id="${formField.id}.${input}" name="${formField.id}.${input}" maxlength="255" class="form-control"
                       value="${params[formField.id + '.' + input] ?: ''}" ${formField.isRequired &&
                        !formField.typeEnum.binder.optionalInputs.contains(input) &&
                        (input != 'security_number' || !formField.typeEnum.binder.canIgnoreValidation(input, hasAccessToFacility(facility: formInstance?.facility))) ? 'required' : ''}/>
            </g:else>
        </div>
    </g:each>
</div>

<g:if test="${!preview && hasFacilityFullRights()}">
    <r:require modules="bootstrap-typeahead"/>

    <r:script>
        $(function() {
            $(".personal-information input[id$=firstname]").attr("autocomplete", "off").attr("type", "search").typeahead({
                source: function(query, process) {
                    $.get("${g.forJavaScript(data: createLink(controller: 'autoCompleteSupport', action: 'personalInformation'))}",
                            {fullName: query},
                            function(data) {
                                return process(data);
                            }
                    );
                }
            }).change(function() {
                var current = $(this).typeahead("getActive");
                if (current.name == $(this).val()) {
                    $(this).val(current.firstname);
                    $(".personal-information input[id$=lastname]").val(current.lastname);
                    $(".personal-information select[id$=gender]").val(current.type);
                    $(".personal-information input[id$=security_number]").val(current.personalNumber);
                    $(".personal-information input[id$=email]").val(current.email);
                    $(".personal-information input[id$=cellphone]").val(current.cellphone);
                    $(".personal-information input[id$=telephone]").val(current.telephone);
                    $(".address input[id$=address1]").val(current.address1);
                    $(".address input[id$=address2]").val(current.address2);
                    $(".address input[id$=postal_code]").val(current.zipcode);
                    $(".address input[id$=city]").val(current.city);
                    $(".parent-information input[id$=firstname]").val(
                            current.guardianName ? current.guardianName.split(" ")[0] : "");
                    $(".parent-information input[id$=lastname]").val(
                            current.guardianName ? current.guardianName.split(" ")[1] : "");
                    $(".parent-information input[id$=email]").val(current.guardianEmail);
                    $(".parent-information input[id$=cellphone]").val(current.guardianTelephone);
                }
            });
        });
    </r:script>
</g:if>