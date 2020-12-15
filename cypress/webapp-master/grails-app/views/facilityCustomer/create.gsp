<%@ page import="com.matchi.membership.Membership; com.matchi.Customer; com.matchi.Sport; com.matchi.Court"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityCustomer.create.message1"/></title>
</head>
<body>

<g:errorMessage bean="${cmd}"/>
<g:errorMessage bean="${customer}"/>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="customer.label.plural"/></g:link><span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomer.create.message1"/></li>
</ul>

<g:form action="add" class="form-horizontal form-well" name="facilityCustomerCreateFrm">
    <g:hiddenField name="facilityId" value="${cmd?.facilityId}"/>
    <g:hiddenField name="returnUrl" value="${params?.returnUrl}"/>

    <div class="form-header">
        <g:message code="facilityCustomer.create.message33"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <g:render template="form"/>

        <hr>
        <h4 id="membership-control"><a href="javascript:void(0)"><g:message code="membership.label"/></a> <i id="membership-marker" class="icon-chevron-down"></i></h4>
        <div id="membership-content">
            <div class="control-group ${hasErrors(bean:cmd, field:'createMembership', 'error')}">
                <div class="controls">
                    <div class="control-group">
                        <label class="checkbox">
                            <g:checkBox name="createMembership" checked="${cmd?.createMembership}" onclick="\$('#membership-details').toggle();"/>
                            <g:message code="facilityCustomer.create.message44"/>
                        </label>
                    </div>
                </div>
            </div>

            <div id="membership-details" class="control-group" style="display: ${!cmd.createMembership ? "none":"block"};">
                <div class="control-group">
                    <label class="control-label" for="membershipType">
                        <g:message code="templates.customer.customerMembership.message3"/>
                    </label>
                    <div class="controls controls-row">
                        <select id="membershipType" name="membershipType" class="span3">
                            <option value=""><g:message code="membershipType.multiselect.noneSelectedText"/></option>
                            <g:each in="${types}">
                                <option value="${it.id}" data-start-date="${it.startDate}" data-end-date="${it.endDate}"
                                        data-grace-period-end-date="${it.gracePeriodEndDate}"
                                        ${cmd?.membershipType?.id == it.id ? 'selected' : ''}>${it.name}</option>
                            </g:each>
                        </select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="startDate">
                        <g:message code="membership.startDate.label.long"/>
                    </label>
                    <div class="controls">
                        <g:textField name="startDate" readonly="true" class="span3 center-text"
                                value="${cmd?.startDate}"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="endDate">
                        <g:message code="membership.endDate.label.long"/>
                    </label>
                    <div class="controls">
                        <g:textField name="endDate" readonly="true" class="span3 center-text"
                                value="${cmd?.endDate}"/>
                    </div>
                </div>

                 <div class="control-group">
                    <label class="control-label" for="gracePeriodEndDate">
                        <g:message code="membership.gracePeriodEndDate.label.long"/>
                    </label>
                    <div class="controls">
                        <g:textField name="gracePeriodEndDate" readonly="true" class="span3 center-text"
                                value="${cmd?.gracePeriodEndDate}"/>
                    </div>
                </div>

                <div class="control-group">
                    <div class="controls">
                        <g:render template="/templates/customer/membershipStartingGracePeriodDays"/>

                        <label class="checkbox" for="membershipPaid">
                            <g:checkBox name="membershipPaid" value="${cmd?.membershipPaid}"
                                    class="membership-paid-checkbox"/>
                            <g:message code="membershipCommand.paid.label"/>
                        </label>

                        <label class="checkbox" for="membershipCancel">
                            <g:checkBox name="membershipCancel" value="${cmd?.membershipCancel}"/>
                            <g:message code="membershipCommand.cancel.label"/>
                        </label>
                    </div>
                </div>
            </div>

        </div>
        <g:if test="${groups && groups.size() > 0}">
            <hr>
            <h4 id="group-control"><a href="javascript:void(0)"><g:message code="group.label.plural"/></a> <i id="group-marker" class="icon-chevron-down"></i></h4>
            <div id="group-content">
                <div class="control-group ${hasErrors(bean:cmd, field:'groupId', 'error')}">
                    <div class="controls">
                        <g:each in="${groups}">
                            <label class="checkbox">
                                <input type="checkbox" name="groupId" value="${it.id}" ${cmd?.groupId?.contains(it.id) ? "checked" : ""}/>${it.name}
                            </label>
                        </g:each>
                    </div>
                </div>
            </div>
        </g:if>
        <div class="form-actions">
            <g:actionSubmit action="add" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
<r:script>
    $(document).ready(function() {
        $("#group-control").on("click", function() {
            var toggleMarker = $("#group-marker");
            if(toggleMarker.hasClass('icon-chevron-right')) {
                toggleMarker.removeClass('icon-chevron-right')
                toggleMarker.addClass('icon-chevron-down')
            } else {
                toggleMarker.removeClass('icon-chevron-down')
                toggleMarker.addClass('icon-chevron-right')
            }

            $("#group-content").toggle();
        });
        $("#membership-control").on("click", function() {
            var toggleMarker = $("#membership-marker");
            if(toggleMarker.hasClass('icon-chevron-right')) {
                toggleMarker.removeClass('icon-chevron-right')
                toggleMarker.addClass('icon-chevron-down')
            } else {
                toggleMarker.removeClass('icon-chevron-down')
                toggleMarker.addClass('icon-chevron-right')
            }

            $("#membership-content").toggle();
        });

        $("#startDate,#endDate,#gracePeriodEndDate").datepicker({
            autoSize: true,
            dateFormat: 'yy-mm-dd'
        });
        $("#endDate,#gracePeriodEndDate").datepicker("option", "minDate", new Date());

        $("#endDate").on("change", function() {
            $("#gracePeriodEndDate").datepicker("option", "minDate", $(this).val());
        });
        $("#startDate").on("change", function() {
            var date = $(this).val();
            var today = new Date();
            $("#endDate").datepicker("option", "minDate",
                    date < today ? today : date).trigger("change");
        }).trigger("change");

        $("#membershipType").on("change", function() {
            if ($(this).val()) {
                var option = $(this).find("option:selected");
                $("#gracePeriodEndDate").val(option.attr("data-grace-period-end-date"));
                $("#endDate").val(option.attr("data-end-date"));
                $("#startDate").val(option.attr("data-start-date")).trigger("change");
            }
        });

        $("#facilityCustomerCreateFrm").preventDoubleSubmission({});
    });
</r:script>
</body>
</html>
