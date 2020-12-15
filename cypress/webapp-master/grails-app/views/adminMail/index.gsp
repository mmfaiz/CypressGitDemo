<%@ page import="org.joda.time.DateTime; grails.util.GrailsUtil; com.matchi.Facility; java.io.File" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>MATCHi <g:message code="adminHome.index.title"/></title>
</head>
<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><g:message code="adminMail.index.heading"/></li>
    </ol>

    <div class="panel panel-default panel-admin">
        <div class="panel-body">
            <!-- MAIL -->
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th><g:message code="default.date.label"/></th>
                        <th class="col-md-2"><g:message code="adminMail.created.label"/></th>
                        <th class="col-md-2"><g:message code="adminMail.sent.label"/></th>
                        <th class="col-md-2"><g:message code="adminMail.error.label"/></th>
                        <th class="col-md-2"></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${mailsByDate}" var="date">
                        <tr>
                            <td>${date.created_date}</td>
                            <td>${date.created}</td>
                            <td>${date.sent}</td>
                            <td>${date.error}</td>
                            <td>
                                <g:if test="${date.error > 0}">
                                    <g:link class="btn btn-success btn-xs" action="resend" params="[date: date.created_date]">
                                        <g:message code="adminMail.resend.label"/>
                                    </g:link>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

</body>
</html>
