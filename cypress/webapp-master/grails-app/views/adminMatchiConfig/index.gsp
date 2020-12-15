<%@ page import="com.matchi.MatchiConfigMethodAvailability; org.joda.time.DateTime; grails.util.GrailsUtil; com.matchi.Facility; java.io.File" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>MATCHi <g:message code="adminHome.index.title"/></title>
</head>
<body>
<div class="container content-container">
    <g:form action="update" name="matchiConfigForm" class="form panel-body no-padding">
        <div class="panel panel-default panel-admin">
            <div class="panel-body">
                <!-- JOBS -->
                    <h3><g:message code="admin.matchiConfig.title"/></h3>
                    <g:message code="admin.matchiConfig.description" args="${[com.matchi.MatchiConfigMethodAvailability.OFFICE_HOURS_START.toString("HH:mm"), com.matchi.MatchiConfigMethodAvailability.OFFICE_HOURS_END.toString("HH:mm")] as String[]}" />
                    <g:each in="${keys}" var="key">
                        <div class="form-group">
                            <label class="block" for="${key.key}">${key.title}</label>
                            <g:if test="${key.possibleValues instanceof List}">
                                <g:select name="${key.key}" from="${key.possibleValues}" value="${settings[key.key]?.value}" />
                            </g:if>
                            <g:else>
                                <g:textField name="${key.key}" value="${settings[key.key]?.value}" />
                            </g:else>
                        </div>
                    </g:each>

                <div class="form-group col-sm-12">
                    <g:submitButton name="submit" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                </div>
            </div>

        </div>
    </g:form>
</div>


</body>
</html>
