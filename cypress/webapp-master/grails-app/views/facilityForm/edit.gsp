<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="button.edit.label"/></title>
</head>

<body>

    <div class="container vertical-padding20">

        <ol class="breadcrumb">
            <li><i class=" ti-write"></i> <g:link action="index"><g:message code="form.label.plural"/></g:link></li>
            <li class="active">${formInstance.name.encodeAsHTML()} - <g:message code="button.edit.label"/></li>
        </ol>

        <g:b3StaticErrorMessage bean="${formInstance}"/>

        <div class="panel panel-default">
            <div class="panel-heading no-padding">
                <div class="tabs tabs-style-underline">
                    <nav>
                        <ul>
                            <li>
                                <g:link action="submissions" id="${formInstance.id}">
                                    <i class="ti-list"></i>
                                    <span><g:message code="submission.label.plural"/></span>
                                </g:link>
                            </li>
                            <li class="active tab-current">
                                <g:link action="edit" id="${formInstance.id}">
                                    <i class="ti-pencil"></i>
                                    <span><g:message code="button.edit.label"/></span>
                                </g:link>
                            </li>
                        </ul>
                    </nav>
                </div>
            </div>

            <g:form action="update" class="form panel-body">
                <g:hiddenField name="id" value="${formInstance.id}"/>
                <g:hiddenField name="version" value="${formInstance.version}"/>

                <div class="vertical-padding20">
                    <span class="block text-muted"><g:message code="default.form.edit.instructions"/></span>
                    <hr/>
                </div>

                <fieldset>
                    <g:render template="form"/>

                    <div class="text-right">
                        <g:link action="index" class="btn btn-default"><g:message code="button.cancel.label"/></g:link>
                        <g:actionSubmit action="delete" value="${message(code: 'button.delete.label')}" class="btn btn-danger"
                        onclick="return confirm('${message(code: 'facilityForm.delete.confirm')}')"/>
                        <g:submitButton name="update" value="${message(code: 'button.update.label')}" class="btn btn-success"/>
                    </div>
                </fieldset>
            </g:form>

        </div><!-- /.panel -->

    </div><!-- /.container -->
</body>
</html>
