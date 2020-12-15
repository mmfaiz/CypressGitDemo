<html>
    <head>
        <meta name="layout" content="facilityLayout">
        <title><g:message code="facility.customer.removeFromGroup.title"/></title>
    </head>

    <body>
        <g:render template="add/breadcrumb" model="[wizardStep: 0]"/>

        <h3><g:message code="group.multiselect.noneSelectedText"/></h3>
        <p class="lead"><g:message code="facility.customer.removeFromGroup.select.desc"/></p>

        <g:form>
            <div class="well">
                <div class="control-group">
                    <div class="controls">
                        <g:each in="${facilityGroups}">
                            <label class="checkbox">
                                <input type="checkbox" name="groupId" value="${it.id}"/>${it.name}
                            </label>
                        </g:each>
                    </div>
                </div>
            </div>

            <div class="form-actions">
                <g:link event="cancel" class="btn btn-danger">
                    <g:message code="button.cancel.label"/>
                </g:link>
                <g:submitButton name="submit" type="submit" class="btn btn-success pull-right"
                        value="${message(code: 'button.next.label')}" data-toggle="button"
                        show-loader="${message(code: 'default.loader.label')}"/>
            </div>
        </g:form>
    </body>
</html>
