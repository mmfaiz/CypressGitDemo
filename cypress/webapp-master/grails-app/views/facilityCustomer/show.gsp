<%@ page import="com.matchi.DateUtil; com.matchi.FacilityProperty; com.matchi.enums.BookingGroupType; com.matchi.BookingGroupFrequencyHandler; org.joda.time.DateTime; com.matchi.Slot; com.matchi.Customer; com.matchi.Sport; com.matchi.Court; com.matchi.facility.FacilityCustomerController" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayoutBooking"/>
    <title><g:message code="facilityCustomer.show.message1"/></title>
    <r:require modules="matchi-customerselect, jquery-validate, mousetrap"/>
</head>

<body>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12" style="min-width: 960px;">
            <div style="margin-left: 25px;margin-right: 25px;">
                <g:flashMessage/>
                <g:flashError/>
            </div>
            <g:errorMessage bean="${customer}"/>

            <ul class="breadcrumb" style="margin-left: 25px;margin-right: 25px;">
                <li>
                    <a href="${prevCustomerId ? createLink(action: 'show', id: prevCustomerId) : 'javascript: void(0)'}"
                       class="right-margin5">
                        <i class="icon-chevron-left ${prevCustomerId ? '' : 'transparent-30'}"></i>
                    </a>
                </li>

                <li>
                    <g:set var="targetCustomerController"
                           value="${session[FacilityCustomerController.CUSTOMER_LIST_CONTROLLER_KEY] ?: 'facilityCustomer'}"/>
                    <g:link controller="${targetCustomerController}" action="index"><g:message
                        code="facilityCustomer.show.list.${targetCustomerController}"/></g:link><span
                    class="divider">/</span>
                </li>
                <li class="active">
                    # ${customer?.number} - ${customer.fullName()}
                </li>

                <a href="${nextCustomerId ? createLink(action: 'show', id: nextCustomerId) : 'javascript: void(0)'}"
                   class="left-margin8 pull-right">
                    <i class="icon-chevron-right ${nextCustomerId ? '' : 'transparent-30'}"></i>
                </a>
            </ul>
        </div>
    </div>

    <div class="row-fluid">
        <div class="span12" style="min-width: 960px">
            <div class="row-fluid">
                <div class="span3" style="max-width: 340px;">
                    <div class="well well-small" style="margin-left: 25px;">
                        <div class="block">
                            <div id="user-image" class="pull-left right-margin10" style="position: relative;">
                                <g:staleFileArchiveUserImage id="${customer.user?.id}" size="small"/>
                            </div>
                            <h5 title="${!customer.type.equals(Customer.CustomerType.ORGANIZATION) ? customer.fullName() : customer.companyname}"
                                class="pull-left no-margin ellipsis"
                                style="font-family: 'Helvetica Neue', Arial; max-width:72%">
                                # ${customer.number}<br>
                                ${!customer.type.equals(Customer.CustomerType.ORGANIZATION) ? customer.fullName() : customer.companyname}
                                <g:if
                                    test="${customer.type.equals(Customer.CustomerType.ORGANIZATION) && customer.contact}">
                                    <br>${customer.contact}
                                </g:if>
                                <g:if test="${customer.type}">
                                    <br><g:message code="customer.type.${customer.type}"/>
                                </g:if>
                                <g:else><br><em><g:message code="facilityCustomer.show.message3"/></em></g:else>
                            </h5>

                            <div class="clearfix"></div>
                        </div>
                        <g:if test="${customer.user}">
                            <small style="color: #777;">
                                <img src="${resource(dir: "images", file: "favicon.png")}" alt="MATCHi" width="14"
                                     height="12" style="vertical-align: text-bottom;"/>
                                <g:message code="customer.user.connected.description"/>
                                <br/>
                                <g:message code="facilityCustomer.show.message23"/>:
                                ${customer.user.lastLoggedIn ? new DateTime(customer.user.lastLoggedIn).toString(message(code: 'date.format.timeShort').toString()) : "-"}
                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <g:link action="unlink" id="${customer.id}"
                                            onclick="return confirm('${message(code: 'button.delete.confirm.message')}')">
                                        <i class="icon-remove"></i>
                                    </g:link>
                                </sec:ifAnyGranted>
                            </small><br>
                        </g:if>
                        <div class="space10"></div>

                        <div class="block">
                            <g:if test="${customer.facility != facility}">
                                <h4><g:message code="facilityCustomer.edit.message3"
                                               args="[customer.facility.name]"/></h4>
                            </g:if>
                        </div>

                        <div class="block">
                            <g:each in="${hierarchicalCustomers}">
                                <g:link action="show" id="${it.id}" class="btn"><i class="icon-user"></i> <g:message
                                    code="facilityCustomer.show.otherFacility" args="[it.facility.name]"/></g:link>
                            </g:each>
                        </div>

                        <div class="block">
                            <g:if test="${!customer.archived}">
                                <div class="btn-group">
                                    <g:link action="edit" id="${customer.id}" class="btn"><i
                                        class="icon-edit"></i> <g:message code="button.edit.label"/></g:link>
                                    <g:if test="${!customer.user}">
                                        <g:if test="${!customer.email}">
                                            <button class="btn btn-info transparent-60" rel="tooltip"
                                                    title="${message(code: 'facilityCustomer.show.message24')}"><g:message
                                                code="button.invite.label"/></button>
                                        </g:if>
                                        <g:elseif test="${customer.clubMessagesDisabled}">
                                            <button class="btn btn-info transparent-60" rel="tooltip"
                                                    title="${message(code: 'facilityCustomer.show.message27')}"><g:message
                                                code="button.invite.label"/></button>
                                        </g:elseif>
                                        <g:else>
                                            <g:link class="btn btn-info" action="invite" id="${customer.id}"><g:message
                                                code="button.invite.label"/></g:link>
                                        </g:else>
                                    </g:if>
                                </div>
                                <g:if test="${customer.inviteTickets.findAll { !it.consumed }.size() > 0}">
                                    <br>
                                    <small style="color: #777;">
                                        <g:message code="facilityCustomer.show.message25"/>:
                                        ${new DateTime(customer.inviteTickets.iterator().next().dateCreated).toString(message(code: 'date.format.timeShort').toString())}
                                    </small>
                                </g:if>
                                <br>
                                <small style="color: #777;">
                                    <g:message code="facilityCustomer.show.message26"/>:
                                    <g:formatDate date="${customer.lastUpdated}" formatName="date.format.timeShort"/>
                                </small>
                            </g:if>
                        </div>

                        <hr>

                        <dl class="dl-horizontal dl-compact">
                            <dt><i class="icon-envelope transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:if test="${!customer.email}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message6"/></span>
                                </g:if>
                                <g:else>
                                    <a href="mailto:${customer.email}"><strong
                                        title="${customer.email}">${customer.email}</strong></a>
                                    <g:if test="${customer.clubMessagesDisabled}">
                                        <i class="icon-ban-circle transparent-60"></i>
                                    </g:if>
                                </g:else>
                            </dd>

                            <dt><i class="icon-calendar transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:set var="personalNumber"
                                       value="${customer.isCompany() ? customer.orgNumber : customer.getPersonalNumber()}"/>
                                <g:if test="${!personalNumber}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message7"/></span>
                                </g:if>
                                <g:else>
                                    <strong>${personalNumber?.encodeAsHTML()}</strong>
                                </g:else>
                            </dd>

                            <dt><i class="icon-home transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:if
                                    test="${!customer.address1 && !customer.address2 && !customer.zipcode && !customer.city}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message8"/></span>
                                </g:if>
                                <g:else>
                                    <strong>
                                        <g:if test="${customer.address1}">
                                            ${customer.address1}<br>
                                        </g:if>
                                        <g:if test="${customer.address2}">
                                            ${customer.address2}<br>
                                        </g:if>
                                        ${customer.zipcode} ${customer.city}
                                        <g:if test="${customer.country}">
                                            <br>${message(code: 'country.' + customer.country)}
                                        </g:if>
                                    </strong>
                                </g:else>
                            </dd>

                            <dt><i class="icon-bell transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:if test="${!customer.telephone && !customer.cellphone}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message9"/></span>
                                </g:if>
                                <g:else>
                                    <strong>${customer.telephone} ${customer.cellphone ? " / " + customer.cellphone : ""}</strong>
                                </g:else>
                            </dd>

                            <dt><i class="icon-globe transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:if test="${!customer.web}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message10"/></span>
                                </g:if>
                                <g:else>
                                    <strong>${customer.web}</strong>
                                </g:else>
                            </dd>

                            <g:if
                                test="${customer.facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name()) && !customer.type.equals(Customer.CustomerType.ORGANIZATION)}">
                                <dt><i class="icon-home transparent-60"></i></dt>
                                <dd class="ellipsis" style="width: 80%">
                                    <g:if test="${!customer.club}">
                                        <span class="placeholder"><g:message code="default.club.label"/></span>
                                    </g:if>
                                    <g:else>
                                        <strong>${customer.club}</strong>
                                    </g:else>
                                </dd>
                            </g:if>

                        </dl>

                        <g:if test="${customer.guardianName || customer.guardianEmail || customer.guardianTelephone ||
                            customer.guardianName2 || customer.guardianEmail2 || customer.guardianTelephone2}">
                            <hr>
                            <h5><g:message code="customer.guardian.label"/></h5>
                            <g:if
                                test="${customer.guardianName || customer.guardianEmail || customer.guardianTelephone}">
                                <dl class="dl-horizontal dl-compact">
                                    <dt><i class="icon-home transparent-60"></i></dt>
                                    <dd class="ellipsis" style="width: 80%;">
                                        <g:if test="${!customer.guardianName}">
                                            <span class="placeholder"><g:message
                                                code="facilityCustomer.show.message12"/></span>
                                        </g:if>
                                        <g:else>
                                            <strong>${customer.guardianName}</strong>
                                        </g:else>
                                    </dd>
                                    <dt><i class="icon-home transparent-60"></i></dt>
                                    <dd class="ellipsis" style="width: 80%;">
                                        <g:if test="${!customer.guardianEmail}">
                                            <span class="placeholder"><g:message
                                                code="facilityCustomer.show.message6"/></span>
                                        </g:if>
                                        <g:else>
                                            <a href="mailto:${customer.guardianEmail}"><strong
                                                title="${customer.guardianEmail}">${customer.guardianEmail}</strong></a>
                                        </g:else>
                                    </dd>
                                    <dt><i class="icon-home transparent-60"></i></dt>
                                    <dd class="ellipsis" style="width: 80%;">
                                        <g:if test="${!customer.guardianTelephone}">
                                            <span class="placeholder"><g:message
                                                code="facilityCustomer.show.message14"/></span>
                                        </g:if>
                                        <g:else>
                                            <strong>${customer.guardianTelephone}</strong>
                                        </g:else>
                                    </dd>
                                </dl>
                            </g:if>
                            <g:if
                                test="${customer.guardianName2 || customer.guardianEmail2 || customer.guardianTelephone2}">
                                <dl class="dl-horizontal dl-compact">
                                    <dt><i class="icon-home transparent-60"></i></dt>
                                    <dd class="ellipsis" style="width: 80%;">
                                        <g:if test="${!customer.guardianName2}">
                                            <span class="placeholder"><g:message
                                                code="facilityCustomer.show.message12"/></span>
                                        </g:if>
                                        <g:else>
                                            <strong>${customer.guardianName2}</strong>
                                        </g:else>
                                    </dd>
                                    <dt><i class="icon-home transparent-60"></i></dt>
                                    <dd class="ellipsis" style="width: 80%;">
                                        <g:if test="${!customer.guardianEmail2}">
                                            <span class="placeholder"><g:message
                                                code="facilityCustomer.show.message6"/></span>
                                        </g:if>
                                        <g:else>
                                            <a href="mailto:${customer.guardianEmail2}"><strong
                                                title="${customer.guardianEmail2}">${customer.guardianEmail2}</strong>
                                            </a>
                                        </g:else>
                                    </dd>
                                    <dt><i class="icon-home transparent-60"></i></dt>
                                    <dd class="ellipsis" style="width: 80%;">
                                        <g:if test="${!customer.guardianTelephone2}">
                                            <span class="placeholder"><g:message
                                                code="facilityCustomer.show.message14"/></span>
                                        </g:if>
                                        <g:else>
                                            <strong>${customer.guardianTelephone2}</strong>
                                        </g:else>
                                    </dd>
                                </dl>
                            </g:if>
                        </g:if>

                        <hr>

                        <h5><g:message code="facilityCustomer.show.message15"/></h5>
                        <dl class="dl-horizontal dl-compact">
                            <dt><i class="icon-home transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:if test="${!customer.invoiceAddress1}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message16"/></span>
                                </g:if>
                                <g:else>
                                    <strong>${customer.invoiceAddress1}</strong>
                                </g:else>
                            </dd>
                            <dt><i class="icon-home transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:if test="${!customer.invoiceAddress2}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message17"/></span>
                                </g:if>
                                <g:else>
                                    <strong>${customer.invoiceAddress2}</strong>
                                </g:else>
                            </dd>

                            <dt><i class="icon-home transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:if test="${!customer.invoiceZipcode}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message18"/></span>
                                </g:if>
                                <g:else>
                                    <strong>${customer.invoiceZipcode}</strong>
                                </g:else>
                            </dd>

                            <dt><i class="icon-home transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:if test="${!customer.invoiceCity}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message19"/></span>
                                </g:if>
                                <g:else>
                                    <strong>${customer.invoiceCity}</strong>
                                </g:else>
                            </dd>

                            <dt><i class="icon-bell transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:if test="${!customer.invoiceTelephone}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message14"/></span>
                                </g:if>
                                <g:else>
                                    <strong>${customer.invoiceTelephone}</strong>
                                </g:else>
                            </dd>

                            <dt><i class="icon-envelope transparent-60"></i></dt>
                            <dd class="ellipsis" style="width: 80%">
                                <g:if test="${!customer.invoiceEmail}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message6"/></span>
                                </g:if>
                                <g:else>
                                    <strong>${customer.invoiceEmail}</strong>
                                </g:else>
                            </dd>

                        </dl>

                        <hr>
                        <dl class="dl-horizontal dl-compact">
                            <dt><i class="icon-pencil transparent-60"></i></dt>
                            <dd>
                                <g:if test="${!customer.notes}">
                                    <span class="placeholder"><g:message code="facilityCustomer.show.message22"/></span>
                                </g:if>
                                <g:else>
                                    <g:toHTML text="${customer.notes}"/>
                                </g:else>
                            </dd>
                        </dl>
                        <g:if test="${customer.facility.hasPersonalAccessCodes()}">
                            <dl class="dl-horizontal dl-compact">
                                <dt><i class="fas fa-key transparent-60"></i></dt>
                                <dd>
                                    <g:if test="${!customer.accessCode}">
                                        <span class="placeholder"><g:message
                                            code="facilityCustomer.show.message28"/></span>
                                    </g:if>
                                    <g:else>
                                        <g:toHTML text="${customer.accessCode}"/>
                                    </g:else>
                                </dd>
                            </dl>
                        </g:if>
                    </div>

                <!-- View order history, View user, Remove customer: Only visible for super admins -->
                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <div class="well well-small" style="margin-left: 25px;">
                            <div>
                                <a href="${createLink(controller: 'facilityCustomer', action: 'remove', params: [id: customer.id])}"
                                   onclick="return confirm('${message(code: 'button.delete.confirm.messageLong')}')"
                                   class="btn btn-inverse">
                                    <i class="fas fa-times"></i> <g:message
                                    code="facilityCustomer.show.adminButtons.removeCustomer"/>
                                </a>

                            </div>
                            <br/>
                            <g:if test="${customer.user != null}">
                                <div>
                                    <a href="${createLink(controller: 'adminOrder', params: [q: customer.user.email, start: new DateTime().minusMonths(1).format(DateUtil.DEFAULT_DATE_FORMAT), end: new DateTime().format(DateUtil.DEFAULT_DATE_FORMAT)])}"
                                       target="_blank" class="btn btn-inverse">
                                        <i class="fa fa-list-alt"></i> <g:message
                                        code="facilityCustomer.show.adminButtons.seeOrderHistory"/>
                                    </a>
                                    <a href="${createLink(controller: 'adminUser', action: 'edit', params: [id: customer.user.id])}"
                                       target="_blank" class="btn btn-inverse">
                                        <i class="fas fa-user"></i> <g:message
                                        code="facilityCustomer.show.adminButtons.seeUser"/>
                                    </a>
                                </div>
                            </g:if>
                        </div>
                    </sec:ifAnyGranted>
                </div>

                <div class="span4">
                    <g:if test="${customer.archived}">
                        <div class="info-box well well-small">
                            <h4><i class="fa fa-warning text-error"></i> <g:message
                                code="facilityCustomer.show.archivedWarning.label"/></h4>

                            <p>
                                <g:message code="facilityCustomer.show.archivedWarning.message1"/>

                                <g:if test="${customer.facility.hasIdrottOnlineMembershipSync()}">
                                    <g:message
                                        code="facilityCustomer.show.archivedWarning.idrottOnlineMemberSyncWarning"/>
                                    <g:if test="${customer.facility.hasIdrottOnlineActivitySync()}">
                                        <g:message
                                            code="facilityCustomer.show.archivedWarning.idrottOnlineActivitySyncWarning"/>
                                    </g:if>
                                </g:if>

                            </p>
                            <g:form name="archivedcustomerid" class="no-margin">
                                <g:hiddenField name="customerId" value="${customer.id}"/>
                            </g:form>
                            <g:set var="returnUrl"
                                   value="${createLink(controller: "facilityCustomer", action: "show", params: [id: customer.id])}"/>
                            <a class="btn btn-primary" href="javascript:void(0)"
                               onclick="submitFormTo('#archivedcustomerid', '<g:createLink action="unarchive" controller="facilityCustomerArchive" params="${[returnUrl: returnUrl]}" />');"><g:message
                                code="reactivate.label"/></a>
                        </div>
                    </g:if>
                    <div class="info-box">
                        <g:customerMembership customer="${customer}"/>
                    </div>

                    <g:customerMembershipInGroup customer="${customer}"/>

                    <g:if test="${customer.membership}">
                        <div class="info-box">
                            <g:customerMembershipFamily customer="${customer}"/>
                        </div>
                    </g:if>
                    <g:if test="${!customer?.facility?.isMasterFacility() && !facility?.isMasterFacility()}">
                        <div class="info-box">
                            <g:customerGroup customer="${customer}"/>
                        </div>
                    </g:if>
                    <g:if
                        test="${customer?.facility?.getFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_TRAINING_PLANNER)}">
                        <div class="info-box">
                            <g:customerCourseSubmission customer="${customer}"/>
                        </div>

                        <div class="info-box">
                            <g:customerCourse customer="${customer}"/>
                        </div>
                    </g:if>
                    <g:if
                        test="${customer?.facility?.getFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_FACILITY_DYNAMIC_FORMS)}">
                        <div class="info-box">
                            <g:customerEventActivity customer="${customer}"/>
                        </div>
                    </g:if>
                    <div class="info-box">
                        <g:customerCoupon customer="${customer}"/>
                    </div>

                    <g:customerCouponInGroup customer="${customer}"/>

                </div>

                <div class="span5">
                    <div style="margin-right: 25px;">
                        <g:if test="${!customer?.facility?.isMasterFacility() && !facility?.isMasterFacility()}">
                            <div class="info-box">
                                <g:customerSubscription customer="${customer}"/>
                            </div>

                            <div class="info-box">
                                <g:customerBooking customer="${customer}"/>
                            </div>
                            <g:if test="${customer.facility.hasApplicationInvoice()}">
                                <div class="info-box">
                                    <g:customerInvoice customer="${customer}"/>
                                </div>

                                <div class="info-box">
                                    <g:customerInvoiceRow customer="${customer}"/>
                                </div>
                            </g:if>
                            <g:if test="${customer.facility.hasApplicationCashRegister()}">
                                <div class="info-box">
                                    <g:customerCashRegisterTransactions customer="${customer}"/>
                                </div>
                            </g:if>
                            <g:if test="${customer.user?.getRoundedAverageSkillLevel()}">
                                <div class="info-box">
                                    <g:customerUserSportProfilesLevel user="${customer.user}"/>
                                </div>
                            </g:if>
                        </g:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="space40"></div>

<div id="customerModal" class="modal hide fade"></div>
<r:script>
    $(document).ready(function() {
        $("[rel='tooltip']").tooltip();

        $('#customerModal').modal({ show:false, dynamic: true });

        <g:if test="${customer.user}">
    $("#user-image").popover({
            width: 500,
            delay: 200,
            title: "<img src='${resource(dir: "images", file: "favicon.png")}' alt='MATCHi' width='15'
                         height='15'
                         style='vertical-align: text-top;'/>&nbsp;${message(code: 'customer.user.connected.label')}",
                    content: "" +
                    "<p>" +
    "<strong>${customer.user.fullName()}</strong><br>" +
    "${customer.user.email}<br>" +
        <g:if test="${customer.user.telephone}">
            "${customer.user.telephone}<br>" +
        </g:if>
        <g:if test="${customer.user.address}">
            "<br>" +
                        "${customer.user.address}<br>" +
                        "${customer.user.zipcode}, ${customer?.user?.municipality?.name}<br><br>" +
        </g:if>
        "${message(code: 'facilityCustomer.show.message23')}: <g:formatDate date="${customer.user.lastLoggedIn}"
                                                                            format="yyyy-MM-dd HH:mm"/>" +
        "</p>",
                trigger: "hover",
                placement: "right"
            });
</g:if>

    /**
        KeyBoard shourtcuts
     */
    Mousetrap.bind(['ctrl+left', 'command+left'], function(e) {
    <g:if test="${prevCustomerId}">
        window.location.href = '<g:createLink action="show" id="${prevCustomerId}"/>';
    </g:if>
    });

    Mousetrap.bind(['ctrl+right', 'command+right'], function(e) {
    <g:if test="${nextCustomerId}">
        window.location.href = '<g:createLink action="show" id="${nextCustomerId}"/>';
    </g:if>
    });
});
</r:script>
</body>
</html>
