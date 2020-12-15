<%@ page import="com.matchi.Facility; com.matchi.facility.FacilityCustomerImportController" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerImport.import.title"/></title>
</head>
<body>

<g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerImport.import.wizard.step1'), message(code: 'facilityCustomerImport.import.wizard.step2'), message(code: 'facilityCustomerImport.import.wizard.step3'), message(code: 'default.completed.message')], current: 1]"/>

<h3><g:message code="facilityCustomerImport.import.columns.message2"/></h3>
<p class="lead">
    <g:message code="default.import.example" args="[resource(dir:'download',file:'example_customer_import.xls')]"/>
</p>

<g:form>
    <div class="well">
    <table id="columns" class="table table-transparent table-condensed">
        <thead>
        <tr>
            <th width="100"><g:message code="default.import.columnNumber"/></th>
            <th width="120"><g:message code="facilityCustomerImport.import.columns.message5"/></th>
            <th width="40"></th>
            <th width="400"><g:message code="default.import.matchingColumn"/></th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${FacilityCustomerImportController.VALID_FILE_COLUMNS}" status="i" var="col">
            <tr>
                <td>${i+1}</td>
                <td>${titles[i]}</td>
                <td class="center-text">→</td>
                <td colspan="2">
                    ${col.title} <g:if test="${col.desc}"><em>${col.desc}</em></g:if>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
    </div>
    <div class="form-actions">
        <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>

        <g:submitButton name="submit" type="submit" value="${message(code: 'button.next.label')}" data-toggle="button" class="btn btn-success pull-right" show-loader="${message(code: 'default.loader.label')}"/>
        <g:link event="previous" class="btn btn-info pull-right right-margin5"><g:message code="button.back.label"/></g:link>
    </div>
</g:form>
<r:script>
    $(document).ready(function(){
        /*
        $(".draggable").sortable({
            helper      : "clone",
            connectWith : ".sortable",
            containment : ".sortable"
        });
        */
    });
</r:script>
</body>
</html>
