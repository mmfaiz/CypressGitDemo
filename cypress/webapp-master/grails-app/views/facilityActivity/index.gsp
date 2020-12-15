<%@ page import="com.matchi.Facility" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="default.activity.plural"/></title>
</head>
<body>
<div class="container content-container">
    <ul class="breadcrumb">
        <li><g:message code="default.activity.plural"/> (${activities.size()} <g:message code="unit.st"/>)</li>
    </ul>

    <g:if test="${!facility?.isMasterFacility()}">
        <div class="panel panel-default panel-admin">
            <g:render template="tabs"/>

            <div class="panel-heading table-header">
                <div class="row">
                    <div class="col-sm-3">
                        <g:form method="get">
                            <div class="input-group input-group-sm">
                                <g:textField id="activities-search-input" class="form-control" name="q" value="${params.q}" placeholder="${message(code: 'facilityActivity.index.message7')}"/>
                                <span class="input-group-btn">
                                    <button class="btn" type="submit"><span class="fas fa-search"></span></button>
                                </span>
                            </div>
                        </g:form>
                    </div>
                    <div class="col-sm-9">
                        <div class="text-right">
                            <g:link action="create" class="btn btn-xs btn-white">
                                <i class="ti-plus"></i> <g:message code="facilityActivity.create.title"/>
                            </g:link>
                        </div>
                    </div>
                </div>
            </div>


            <table class="table table-striped table-hover table-bordered">
                <thead>
                <tr>
                    <th><g:message code="default.name.label"/></th>
                    <th width="150"><g:message code="facilityActivity.index.message4"/></th>
                </tr>
                </thead>
                <tbody data-link="row" class="rowlink">
                <g:if test="${activities.size() == 0}">
                    <tr>
                        <td colspan="2"><i><g:message code="facilityActivity.index.message6"/></i></td>
                    </tr>
                </g:if>

                <g:each in="${activities}" var="activity">
                    <tr>
                        <td><g:link action="occasions" params="[id: activity.id]">${activity.name}</g:link></td>
                        <td>${activity.occasions.size()}<g:message code="unit.st"/></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:if>
    <g:else><g:message code="facility.onlyLocal"/></g:else>
</div>
</div>
<r:script>
    $(document).ready(function() {
        $(".search").focus();
    });
</r:script>
</div>
</body>
</html>