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
            message(code: 'default.completed.message')], current: 1]"/>

    <h3><g:message code="facilityAccessCodeImport.import.columns.heading"/></h3>

    <p class="lead"><g:message code="default.import.example"
            args="[resource(dir:'download',file:'example_customer_import.xls')]"/></p>

    <g:form>
        <div class="well">
            <table id="columns" class="table table-transparent table-condensed">
                <thead>
                    <tr>
                        <th width="100"><g:message code="default.import.columnNumber"/></th>
                        <th width="120"><g:message code="facilityAccessCodeImport.import.columns.fromFile"/></th>
                        <th width="40"></th>
                        <th width="400"><g:message code="default.import.matchingColumn"/></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${validColumns}" status="i" var="col">
                        <tr>
                            <td>${i + 1}</td>
                            <td>${titles[i]}</td>
                            <td class="center-text">→</td>
                            <td colspan="2">
                                ${col.title} ${col.desc ? "<em>" + col.desc + "</em>" : ""}
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>

        <div class="form-actions">
            <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            <g:submitButton name="submit" type="submit" value="${message(code: 'button.next.label')}"
                    data-toggle="button" class="btn btn-success pull-right" show-loader="${message(code: 'default.loader.label')}"/>
            <g:link event="previous" class="btn btn-info pull-right right-margin5"><g:message code="button.back.label"/></g:link>
        </div>
    </g:form>
</body>
</html>
