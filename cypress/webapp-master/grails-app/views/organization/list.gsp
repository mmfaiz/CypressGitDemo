<%@ page import="com.matchi.facility.Organization" %>
<!doctype html>
<html>
<head>
  <meta name="layout" content="b3facility"/>
  <g:set var="entityName" value="${message(code: 'organization.label')}"/>
  <title>MATCHi - <g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="container content-container">
  <ol class="breadcrumb">
    <li><i class=" ti-write"></i> <g:message code="default.list.label" args="[entityName]"/></li>
  </ol>

  <g:flashMessage/>
  <div class="panel panel-default panel-admin">

    <div class="panel-heading table-header">
      <div class="text-right">
        <g:link action="create" class="btn btn-xs btn-success">
          <i class="ti-plus"></i> <g:message code="button.add.label"/>
        </g:link>
      </div>
    </div>

    <table class="table table-striped table-hover table-bordered">
      <thead>
      <tr>
        <g:sortableColumn property="name" titleKey="organization.name.label" class="vertical-padding20"/>
        <g:sortableColumn property="number" titleKey="organization.number.label" class="vertical-padding20"/>

      </tr>
      </thead>
      <tbody>
      <g:each in="${organizationInstanceList}" status="i" var="organizationInstance">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

          <td><g:link action="edit"
                      id="${organizationInstance.id}">${fieldValue(bean: organizationInstance, field: "name")}</g:link></td>

          <td>${fieldValue(bean: organizationInstance, field: "number")}</td>
        </tr>
      </g:each>
      </tbody>
      <g:if test="${!organizationInstanceList}">
        <tfoot>
        <tr>
          <td colspan="6" class="vertical-padding20">
            <span class="text-muted"><g:message code="default.noElements"/></span>
          </td>
        </tr>
        </tfoot>
      </g:if>
    </table>
  </div>

  <g:if test="${organizationInstanceTotal > 50}">
    <div class="text-center">
      <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered" maxsteps="0" max="50" action="index" total="${organizationInstanceTotal}"/>
    </div>
  </g:if>
</div>
</body>
</html>
