<%@ page import="com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>MATCHi <g:message code="adminHome.index.title"/></title>
</head>
<body>

<div class="container content-container">
    <ol class="breadcrumb">
        <li><g:message code="adminContactMe.index.heading" args="[signups.size()]"/></li>
    </ol>

    <div class="panel panel-default panel-admin">
        <table class="table table-striped table-hover table-bordered">
            <thead>
            <tr height="34">
                <th width="120"><g:message code="default.name.label"/></th>
                <th width="120"><g:message code="adminContactMe.index.email"/></th>
                <th width="120"><g:message code="facility.label"/></th>
                <th width="120"><g:message code="adminContactMe.index.type"/></th>
                <th width="120"><g:message code="default.date.label"/></th>
                <th width="80" class="text-center"><g:message code="default.status.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${signups}" var="contactme">
                <tr>
                    <td>${contactme.name}</td>
                    <td>${contactme.email}</td>
                    <td>${contactme.facility}</td>
                    <td>${contactme.type}</td>
                    <td width="150"  class="center-text" width="150"><g:formatDate date="${contactme.dateCreated}" format="yyyy-MM-dd HH:mm:ss"/> </td>
                    <td class="text-center" width="150">
                        <g:if test="${contactme.contacted}">
                            <g:link action="toggle" id="${contactme.id}">
                                <button class="btn btn-success btn-xs" href="#"><g:message code="adminContactMe.index.contacted"/></button>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link action="toggle" id="${contactme.id}">
                                <button class="btn btn-danger btn-xs" href="#"><g:message code="adminContactMe.index.notContacted"/></button>
                            </g:link>
                        </g:else>



                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
