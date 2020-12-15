<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>MATCHi - <g:message code="formTemplate.label.plural"/></title>
</head>

<body>
<div class="container content-container">

    <ol class="breadcrumb">
        <li><i class="ti-write"></i> <g:message code="formTemplate.label.plural"/></li>
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
                    <g:sortableColumn property="name" titleKey="formTemplate.name.label"/>
                    <g:sortableColumn property="description" titleKey="formTemplate.description.label"/>
                    <g:sortableColumn property="dateCreated" titleKey="formTemplate.dateCreated.label"/>
                    <g:sortableColumn property="lastUpdated" titleKey="formTemplate.lastUpdated.label"/>
                </tr>
            </thead>
            <tbody data-link="row" class="rowlink">
            <g:each in="${templates}" var="template">
                <tr>
                    <td><g:link action="edit" id="${template.id}">${template.name.encodeAsHTML()}</g:link></td>
                    <td>${template.description.encodeAsHTML()}</td>
                    <td><g:formatDate date="${template.dateCreated}"/></td>
                    <td><g:formatDate date="${template.lastUpdated}"/></td>
                </tr>
            </g:each>

            </tbody>
            <g:if test="${!templates}">
                <tfoot>
                <tr>
                    <td colspan="4">
                        <span class="text-muted text-md"><i class="ti-info-alt"></i> <g:message code="facilityForm.index.noForms"/></span>
                    </td>
                </tr>
                </tfoot>
            </g:if>
        </table>
    </div>

    <g:if test="${templatesCount > 50}">
        <div>
            <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                                          maxsteps="0" max="50" action="index" total="${templatesCount}"/>
        </div>
    </g:if>
</div>
</body>
</html>
