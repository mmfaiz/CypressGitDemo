<%@ page import="com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="templates.navigation.menuAdmin.message2"/></title>
</head>
<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><g:message code="adminHome.index.title"/></li>
    </ol>

    <div class="row">
        <g:link controller="adminUser" action="index" class="col-sm-3">
            <div class="well">
                <i class="fas fa-users"></i> <g:message code="user.label.plural"/>
            </div>
        </g:link>
        <g:link controller="adminFacility" action="index" class="col-sm-3">
            <div class="well">
                <i class="fas fa-building"></i> <g:message code="facility.label.plural"/>
            </div>
        </g:link>
        <g:link controller="adminOrder" action="index" class="col-sm-3">
            <div class="well">
                <i class="fas fa-shopping-cart"></i> <g:message code="order.label.plural"/>
            </div>
        </g:link>
        <g:link controller="adminStatistics" action="index" class="col-sm-3">
            <div class="well">
                <i class="fa fa-bar-chart"></i> <g:message code="adminHome.index.statistics"/>
            </div>
        </g:link>

        <g:link controller="adminGlobalNotification" action="index" class="col-sm-3">
            <div class="well">
                <i class="fas fa-volume-up"></i> <g:message code="globalNotification.label.plural"/>
            </div>
        </g:link>
        <g:link controller="adminFormTemplate" action="index" class="col-sm-3">
            <div class="well">
                <i class="fa fa-pencil"></i> <g:message code="formTemplate.label.plural"/>
            </div>
        </g:link>
    </div>
</div>
</body>
</html>