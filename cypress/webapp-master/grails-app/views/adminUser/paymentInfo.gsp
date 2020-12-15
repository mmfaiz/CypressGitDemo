<%@ page import="com.matchi.Facility; com.matchi.Role" contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="adminUser.edit.paymentInfo"/></title>
    <r:script>
        function confirmDelete() {
            return confirm('${message(code: 'adminUser.paymentInfo.deleteConfirmation')}');
        };
    </r:script>
</head>
<body>

<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link action="index"><g:message code="user.label.plural"/></g:link></li>
        <li><g:link action="edit" params="[id: user.id]">${user.fullName()}</g:link></li>
        <li class="active"><g:message code="adminUser.edit.paymentInfo"/></li>
    </ol>

    <div class="panel panel-default">
        <div class="panel-heading no-padding">
            <div class="tabs tabs-style-underline">
                <nav>
                    <ul>
                        <li>
                            <g:link action="edit" params="[id: user.id]">
                                <i class="fa fa-pencil"></i>
                                <span><g:message code="button.edit.label"/></span>
                            </g:link>
                        </li>
                        <li>
                            <g:link action="devices" params="[id: user.id]">
                                <i class="fa fa-mobile-phone"></i>
                                <span><g:message code="adminUser.devices.devices"/></span>
                            </g:link>
                        </li>
                        <li class="active tab-current">
                            <g:link action="paymentInfo" params="[id: user.id]">
                                <i class="fas fa-credit-card"></i>
                                <span><g:message code="adminUser.edit.paymentInfo"/></span>
                            </g:link>
                        </li>
                    </ul>
                </nav>
            </div>

            <table class="table table-striped table-hover table-bordered">
                <tr height="34">
                    <th><g:message code="adminUser.paymentInfo.issuer"/></th>
                    <th><g:message code="adminUser.paymentInfo.maskedPan"/></th>
                    <th><g:message code="adminUser.paymentInfo.expireDate"/></th>
                    <th><g:message code="adminUser.paymentInfo.dateCreated"/></th>
                    <th><g:message code="adminUser.paymentInfo.lastUpdated"/></th>
                    <th><g:message code="adminUser.paymentInfo.provider"/></th>
                    <th><g:message code="adminUser.paymentInfo.delete"/></th>
                </tr>
            </thead>
                <tbody>
                <g:if test="${paymentInfos}">
                    <g:each in="${paymentInfos}" var="${paymentInfo}">
                        <tr>
                            <td>${paymentInfo.issuer}</td>
                            <td>${paymentInfo.maskedPan}</td>
                            <td>${paymentInfo.formatExpiryDate()}</td>
                            <td>${paymentInfo.dateCreated}</td>
                            <td>${paymentInfo.lastUpdated}</td>
                            <td>${paymentInfo.provider.toString()}</td>
                            <td>
                                <form method="POST" action="${createLink(action: 'deletePaymentInfo')}" onsubmit="return confirmDelete();">
                                    <input type="hidden" value="${paymentInfo.id}" name="paymentInfoId">
                                    <button class="btn btn-danger btn-sm" type="submit">
                                        <g:message code="adminUser.paymentInfo.delete"/>
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </g:each>
                </g:if>
                <g:else>
                    <tr>
                        <td colspan="5"><g:message code="adminUser.paymentInfo.noPaymentInfo"/></td>
                    </tr>
                </g:else>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
