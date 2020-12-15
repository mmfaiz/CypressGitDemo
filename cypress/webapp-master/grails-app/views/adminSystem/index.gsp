<%@ page import="org.joda.time.DateTime; grails.util.GrailsUtil; com.matchi.Facility; java.io.File" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>MATCHi <g:message code="adminHome.index.title"/></title>
</head>
<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><g:message code="adminSystem.index.heading"/></li>
    </ol>

    <div class="panel panel-default panel-admin">
        <div class="panel-body">
            <div class="well">
                <h3><g:message code="adminSystem.index.settings"/></h3>
                <p><g:message code="adminSystem.index.settings.desc"/></p>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th><g:message code="default.name.label"/></th>
                        <th><g:message code="adminSystem.index.value"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><g:message code="adminSystem.index.serverTime"/></td>
                        <td>${new DateTime().toString("yyyy-MM-dd HH:mm")}</td>
                    </tr>
                    <tr>
                        <td><g:message code="adminSystem.index.passwordExpires"/></td>
                        <td>${grailsApplication.config.ticket.resetPassword.expiresInDays} days</td>
                    </tr>
                    <tr>
                        <td><g:message code="adminSystem.index.fbClientId"/></td>
                        <td>${grailsApplication.config.grails.plugins.springsocial.facebook.clientId}</td>
                    </tr>
                    <tr>
                        <td><g:message code="adminSystem.index.quartzStatus"/></td>
                        <td>
                            <g:if test="${scheduler.isStarted()}"><span class="label label-success"><g:message code="adminSystem.index.quartzRunning"/></span>
                            </g:if>
                            <g:else>
                                <span class="label label-important"><g:message code="adminSystem.index.quartzStopped"/></span>
                            </g:else>

                        </td>
                    </tr>
                    <tr>
                        <td><g:message code="adminSystem.index.quartzJobs"/></td>
                        <td>${scheduler.getJobGroupNames()}</td>
                    </tr>
                    </tbody>
                </table>
            </div>

            <div class="well">
                <h2><g:message code="adminSystem.index.grailsConfig"/></h2>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th><g:message code="default.name.label"/></th>
                        <th><g:message code="adminSystem.index.value"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><g:message code="adminSystem.index.environment"/></td>
                        <td>${GrailsUtil.getEnvironment()}</td>
                    </tr>

                    <tr>
                        <td><g:message code="adminSystem.index.grailsVersion"/></td>
                        <td><g:meta name="app.grails.version"/></td>
                    </tr>
                    <tr>
                        <td><g:message code="adminSystem.index.jvmVersion"/></td>
                        <td>${System.getProperty('java.version')}</td>
                    </tr>

                    <tr>
                        <td><g:message code="adminSystem.index.reloadingActive"/></td>
                        <td>${grails.util.Environment.reloadingAgentEnabled}</td>
                    </tr>
                    </tbody>
                </table>
            </div>

            <div class="well">
                <h2><g:message code="adminSystem.index.hardware"/></h2>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th><g:message code="default.name.label"/></th>
                        <th><g:message code="adminSystem.index.value"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><g:message code="adminSystem.index.harddrive"/></td>
                        <td>${(int) (new File("/").getFreeSpace() / 1000000) }mb / ${(int) (new File("/").getTotalSpace() / 1000000) }mb</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
