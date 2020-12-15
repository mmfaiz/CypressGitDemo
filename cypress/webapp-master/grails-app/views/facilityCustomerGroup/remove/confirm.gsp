<html>
    <head>
        <meta name="layout" content="facilityLayout">
        <title><g:message code="facility.customer.removeFromGroup.title"/></title>
    </head>

    <body>
        <g:render template="add/breadcrumb" model="[wizardStep: 1]"/>

        <h3><g:message code="button.confirm.label"/></h3>
        <p class="lead">
            <g:message code="facility.customer.removeFromGroup.confirm.desc"
                    args="[customers.size(), groups.name.join(', '), groups.size()]"/>
        </p>

        <g:form>
            <table class="table table-transparent">
                <thead>
                    <tr>
                        <th><g:message code="customer.number.label"/></th>
                        <th><g:message code="customer.label"/></th>
                        <th><g:message code="customer.email.label"/></th>
                        <th><g:message code="customer.telephone.label"/></th>
                        <th><g:message code="customer.type.label"/></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${customers}" var="customer">
                        <tr>
                            <td>${customer.number}</td>
                            <td>${customer.fullName()}</td>
                            <td>${customer.email}</td>
                            <td>${customer.telephone ?: customer.cellphone}</td>
                            <td><g:if test="${customer.type}"><g:message code="customer.type.${customer.type}"/></g:if></td>
                        </tr>
                    </g:each>
                </tbody>
            </table>

            <div class="form-actions">
                <g:link event="cancel" class="btn btn-danger">
                    <g:message code="button.cancel.label"/>
                </g:link>
                <g:submitButton name="submit" type="submit" class="btn btn-success pull-right"
                        value="${message(code: 'button.confirm.label')}" data-toggle="button"
                        show-loader="${message(code: 'default.loader.label')}"/>
            </div>
        </g:form>
    </body>
</html>
