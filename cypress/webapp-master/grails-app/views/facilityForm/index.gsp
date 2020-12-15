<%@ page import="com.matchi.dynamicforms.Submission" %>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="form.label.plural"/></title>
</head>

<body>
<div class="container content-container">

    <ol class="breadcrumb">
        <li><i class="ti-write"></i> <g:message code="form.label.plural"/></li>
    </ol>

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
                <g:sortableColumn property="name" titleKey="form.name.label"/>
                <th>
                    <g:message code="facilityForm.index.formLink"/>
                </th>
                <g:sortableColumn property="activeFrom" titleKey="form.activeFrom.label"/>
                <g:sortableColumn property="activeTo" titleKey="form.activeTo.label"/>
            </th>
                <th><g:message code="submission.label.plural"/>
                </th>
            </tr>
            </thead>
            <tbody data-link="row" class="rowlink">
            <g:set var="today" value="${new Date().clearTime()}"/>
            <g:each in="${forms}" var="formInstance">
                <tr>
                    <td>
                        <g:link action="submissions" id="${formInstance.id}">${formInstance.name.encodeAsHTML()}</g:link>
                    </td>
                    <td class="rowlink-skip">
                        <g:if test="${formInstance.activeFrom <= today && today <= formInstance.activeTo}">
                            <g:link controller="form" action="show" params="[hash: formInstance.hash]" absolute="true" target="_blank">
                                ${createLink(controller: "form", action: "show", params: [hash: formInstance.hash], absolute: true)}
                            </g:link>
                        </g:if>
                        <g:else>
                            ${createLink(controller: "form", action: "show", params: [hash: formInstance.hash], absolute: true)}
                        </g:else>
                    </td>
                    <td class="center-text">
                        <g:formatDate date="${formInstance.activeFrom}" formatName="date.format.dateOnly"/>
                    </td>
                    <td class="center-text">
                        <g:formatDate date="${formInstance.activeTo}" formatName="date.format.dateOnly"/>
                    </td>
                    <td class="center-text">
                        <g:message code="facilityForm.index.submissionsValue" args="[Submission.countByForm(formInstance)]"/>
                    </td>
                </tr>
            </g:each>
            </tbody>
            <g:if test="${!forms}">
                <tfoot>
                <tr>
                    <td colspan="6" class="vertical-padding20">
                        <span class="text-muted text-md"><i class="ti-info-alt"></i><g:message code="default.noElements"/></span>
                    </td>
                </tr>
                </tfoot>
            </g:if>
        </table>
    </div><!-- /.panel -->

    <g:if test="${formsCount > 50}">
        <div class="text-center">
            <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered" maxsteps="0" max="50" action="index" total="${formsCount}"/>
        </div>
    </g:if>
</div><!-- /.container -->
</body>
</html>
