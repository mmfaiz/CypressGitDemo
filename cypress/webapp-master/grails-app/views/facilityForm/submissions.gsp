<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="submission.label.plural"/></title>
</head>

<body>
    <div class="container vertical-padding20">

        <ol class="breadcrumb">
            <li><i class=" ti-write"></i> <g:link action="index"><g:message code="form.label.plural"/></g:link></li>
            <li class="active">${formInstance.name.encodeAsHTML()} - <g:message code="submission.label.plural"/></li>
        </ol>

        <div class="panel panel-default panel-admin">
            <div class="panel-heading no-padding">
                <div class="tabs tabs-style-underline">
                    <nav>
                        <ul class="">
                            <li class="active tab-current">
                                <g:link action="submissions" id="${formInstance.id}">
                                    <i class="ti-list"></i>
                                    <span><g:message code="submission.label.plural"/></span>
                                </g:link>
                            </li>
                            <g:if test="${new Date() < formInstance.activeFrom}">
                                <li>
                                    <g:link action="edit" id="${formInstance.id}">
                                        <i class="ti-pencil"></i>
                                        <span><g:message code="facilityForm.edit.title"/></span>
                                    </g:link>
                                </li>
                            </g:if>
                        </ul>
                    </nav>
                </div>
            </div>
            <g:if test="${submissions}">
                <table id="submissionsTable" class="table table-striped table-hover table-bordered" data-provides="rowlink">
                    <thead>
                        <tr>
                            <g:sortableColumn property="dateCreated" titleKey="submission.dateCreated.label"
                                    params="[id: formInstance.id]"/>
                            <th><g:message code="submission.customer.label"/></th>
                        </tr>
                    </thead>

                    <tbody data-link="row" class="rowlink">
                        <g:each in="${submissions}" var="submission">
                            <tr>
                                <td>
                                    <g:link action="showSubmission" id="${submission.id}">
                                        <g:formatDate date="${submission.dateCreated}" format="${g.message(code: 'date.format.dateOnly')}"/>
                                    </g:link>
                                </td>
                                <td>
                                    ${submission.customer?.fullName()}
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </g:if>
            <g:else>
                <div class="panel-body bottom-padding80">
                    <span class="block text-muted text-md"><i class="ti-info-alt"></i> <g:message code="facilitySubmission.index.noSubmissions"/></span>
                </div>
            </g:else>

            <!-- PAGINATION -->
            <g:if test="${submissionsCount > 50}">
                <div class="text-center">
                    <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered" maxsteps="0" max="50" action="submissions" total="${submissionsCount}" params="[id: formInstance.id]"/>
                </div>
            </g:if>
        </div><!-- /.panel -->

    </div><!-- /.container -->
    <r:script>
        $(document).ready( function () {
            $('#submissionsTable').DataTable();
        } );
    </r:script>
</body>
</html>
