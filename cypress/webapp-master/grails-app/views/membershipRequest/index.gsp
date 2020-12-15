<%@ page import="com.matchi.membership.MembershipType; com.matchi.User; org.joda.time.DateTime; com.matchi.Customer; com.matchi.FacilityProperty.FacilityPropertyKey" %>
<head>
    <meta name="paymentDialogIncluded" content="true"/>
    <meta name="layout" content="b3main"/>
    <title><g:message code="facility.membership.apply"/></title>
</head>

<body>

<g:render template="header"/>

<!-- Messages -->
<g:b3StaticErrorMessage bean="${cmd}"/>

<g:set var="membershipTypesNonOrganization" value="${typesWithDates.findAll { !it.type.organizationType }}"/>
<g:set var="countries" value="${grailsApplication.config.matchi.settings.available.countries}"/>

<section class="block block-white">
    <div class="container">
        <div class="row">
            <div class="col-sm-8">
                <g:form controller="membershipRequest" action="request" class="form vertical-padding30">
                    <g:hiddenField name="sname" value="${cmd?.sname ?: name}"/>
                    <g:hiddenField name="startDate" value="${startDate}"/>
                    <g:hiddenField name="baseMembership" value="${cmd?.baseMembership?.id}"/>
                    <g:hiddenField name="purchasedAtFacility" value="${purchasedAtFacility?.id}"/>


                    <g:if test="${familyMembershipsEnabledFacilities && !familyMembershipsEnabledFacilities.isEmpty()}">
                        <div class="form-group">
                            <label>
                                <g:radio name="familyMembershipFacility"
                                         value="0" checked="${cmd?.familyMembershipFacility?.id > 0}"/>
                                None
                            </label>
                        </div>
                        <g:each in="${familyMembershipsEnabledFacilities}" var="facility">
                            <g:if
                                test="${facility.isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_USE_FAMILY_MEMBERSHIPS.name())}">
                                <div class="form-group">
                                    <label>
                                        <g:radio name="familyMembershipFacility"
                                                 value="${facility.id}"
                                                 checked="${cmd?.familyMembershipFacility == facility}"/>
                                        <g:message code="membershipRequest.index.familyMembership"/> (${facility})
                                        <g:if
                                            test="${facility.getFacilityProperty(FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT)}">
                                            <span id="max-price"><g:message
                                                code="membershipRequest.index.familyMembership.maxPrice"
                                                args="[formatMoney(value: facility.getFacilityProperty(FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT).value, facility: facility)]"/></span>
                                        </g:if>
                                    </label>
                                </div>
                            </g:if>
                        </g:each>
                        <hr/>
                    </g:if>

                    <div class="member-list">
                        <g:if test="${cmd?.items}">
                            <g:each in="${cmd.items}" var="item" status="i">
                                <g:set var="requireSecurityNumber"
                                       value="${item?.membershipType?.facility?.requireSecurityNumber}"/>

                                <div class="member-type">
                                    <g:if test="${i ? membershipTypesNonOrganization : typesWithDates}">
                                        <div
                                            class="form-group ${hasErrors(bean: item, field: 'membershipType', 'has-error')}">
                                            <label for="items[${i}].membershipType"><g:message
                                                code="membershipType.label"/> (*)</label>
                                            <g:if test="${i}">
                                                <a href="javascript: void(0)"
                                                   class="btn btn-link text-danger delete-member">
                                                    <i class="ti-close"></i>
                                                </a>
                                            </g:if>
                                            <div class="row no-horizontal-margin">
                                                <g:each in="${i ? membershipTypesNonOrganization : typesWithDates}">
                                                    <g:set var="mt" value="${it.type}"/>
                                                    <div class="radio bottom-margin20 col-md-6 top-margin10">
                                                        <g:radio id="items[${i}].membershipType_${mt.id}"
                                                                 name="items[${i}].membershipType"
                                                                 value="${mt.id}"
                                                                 checked="${item.membershipType == mt || it == membershipTypesNonOrganization.first()}"
                                                                 data-organization="${mt.organizationType}"
                                                                 data-facility="${mt.facility.id}"/>
                                                        <label for="items[${i}].membershipType_${mt.id}">
                                                            <strong>${mt.name}</strong><br/>
                                                            <g:message code="default.price.label"/>:
                                                            <strong><g:if test="${mt.price}"><g:formatMoney
                                                                value="${mt.price}"
                                                                facility="${mt.facility}"/></g:if><g:else><g:message
                                                                code="payment.method.FREE"/></g:else></strong><br/>
                                                            <g:message code="membership.buy.valid.label"/>:
                                                            <strong><g:formatDate date="${it.startDate}"
                                                                                  formatName="date.format.daterangepicker.short"/></strong>
                                                            -
                                                            <strong><g:formatDate date="${it.endDate}"
                                                                                  formatName="date.format.daterangepicker.short"/></strong>
                                                            <g:if test="${mt.recurring}">
                                                                <br/>
                                                                <g:message code="membership.buy.type.label"/>:
                                                                <strong>${message(code: 'membershipType.paidOnRenewal.recurring.tooltip')}</strong>
                                                            </g:if>
                                                        </label>
                                                    </div>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:if>

                                    <div class="fields-container">
                                        <div class="member-fields" data-facility="${item?.membershipType?.facility?.id}">
                                            <div class="row person-control" style="display: none">
                                                <div
                                                    class="col-md-6 form-group ${hasErrors(bean: item, field: 'firstname', 'has-error')}">
                                                    <label for='items[${i}].firstname'><g:message
                                                        code="customer.firstname.label"/> (*)</label>
                                                    <g:textField class="form-control" name="items[${i}].firstname"
                                                                 value="${item.firstname}"/>
                                                </div>

                                                <div
                                                    class="col-md-6 form-group ${hasErrors(bean: item, field: 'lastname', 'has-error')}">
                                                    <label for='items[${i}].lastname'><g:message
                                                        code="customer.lastname.label"/> (*)</label>
                                                    <g:textField class="form-control" name="items[${i}].lastname"
                                                                 value="${item.lastname}"/>
                                                </div>
                                            </div>

                                            <div class="row organization-control" style="display: none">
                                                <input type="hidden" name="items[${i}].type"
                                                       value="${Customer.CustomerType.ORGANIZATION.name()}"/>

                                                <div
                                                    class="col-md-6 form-group ${hasErrors(bean: item, field: 'companyname', 'has-error')}">
                                                    <label for='items[${i}].companyname'><g:message
                                                        code="facilityCustomer.form.message10"/> (*)</label>
                                                    <g:textField class="form-control" name="items[${i}].companyname"
                                                                 value="${item.companyname}"/>
                                                </div>

                                                <div
                                                    class="col-md-6 form-group organization-control ${hasErrors(bean: item, field: 'contact', 'has-error')}"
                                                    style="display: none">
                                                    <label for="items[${i}].contact"><g:message
                                                        code="customer.contact.label"/> (*)</label>
                                                    <g:textField class="form-control" name="items[${i}].contact"
                                                                 value="${item.contact}" maxlength="255"/>
                                                </div>
                                            </div>

                                            <div class="row">
                                                <div
                                                    class="col-md-6 form-group ${hasErrors(bean: item, field: 'email', 'has-error')}">
                                                    <label for="items[${i}].email"><g:message
                                                        code="customer.email.label"/> (*)</label>
                                                    <g:textField class="form-control" name="items[${i}].email"
                                                                 value="${item.email}"/>
                                                </div>

                                                <div
                                                    class="col-md-6 form-group ${hasErrors(bean: item, field: 'telephone', 'has-error')}">
                                                    <label for="items[${i}].telephone"><g:message
                                                        code="customer.telephone.label"/> (*)</label>
                                                    <g:textField class="form-control" name="items[${i}].telephone"
                                                                 value="${item.telephone}"/>
                                                </div>
                                            </div>


                                            <g:if
                                                test="${facility?.isFacilityPropertyEnabled(com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                                                <div class="row person-control">
                                                    <div
                                                        class="col-md-6 form-group ${hasErrors(bean: item, field: 'club', 'has-error')}">
                                                        <label for="items[${i}].club"><g:message
                                                            code="default.club.label"/> (*)</label>
                                                        <g:textField class="form-control" name="items[${i}].club"
                                                                     value="${item.club}"/>
                                                    </div>
                                                </div>
                                            </g:if>

                                            <div class="row">
                                                <div
                                                    class="col-md-6 form-group ${hasErrors(bean: item, field: 'birthday', 'has-error')} ${hasErrors(bean: item, field: 'securitynumber', 'has-error')}">
                                                    <label for="items[${i}].birthDay">
                                                        <span class="person-control" style="display: none">
                                                            <g:if test="${requireSecurityNumber}">
                                                                <g:message code="membershipRequest.index.message19"
                                                                           args="[message(code: 'membershipRequestCommand.securitynumber.format', locale: locale)]"/>
                                                                <g:inputHelp
                                                                    title="${message(code: 'membershipRequest.index.message20')}"/> (*)
                                                            </g:if>
                                                            <g:else>
                                                                <g:message code="membershipRequest.index.message23"
                                                                           args="[message(code: 'membershipRequestCommand.birthday.format', locale: locale)]"/>
                                                                <g:inputHelp
                                                                    title="${message(code: 'membershipRequest.index.message20')}"/> (*)
                                                            </g:else>
                                                        </span>
                                                        <span class="organization-control" style="display: none">
                                                            <g:message code="membershipRequest.index.orgNumber"
                                                                       args="[message(code: 'membershipRequestCommand.birthday.format', locale: locale)]"/>
                                                        </span>
                                                    </label>

                                                    <div class="row">
                                                        <div class="col-md-5 col-sm-5">
                                                            <g:textField name="items[${i}].birthday"
                                                                         class="form-control ${requireSecurityNumber ? "text-right" : ""}"
                                                                         value="${item.birthday}"/>
                                                        </div>
                                                        <g:if test="${requireSecurityNumber}">
                                                            <div class="col-xs-4 col-md-4">
                                                                <g:textField name="items[${i}].securitynumber"
                                                                             class="form-control"
                                                                             value="${item.securitynumber}"/>
                                                            </div>
                                                        </g:if>
                                                    </div>

                                                </div>

                                                <div
                                                    class="col-md-6 form-group person-control ${hasErrors(bean: item, field: 'type', 'has-error')}"
                                                    style="display: none">
                                                    <label for="items[${i}].type"><g:message
                                                        code="default.gender.label"/> (*)</label><br>
                                                    <select id="items[${i}].type" name="items[${i}].type"
                                                            data-style="form-control">
                                                        <g:each in="${Customer.CustomerType.listGender()}">
                                                            <option
                                                                value="${it}" ${item.type == it ? "selected" : ""}>&nbsp;<g:message
                                                                code="customer.type.${it}"/></option>
                                                        </g:each>
                                                    </select>
                                                </div>
                                            </div>

                                            <div class="row">
                                                <div
                                                    class="col-md-6 form-group ${hasErrors(bean: item, field: 'address', 'has-error')}">
                                                    <label for="items[${i}].address"><g:message
                                                        code="membershipRequest.index.message8"/> (*)</label>
                                                    <g:textField class="form-control" name="items[${i}].address"
                                                                 value="${item.address}"/>
                                                </div>

                                                <div
                                                    class="col-md-6 form-group ${hasErrors(bean: item, field: 'zipcode', 'has-error')}">
                                                    <label for="items[${i}].zipcode"><g:message
                                                        code="membershipRequest.index.message9"/> (*)</label>
                                                    <g:textField class="form-control" name="items[${i}].zipcode"
                                                                 value="${item.zipcode}"/>
                                                </div>
                                            </div>

                                            <div class="row">
                                                <div
                                                    class="col-md-6 form-group ${hasErrors(bean: item, field: 'city', 'has-error')}">
                                                    <label for="items[${i}].city"><g:message
                                                        code="membershipRequest.index.message10"/> (*)</label>
                                                    <g:textField class="form-control" name="items[${i}].city"
                                                                 value="${item.city}"/>
                                                </div>

                                                <div
                                                    class="col-md-6 form-group ${hasErrors(bean: item, field: 'country', 'has-error')}">
                                                    <label for="items[${i}].country"><g:message
                                                        code="default.country.label"/> (*)</label>
                                                    <!--<g:textField class="form-control" name="items[${i}].country"
                                                                     value="${item.country}"/>-->

                                                    <g:select id="items[${i}].country" name="items[${i}].country"
                                                              from="${countries}"
                                                              valueMessagePrefix="country" value="${item?.country}"
                                                              class="form-control"/>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </g:each>

                            <g:javascript>
                                $(function() {
                                    changeCustomerType(null, "${g.forJavaScript(data: cmd.items[0].type == Customer.CustomerType.ORGANIZATION)}");

                                });
                            </g:javascript>
                        </g:if>

                        <g:javascript>
                            $(function () {
                                changeCustomerType(null, isOrganizationMembershipTypeSelected());
                            });
                        </g:javascript>
                    </div>

                    <a id="add-member" href="javascript:void(0)" class="btn btn-info bottom-margin20"
                       style="display: none"><g:message code="membershipRequest.index.addMember"/></a>

                    <div class="form-group ${hasErrors(bean: cmd, field: 'message', 'has-error')}">
                        <label for="message"><g:message code="membershipRequest.index.message15"/></label>
                        <g:textArea name="message" rows="3" cols="50" class="form-control" value="${cmd?.message}"/>
                    </div>

                    <div class="form-group ${hasErrors(bean: cmd, field: 'confirmation', 'has-error')}">
                        <div class="checkbox">
                            <g:checkBox class="checkbox" name="confirmation" value="${cmd?.confirmation}"/>
                            <label for="confirmation">
                                <g:message code="membershipRequest.index.message21"/>
                            </label>
                        </div>
                    </div>

                    <g:submitButton name="sumbit" class="btn btn-success"
                                    value="${message(code: 'membershipRequest.index.submit' + (requestPayment ? 'AndPay' : ''))}"/>
                    <a href="javascript:history.go(-1)" class="btn btn-danger"><g:message
                        code="button.cancel.label"/></a>
                </g:form>
            </div><!-- /.col-sm-8 -->

            <div class="col-sm-4">
                <g:each in="${facilitiesThatReceivesMemberships}" var="facility">
                    <div class="vertical-padding20">
                        <g:if test="${facility?.membershipRequestDescription}">
                            <h4 class="weight400">
                                <i class="fa fa-comment-o"></i> <g:message code="facility.membership.requestDescription"
                                                                           args="[facility?.name]"/>
                            </h4>
                            ${g.toRichHTML(text: facility?.membershipRequestDescription)}
                        </g:if>

                        <g:if test="${facility.requireSecurityNumber}">
                            <h4 class="top-margin30 weight400">
                                <i class="fas fa-info-circle"></i> <g:message code="membershipRequest.index.message18"/>
                            </h4>

                            <p>
                                <g:message code="membershipRequest.index.message22"/>
                            </p>
                        </g:if>
                        <hr/>
                    </div>
                </g:each>
            </div><!-- /.col-sm-4 -->

        </div><!-- /.row -->
    </div>
</section>


<div class="member-type-template" style="display: none;">
    <g:if test="${membershipTypesNonOrganization}">
        <div class="form-group">
            <label><g:message code="membershipType.label"/> (*)</label>
            <a href="javascript: void(0)" class="btn btn-link text-danger delete-member">
                <i class="ti-close"></i>
            </a>

            <div class="row no-horizontal-margin">
                <g:each in="${membershipTypesNonOrganization}">
                    <g:set var="mt" value="${it.type}"/>
                    <div class="radio bottom-margin20 col-md-6 top-margin10">
                        <g:radio id="membershipType_${mt.id}" name="membershipType" value="${mt.id}"
                                 checked="${it == membershipTypesNonOrganization.first()}"
                                 data-organization="${mt.organizationType}" data-facility="${mt.facility.id}"/>
                        <label>
                            <strong>${mt.name}</strong><br/>

                            <div><small><em><g:message code="membership.buy.globalDescription"
                                                       args="${mt.facility}"/></em></small></div>

                            <g:message code="default.price.label"/>:
                            <strong><g:if test="${mt.price}"><g:formatMoney value="${mt.price}"
                                                                            facility="${mt.facility}"/></g:if><g:else><g:message
                                code="payment.method.FREE"/></g:else></strong><br/>
                            <g:message code="membership.buy.valid.label"/>:
                            <strong><g:formatDate date="${it.startDate}"
                                                  formatName="date.format.daterangepicker.short"/></strong>
                            -
                            <strong><g:formatDate date="${it.endDate}"
                                                  formatName="date.format.daterangepicker.short"/></strong>
                            <g:if test="${mt.recurring}">
                                <br/>
                                <g:message code="membership.buy.type.label"/>:
                                <strong>${message(code: 'membershipType.paidOnRenewal.recurring.tooltip')}</strong>
                            </g:if>
                        </label>
                    </div>
                </g:each>
            </div>
        </div>
    </g:if>
    <div class="fields-container">

    </div>
    <hr/>
</div>

<div class="member-fields-templates" style="display: none;">
    <g:each in="${facilitiesThatReceivesMemberships}" var="facility">
        <g:set var="requireSecurityNumber"
               value="${facility.requireSecurityNumber}"/>
        <div class="member-fields-template" data-facility="${facility.id}">
            <div class="row person-control">
                <div class="col-md-6 form-group">
                    <label><g:message code="customer.firstname.label"/> (*)</label>
                    <g:textField class="form-control" name="firstname"/>
                </div>

                <div class="col-md-6 form-group">
                    <label><g:message code="customer.lastname.label"/> (*)</label>
                    <g:textField class="form-control" name="lastname"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6 form-group">
                    <label><g:message code="customer.email.label"/> (*)</label>
                    <g:textField class="form-control" name="email"/>
                </div>

                <div class="col-md-6 form-group">
                    <label><g:message code="customer.telephone.label"/> (*)</label>
                    <g:textField class="form-control" name="telephone"/>
                </div>
            </div>

            <g:if
                test="${facility?.isFacilityPropertyEnabled(com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                <div class="row person-control">
                    <div class="col-md-6 form-group">
                        <label><g:message code="default.club.label"/> (*)</label>
                        <g:textField class="form-control" name="club"/>
                    </div>
                </div>
            </g:if>

            <div class="row">
                <div class="col-md-6 form-group">
                    <label>
                        <span class="person-control">
                            <g:if test="${requireSecurityNumber}">
                                <g:message code="membershipRequest.index.message19"
                                           args="[message(code: 'membershipRequestCommand.securitynumber.format', locale: locale)]"/>
                                <g:inputHelp title="${message(code: 'membershipRequest.index.message20')}"/> (*)
                            </g:if>
                            <g:else>
                                <g:message code="membershipRequest.index.message23"
                                           args="[message(code: 'membershipRequestCommand.birthday.format', locale: locale)]"/>
                                <g:inputHelp title="${message(code: 'membershipRequest.index.message20')}"/> (*)
                            </g:else>
                        </span>
                    </label>

                    <div class="row">
                        <div class="col-xs-5 col-md-5">
                            <g:textField name="birthday"
                                         class="form-control ${requireSecurityNumber ? "text-right" : ""}"/>
                        </div>
                        <g:if test="${requireSecurityNumber}">
                            <div class="col-xs-4 col-md-4">
                                <g:textField name="securitynumber" class="form-control"/>
                            </div>
                        </g:if>
                    </div>
                </div>

                <div class="col-md-6 form-group person-control">
                    <label><g:message code="default.gender.label"/> (*)</label><br>
                    <g:select name="type" from="${Customer.CustomerType.listGender()}"
                              valueMessagePrefix="customer.type" class="form-control"
                              noSelection="${['': message(code: 'facilityCourseParticipant.index.genders.noneSelectedText')]}"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6 form-group">
                    <label><g:message code="membershipRequest.index.message8"/> (*)</label>
                    <g:textField class="form-control" name="address"/>
                </div>

                <div class="col-md-6 form-group">
                    <label><g:message code="membershipRequest.index.message9"/> (*)</label>
                    <g:textField class="form-control" name="zipcode"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6 form-group">
                    <label><g:message code="membershipRequest.index.message10"/> (*)</label>
                    <g:textField class="form-control" name="city"/>
                </div>

                <div class="col-md-6 form-group">
                    <label><g:message code="default.country.label"/> (*)</label>
                    <g:select name="country" from="${countries}" value="${facility.country}"
                              valueMessagePrefix="country" class="form-control"/>
                </div>
            </div>
        </div>
    </g:each>
</div>

<g:if test="${membershipTypeToPay}">
    <g:render template="/templates/payments/paymentDialog"/>
    <r:script>
        $(function() {
            openPaymentDialog();
            $.ajax({
                type: "POST",
                data: {
                    <g:each in="${familyMembers}" var="fm">
        member_${g.forJavaScript(data: fm.key)}: ${g.forJavaScript(data: fm.value.id)},
    </g:each>
        id: ${g.forJavaScript(data: membershipTypeToPay.id)},
                    facilityMessage: "${g.forJavaScript(data: cmd.message)}",
                    startDate: "${startDate.toString()}"
        <g:if test="${cmd.baseMembership?.id}">, baseMembership: ${cmd.baseMembership.id}</g:if>
        <g:if test="${purchasedAtFacility?.id}">, purchasedAtFacilityId: ${purchasedAtFacility.id}</g:if>
        },
        url: "${g.forJavaScript(data: createLink(controller: 'membershipPayment', action: 'confirm'))}",
                success: function (data) {
                    $("#userBookingModal").find(".modal-dialog").html(data);
                },
                error: function () {
                    handleAjaxError();
                }
            });
        });
    </r:script>
</g:if>

<g:javascript>
$(document).ready(function() {
    try {
        $("[rel=tooltip]").tooltip();

        $("select[name$='.type']").selectpicker({
            title: "${message(code: 'membershipRequest.index.message14')}"
        });

        $("select[name$='.country']").selectpicker();

        $(":radio[name=familyMembershipFacility]").on("change", (e) => {
            var facilityId = $(e.target).val()

            $("#add-member").toggle(facilityId > 0);
            if (facilityId < 0) {
                $(".member-fields").slice(1).remove();
            }

            if (facilityId > 0) {
                $(":radio[name$='.membershipType']").prop("disabled", true).closest("div.radio").hide()
                $(":radio[name$='.membershipType'][data-facility=" + facilityId + "]").prop("disabled", false).closest("div.radio").show()

                $(":radio[name$='.membershipType'][data-organization=true]").prop("disabled", true).closest("div.radio").hide()
            } else {
                $(":radio[name$='.membershipType']").prop("disabled", false).closest("div.radio").show()
            }
        }).sort(function(a,b) {
          return $(b).is(":checked") - $(a).is(":checked")
        }).first().prop("checked", true).trigger("change");

        $(".member-list").on("click", ".delete-member", function() {
            $(this).closest(".member-type").remove();
            $(".member-list").find(".member-type").each(function(index) {
                $(this).find(":input").each(function() {
                    if ($(this).attr("name")) {
                        $(this).attr("name", $(this).attr("name").replace(
                                /items\[\d+\]/, "items[" + index + "]"));
                    }
                    if ($(this).attr("id")) {
                        $(this).attr("id", $(this).attr("id").replace(
                                /items\[\d+\]/, "items[" + index + "]"));
                    }
                });
                $(this).find("label").each(function() {
                    if ($(this).attr("for")) {
                        $(this).attr("for", $(this).attr("for").replace(
                                /items\[\d+\]/, "items[" + index + "]"));
                    }
                });
            });
        });

        $("#items\\[0\\]\\.firstname").prop("readonly", true);
        $("#items\\[0\\]\\.lastname").prop("readonly", true);
        $("#items\\[0\\]\\.email").prop("readonly", true);
    } catch (e) {
        console.log("error", e);
    }

    $("#add-member").click(function() {
        var index = $(".member-fields").length;
        var typeTemplate = $(".member-type-template").clone();

        typeTemplate.removeClass("member-type-template");
        typeTemplate.addClass("member-type");

        $(".member-list").append(typeTemplate);

        typeTemplate.show();

        typeTemplate.find(":input").each(function() {
            var name = "items[" + index + "]." + $(this).attr("name");
            var id = "items[" + index + "]." + ($(this).attr("id") || $(this).attr("name"));
            $(this).attr("name", name).attr("id", id).parent().find("label").attr("for", id);
        });

        typeTemplate.find(":radio[name$='.membershipType']").off("change").on("change", (e) => {
          changeMembershipType(e,index);
        });

        typeTemplate.find(":radio[name$='.membershipType']:checked").change();

        typeTemplate.find(":input[name=address]").val($("#items\\[0\\]\\.address").val());
        typeTemplate.find(":input[name=zipcode]").val($("#items\\[0\\]\\.zipcode").val());
        typeTemplate.find(":input[name=city]").val($("#items\\[0\\]\\.city").val());
        typeTemplate.find(":input[name=country]").val($("#items\\[0\\]\\.country").val());

        $(":radio[name=familyMembershipFacility]:checked").trigger("change");
    });
    <g:if test="${!cmd?.items}">
        $("#add-member").click();
    </g:if>
    $(".member-list").find(".member-type .delete-member").hide()
});

function changeMembershipType(e, index) {
    var template = $(".member-fields-template[data-facility=" + $(e?.target).data("facility") + "]").clone();

    template.removeClass("member-fields-template");
    template.addClass("member-fields");

    template.find(":input").each(function() {
        var name = "items[" + index + "]." + $(this).attr("name");
        var id = "items[" + index + "]." + ($(this).attr("id") || $(this).attr("name"));
        $(this).attr("name", name).attr("id", id).parent().find("label").attr("for", id);
    });

    var fieldsContainer = $(e.target).parents(".member-type").find(".fields-container");

    template.find("input").each(function() {
        $(this).val(fieldsContainer.find("[name='" + $(this).attr("name") + "']").val())
    })

    fieldsContainer.html("");
    fieldsContainer.append(template);
    template.show();

    template.find("select").selectpicker({
        title: "${message(code: 'membershipRequest.index.message14')}"
        });
    }

    function changeCustomerType(e, organization) {
        $(e?.target).parents(".member-fields").find(".membership-fields").hide()
        $(e?.target).parents(".member-fields").find("[data-facility=" + $(e?.target).data("facility") + "]").show()

        if (organization == null) {
            organization = isOrganizationMembershipTypeSelected();
        }

        $(".person-control").toggle(organization != "true")
                .find(":input").prop("disabled", organization == "true");
        $(".organization-control").toggle(organization == "true")
                .find(":input").prop("disabled", organization != "true");
    }

    function isOrganizationMembershipTypeSelected(){
        return $(":radio[name$='.membershipType']:checked").attr("data-organization");
    }

</g:javascript>
</body>
