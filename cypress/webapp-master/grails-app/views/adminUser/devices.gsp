<%@ page import="com.matchi.Facility; com.matchi.Role" contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="adminUser.edit.title"/></title>
</head>
<body>

<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link action="index"><g:message code="user.label.plural"/></g:link></li>
        <li><g:link action="edit" params="[id: user.id]">${user.fullName()}</g:link></li>
        <li class="active"><g:message code="adminUser.devices.devices"/></li>
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
                        <li class="active tab-current">
                            <g:link action="devices" params="[id: user.id]">
                                <i class="fa fa-mobile-phone"></i>
                                <span><g:message code="adminUser.devices.devices"/></span>
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

            <table class="table table-striped table-hover table-bordered">
                <tr height="34">
                    <th class="text-center"><g:message code="adminUser.devices.type"/></th>
                    <th><g:message code="adminUser.devices.deviceId"/></th>
                    <th><g:message code="adminUser.devices.model"/></th>
                    <th><g:message code="device.deviceDescription.label"/></th>
                    <th><g:message code="default.created.label"/></th>
                    <th><g:message code="adminUser.devices.lastUsed"/></th>
                    <th class="center-text"></th>
                </tr>
            </thead>
                <tbody>
                <g:each in="${devices}" var="device">
                    <tr>
                        <td class="text-center"><r:img dir="/images/${device.getModelShortname()}_icon_small.png"/> </td>
                        <td>${device.deviceId}</td>
                        <td>${device.deviceModel}</td>
                        <td>${device.deviceDescription}</td>
                        <td>${device.getDateCreated()}</td>
                        <td>${device.getLastUsed()}</td>
                        <td class="text-center">
                            <g:if test="${device.blocked}">
                                <g:link action="activateDevice" params="[id: user.id, deviceId: device.id]" class="btn btn-success btn-xs"><g:message code="button.unblock"/></g:link>
                            </g:if>
                            <g:else>
                                <g:link action="blockDevice" params="[id: user.id, deviceId: device.id]" class="btn btn-warning btn-xs"><g:message code="button.block"/></g:link>
                            </g:else>
                        </td>
                    </tr>
                </g:each>
                <g:if test="${devices.isEmpty()}">
                    <tr>
                        <td colspan="7"><g:message code="adminUser.devices.noDevices"/></td>
                    </tr>
                </g:if>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
