<%@ page import="org.joda.time.DateTime; grails.util.GrailsUtil; com.matchi.Facility; java.io.File" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>MATCHi <g:message code="adminHome.index.title"/></title>
</head>
<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li>MPC Status</li>
    </ol>

    <div class="panel panel-default panel-admin">
        <div class="panel-body">
            <div class="well">
                <!-- MPC Status  -->
                <h3>MPC Status</h3>
                <p>QT Systems</p>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Lastcall</th>
                        <th>Node</th>
                        <th>Mac</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${qtStatus}">
                        <tr>
                            <td>${it.key}</td>
                            <g:each in="${it.value}" var="val">
                                <td>${val?.value}</td>
                            </g:each>
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
