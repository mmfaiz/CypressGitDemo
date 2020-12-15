<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="requirementProfile.label.plural"/></title>
    <r:require modules="jquery-timepicker, datejs, jquery-multiselect-widget"/>
</head>
<body>
<ul class="breadcrumb">
    <li class="active"><g:message code="requirementProfile.label.plural"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <div class="action-bar">
        <div class="btn-toolbar-right">
            <g:link action="create" class="btn btn-inverse">
                <span><g:message code="facilityRequirements.create.title"/></span>
            </g:link>
        </div>
    </div>
    <div class="row">
        <div class="span12">
            <table class="table table-striped table-bordered table-hover" data-provides="rowlink">
                <thead>
                <tr>
                    <th><g:message code="default.name.label"/></th>
                </tr>
                </thead>

                <g:if test="${profiles.size() == 0}">
                    <tr>
                        <td colspan="2"><i><g:message code="facilityRequirements.index.noneFound"/></i></td>
                    </tr>
                </g:if>

                <g:each in="${profiles}" var="profile">
                    <tr>
                        <td><g:link action="edit" params="[id: profile.id]">${profile.name}</g:link></td>
                    </tr>
                </g:each>
            </table>
        </div>
    </div>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>

<r:script>
</r:script>
</body>
</html>
