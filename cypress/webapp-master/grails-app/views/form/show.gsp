<%@ page import="com.matchi.dynamicforms.FormField" %>
<html>
<head>
    <meta name="paymentDialogIncluded" content="true"/>
    <meta name="layout" content="${hasFacilityFullRights() ? 'b3facility' : 'b3main'}">
    <title><g:message code="course.form.submission.label" args="[formInstance.activity?.name]" default="${formInstance.name.encodeAsHTML()}"/></title>
    <r:script>
        $(function() {
            $("[rel='tooltip']").tooltip();

            $(":checkbox[required]").css("display", "block");
            if($(":checkbox").is(":checked")) {
                $(":checkbox").change();
            }
        });
    </r:script>
</head>

<body>
<g:b3StaticErrorMessage bean="${submission}"/>
<g:set var="checkboxesRequired" value="${fields.findAll { it.type.contains(FormField.Type.CHECKBOX.name()) && it.isRequired }}"/>
<div>
    <div class="container vertical-padding40">
        <div class="row">
            <div class="col-sm-8 col-sm-offset-2">
                <div class="panel panel-default panel-admin">
                    <div class="panel-heading">
                        <h1 class="h3">
                            <i class="ti-write"></i> <g:message code="course.form.submission.label" args="[formInstance.activity?.name]" default="${formInstance.name.encodeAsHTML()}"/>
                        </h1>
                        <g:if test="${formInstance?.membershipRequired || formInstance?.activity}">
                            <ul class="list-inline">
                                <g:if test="${formInstance?.activity}">
                                    <li>
                                        <small>
                                            <i class="fa fa-calendar"></i>
                                            <g:formatDate date="${formInstance?.activity?.startDate}" formatName="date.format.readable.year"/>
                                            <g:if test="${formInstance.activity.endDate.after(formInstance.activity.startDate)}">
                                                -
                                                <g:formatDate date="${formInstance.activity.endDate}" formatName="date.format.readable.year"/>
                                            </g:if>
                                        </small>
                                    </li>
                                </g:if>
                                <g:if test="${formInstance?.membershipRequired}">
                                    <li>
                                        <span class="label label-info">
                                            <g:message code="course.membershipRequired.label"/>
                                        </span>
                                    </li>
                                </g:if>
                            </ul>
                        </g:if>

                        <g:if test="${formInstance.activity?.description || formInstance.description}">
                            <p class="top-margin30">${g.toRichHTML(text: formInstance.activity?.description ?: formInstance.description)}</p>
                        </g:if>
                    </div>
                    <g:if test="${formInstance.paymentRequired && formInstance.price}">
                        <div class="panel-heading">
                            <div class="block">
                                <span class="text-muted"><i class="ti-credit-card"></i> <g:formatMoney value="${formInstance.price}" facility="${formInstance.facility}"/></span>
                            </div>
                        </div>
                    </g:if>

                    <div class="panel-body">
                        <g:form action="submit" class="form">
                            <g:hiddenField name="id" value="${formInstance.id}"/>
                            <g:hiddenField name="returnUrl" value="${params?.returnUrl}" />
                            <g:ifFacilityAccessible>
                                <g:hiddenField name="customerId" value="${params.customerId}"/>
                            </g:ifFacilityAccessible>

                            <g:each in="${fields}" var="formField">
                                <g:render template="/templates/dynamicForms/field/${formField.type.toLowerCase()}"
                                          model="[formField: formField]"/>
                                <div class="clearfix"></div>
                                <hr>
                            </g:each>

                            <g:if test="${!preview && !formInstance.paymentRequired && hasFacilityFullRights()}">
                                <div class="pull-right left-margin20">
                                    <g:actionSubmit action="submitAndSendNotification" class="btn btn-success"
                                            value="${message(code: 'course.sendSubmissionNotification.label')}"/>
                                </div>
                            </g:if>
                            <g:else>
                                <g:hiddenField name="sendSubmissionNotification" value="true"/>
                            </g:else>

                            <div class="pull-right">
                                <g:if test="${preview}">
                                    <a href="javascript: void(0)" class="btn btn-success" onclick="window.close()">
                                        <g:message code="button.closewindow.label"/>
                                    </a>
                                </g:if>
                                <g:else>
                                    <g:if test="${formInstance.paymentRequired && formInstance.price && !hasAccessToFacility(facility: formInstance.facility)}">
                                        <sec:ifLoggedIn>
                                            <g:actionSubmit action="submitAndPay" class="btn btn-success"
                                                            value="${message(code: 'form.show.submitAndPay')}"/>
                                        </sec:ifLoggedIn>
                                        <sec:ifNotLoggedIn>
                                            <div rel="tooltip" title="${message(code: 'form.show.loginRequired.tooltip')}">
                                                <input type="button" class="btn btn-inverse" disabled
                                                       value="${message(code: 'form.show.submitAndPay')}"/>
                                            </div>
                                        </sec:ifNotLoggedIn>
                                    </g:if>
                                    <g:else>
                                        <g:actionSubmit action="submit" class="btn btn-success"
                                                        value="${!hasAccessToFacility(facility: formInstance.facility) ? message(code: 'button.submit.label') : message(code: 'button.save.label')}"/>
                                    </g:else>
                                </g:else>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<g:if test="${pay}">
    <g:render template="/templates/payments/paymentDialog"/>
    <r:script>
    $(document).ready(function() {
        openPaymentDialog();
        $.ajax({
            type: "POST",
            data: {id: ${g.forJavaScript(data: formInstance.id)}, returnUrl: "${g.forJavaScript(data: params?.returnUrl)}"},
            url: "${g.forJavaScript(data: createLink(controller: 'formPayment', action: 'confirm'))}",
            success: function (data) {
                $("#userBookingModal").find(".modal-dialog").html(data);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 401) {
                        location.href = "${createLink(controller: 'login', action: 'auth',
                        params: [returnUrl: createLink(controller: 'form', action: 'showProtectedForm',
                        params: [hash: formInstance.hash ])])}";
                } else {
                    handleAjaxError(jqXHR, textStatus, errorThrown);
                }
            }
        });
    });
    </r:script>
</g:if>

<g:if test="${checkboxesRequired}">
    <r:script>

        <g:each in="${checkboxesRequired}">
            var ${g.forJavaScript(data: it.type)} = $(":checkbox[data-type='${g.forJavaScript(data: it.type)}']");
            ${g.forJavaScript(data: it.type)}.change(function() {
                if(${g.forJavaScript(data: it.type)}.is(":checked")) {
                    ${g.forJavaScript(data: it.type)}.removeAttr("required");
                } else {
                    ${g.forJavaScript(data: it.type)}.attr("required", "required");
                }
            });
        </g:each>

        $("input[type='submit']").click(function() {
            var checkboxes = $(":checkbox[data-type='${g.forJavaScript(data: FormField.Type.CHECKBOX.name())}']");
            var timeRangeCheckboxes = $(":checkbox[data-type='${g.forJavaScript(data: FormField.Type.TIMERANGE_CHECKBOX.name())}']");
            var found = [checkboxes, timeRangeCheckboxes];
            for(var i = 0; i < found.length; i++) {
                if (found[i].length > 1) {
                    found[i].map(function(idx, el) {
                        if ($(el).is("[required]")) {
                            return (el.setCustomValidity("${message(code: "formField.checkbox.required.validation.message")}"));
                        }
                        return el.setCustomValidity("");
                    });
                }
            }
        });
    </r:script>
</g:if>
</body>
</html>
