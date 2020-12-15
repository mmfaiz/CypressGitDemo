<%@ page import="org.joda.time.DateTime; com.matchi.Facility; com.matchi.Role" contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="adminUser.edit.title"/></title>
</head>
<body>
<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link action="index"><g:message code="user.label.plural"/></g:link></li>
        <li class="active"><g:message code="adminUser.edit.title"/></li>
    </ol>
    <g:b3StaticErrorMessage bean="${user}"/>
    <g:b3StaticErrorMessage bean="${cmd}"/>

    <div class="panel panel-default">
        <div class="panel-heading no-padding">
            <div class="tabs tabs-style-underline">
                <nav>
                    <ul>
                        <li class="active tab-current">
                            <g:link action="edit" params="[id: user.id]">
                                <i class="fa fa-pencil"></i>
                                <span><g:message code="button.edit.label"/></span>
                            </g:link>
                        </li>
                        <li>
                            <g:link action="devices" params="[id: user.id]">
                                <i class="fa fa-mobile-phone"></i>
                                <span><g:message code="adminUser.edit.devices"/></span>
                            </g:link>
                        </li>
                        <li>
                            <g:link action="paymentInfo" params="[id: user.id]">
                                <i class="fas fa-credit-card"></i>
                                <span><g:message code="adminUser.edit.paymentInfo"/></span>
                            </g:link>
                        </li>
                    </ul>
                </nav>
            </div>
        </div>
        <g:form action="update" name="userForm" class="form panel-body no-top-padding">
            <g:hiddenField name="id" value="${user?.id}"/>

            <div class="row well">
                <div class="form-group col-sm-6 ${hasErrors(bean: cmd, field: 'firstname', 'error')}">
                    <label class="control-label" for="firstname"><g:message code="default.firstname.label" default="Förnamn"/>*</label>
                    <g:textField name="firstname" value="${user?.firstname}" class="form-control"/>
                </div>

                <div class="form-group col-sm-6 ${hasErrors(bean: cmd, field: 'lastname', 'error')}">
                    <label for="lastname"><g:message code="default.lastname.label" default="Efternamn"/>*</label>
                    <g:textField name="lastname" value="${user?.lastname}" class="form-control"/>
                </div>


                <div class="form-group col-sm-12 ${hasErrors(bean: cmd, field: 'email', 'error')}">
                    <label for="email"><g:message code="default.email.label" default="Epost"/>*</label>
                    <g:textField name="email" value="${user?.email}" class="form-control"/>
                </div>
            </div>

            <div class="row">
                <g:if test="${!user.enabled && user.activationcode}">
                    <div class="form-group col-sm-12">
                        <label><g:message code="adminUser.edit.activationLink"/>:</label>
                        <g:set var="activationLink" value="${createLink(controller: 'userRegistration',
                                action: 'enable', absolute: 'true', params: [ac: user.activationcode,
                                        wl: params.wl ? 1 : '', f: params.f ?: ''])}"/>
                        <a href="${activationLink}">${activationLink}</a>
                    </div>
                </g:if>

                <g:if test="${customers}">
                    <div class="form-group col-sm-12">
                        <h4><g:message code="admin.user.customer.at"/>:</h4>
                        <table class="table table-striped">
                            <thead>
                                <th><g:message code="facility.label" /></th>
                                <th><g:message code="customer.number.label" /></th>
                                <th><g:message code="default.name.label" /></th>
                            </thead>
                            <tbody>
                            <g:each in="${customers}" var="customer">
                                <tr>
                                    <td>${customer.facility.name}</td>
                                    <td>${customer.number}</td>
                                    <td>${customer.fullName()}</td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </div>
                </g:if>

                <g:if test="${activeMemberships}">
                    <div class="form-group col-sm-12">
                        <h4><g:message code="membership.label.plural"/> (<g:message code="default.active.label" />):</h4>
                        <table class="table table-striped">
                            <thead>
                                <th><g:message code="facility.label" /></th>
                                <th><g:message code="customer.number.label" /></th>
                                <th></th>
                            </thead>
                            <tbody>
                            <g:each in="${activeMemberships}" var="membership">
                                <tr>
                                    <td>${membership.customer.facility.name}</td>
                                    <td>${membership.customer.number}</td>
                                    <td>${membership.type?.name}</td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </div>
                </g:if>

                <g:if test="${customerCoupons}">
                    <div class="form-group col-sm-12">
                        <h4><g:message code="offer.label"/> (<g:message code="default.available.label" />):</h4>
                        <table class="table table-striped">
                            <thead>
                                <th><g:message code="facility.label" /></th>
                                <th><g:message code="customer.number.label" /></th>
                                <th><g:message code="giftCard.nrOfTickets.label" /></th>
                                <th></th>
                            </thead>
                            <tbody>
                            <g:each in="${customerCoupons}" var="cc">
                                <tr>
                                    <td>${cc.coupon.facility.name}</td>
                                    <td>${cc.customer.number}</td>
                                    <g:if test="${cc.coupon.unlimited}">
                                        <td><g:message code="default.unlimited.label" /></td>
                                    </g:if>
                                    <g:else>
                                        <td>${cc.nrOfTickets}</td>
                                    </g:else>

                                    <g:if test="${cc.coupon instanceof com.matchi.coupon.GiftCard}">
                                        <th><g:message code="offers.giftCard.label" /></th>
                                    </g:if>
                                    <g:else>
                                        <th><g:message code="offers.coupon.label" /></th>
                                    </g:else>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </div>
                </g:if>

            </div>

            <div class="row well">
                <div class="form-group col-sm-6">
                    <label class="control-label" for="roles"><g:message code="adminUser.edit.roles"/></label>

                    <g:each var="role" in="${Role.findAll()}">
                        <div class="checkbox">
                            <g:checkBox id="role_${role.authority}" name="roles" class="form-control"
                                        checked="${user.isInRole(role.authority)}"
                                        value="${role.id}"/>
                            <label for="role_${role.authority}"><g:message code="auth.roles.${role.authority}"/></label>
                        </div>
                    </g:each>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12">
                    <a href="${createLink(controller: 'adminOrder', params: [q: user.email, start: new org.joda.time.DateTime().minusMonths(6).format(com.matchi.DateUtil.DEFAULT_DATE_FORMAT), end: new org.joda.time.DateTime().format(com.matchi.DateUtil.DEFAULT_DATE_FORMAT)])}" target="_blank" class="btn btn-success">
                        <i class="fas fa-list-alt"></i> <g:message code="facilityCustomer.show.adminButtons.seeOrderHistory" />
                    </a>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-12">
                    <g:submitButton name="submit" value="${message(code: "button.save.label", default: "Spara")}"
                                    class="btn btn-success"/>
                    <g:link action="reactivate" params="[userId: user?.id]" onclick="return confirm('${message(code: 'button.delete.confirm.message')}')" name="activation"
                            class="btn btn-info"><g:message code="button.resendActivation"
                                                            default="Skicka aktiveringsmail"/></g:link>
                    <g:link action="${user.accountLocked ? 'unlock' : 'lock'}" onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"
                            params="[id: user?.id]" name="lockage" class="btn btn-warning"><g:message
                            code="${user.accountLocked ? 'button.unblock' : 'button.block'}" default="Blockera"/></g:link>
                    <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label" default="Avbryt"/></g:link>
                    <g:link action="deleteUser" params="[id: user?.id]" class="btn btn-danger" onclick="return confirm('Are you SURE? This action cannot be undone!!')">
                        <g:if test="${canBeDeleted}">
                            Remove user (hard delete)
                        </g:if>
                        <g:else>
                            Remove user (soft delete)
                        </g:else>
                    </g:link>
                </div>
            </div>
        </g:form>
    </div>
</div>
</body>
</html>
