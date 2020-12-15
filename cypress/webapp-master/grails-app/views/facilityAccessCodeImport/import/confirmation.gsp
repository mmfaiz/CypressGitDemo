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
            message(code: 'default.completed.message')], current: 3]"/>

    <h3><g:message code="facilityAccessCodeImport.import.confirmation.heading"/></h3>
    <p class="lead"><g:message code="facilityAccessCodeImport.import.confirmation.description"
            args="[importedInfo.imported.size(), importedInfo.failed.size()]"/></p>

    <div class="form-actions">
        <div class="btn-toolbar pull-right">
            <g:link controller="facilityAccessCode" action="index" class="btn btn-info">
                <g:message code="button.confirm.label"/>
            </g:link>
        </div>
    </div>
</body>
</html>
