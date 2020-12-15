<html>
<head>
    <meta name="layout" content="facilityLayout">
    <title><g:message code="facilityAccessCodeImport.import.title"/></title>
</head>

<body>
    <ul class="breadcrumb">
        <li><g:link controller="facilityAdministration" action="index"><g:message code="facilityAdministration.index.title"/></g:link> <span class="divider">/</span></li>
        <li><g:link controller="facilityAccessCode" action="index"><g:message code="facilityAccessCode.label.plural"/></g:link> <span class="divider">/</span></li>
        <li class="active"><g:message code="facilityAccessCodeImport.import.title"/></li>
    </ul>

    <g:render template="/templates/wizard" model="[steps: [message(code: 'facilityAccessCodeImport.import.step1'),
            message(code: 'facilityAccessCodeImport.import.step2'), message(code: 'facilityAccessCodeImport.import.step3'),
            message(code: 'default.completed.message')], current: 2]"/>

    <h3><g:message code="facilityAccessCodeImport.import.confirm.heading"/></h3>

    <p class="lead"><g:message code="facilityAccessCodeImport.import.confirm.description"/></p>

    <g:form>
        <div class="well" style="overflow-x: scroll">
            <table id="columns" class="table table-transparent table-condensed">
                <thead>
                    <tr>
                        <th width="30"><g:message code="default.status.label"/></th>
                        <th><g:message code="facilityAccessCodeImport.import.confirm.validFrom"/></th>
                        <th><g:message code="facilityAccessCodeImport.import.confirm.validTo"/></th>
                        <th><g:message code="facilityAccessCodeImport.import.confirm.code"/></th>
                        <th><g:message code="facilityAccessCodeImport.import.confirm.courts"/></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${accessCodeData}" var="accessCode" status="i">
                        <tr>
                            <g:if test="${accessCode.error}">
                                <td class="${i}_popover"><span class="label label-warning"><i class="icon-exclamation-sign"></i>
                                </span></td>
                            </g:if>
                            <g:else>
                                <td><span class="label label-success"><g:message code="default.status.ok"/></span></td>
                            </g:else>
                            <td class="ellipsis">${accessCode.cmd.validFrom?.encodeAsHTML()}</td>
                            <td class="ellipsis">${accessCode.cmd.validTo?.encodeAsHTML()}</td>
                            <td class="ellipsis">${accessCode.cmd.content?.encodeAsHTML()}</td>
                            <td class="ellipsis">${accessCode.cmd.courts?.encodeAsHTML()}</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>

        <div class="form-actions">
            <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            <g:submitButton name="submit" type="submit" value="${message(code: 'button.confirm.label')}"
                    data-toggle="button" class="btn btn-success pull-right" show-loader="${message(code: 'default.loader.label')}"/>
            <g:link event="previous" class="btn btn-info pull-right right-margin5"><g:message code="button.back.label"/></g:link>
        </div>
    </g:form>
</body>
</html>
