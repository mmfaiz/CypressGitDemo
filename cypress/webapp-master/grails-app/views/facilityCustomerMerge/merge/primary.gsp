<%@ page import="org.joda.time.LocalTime; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerImport.import.title"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerMerge.merge.heading"/></li>
</ul>

<g:if test="${error}">
    <div class="alert alert-error">
        <a class="close" data-dismiss="alert" href="#">×</a>

        <h4 class="alert-heading">
            ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
        </h4>
        ${error}
    </div>
</g:if>

<g:render template="/templates/wizard"
          model="[steps: [message(code: 'facilityCustomerMerge.merge.wizard.step1'), message(code: 'facilityCustomerMerge.merge.wizard.step2'), message(code: 'default.completed.message')], current: 0]"/>

<h3><g:message code="facilityCustomerMerge.merge.primary.message4"/></h3>

<g:if test="${customers?.size() < 1}">
    <p class="lead">
        <g:message code="facilityCustomerMerge.merge.primary.message5"/></p>

    <div class="form-actions">
        <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
    </div>
</g:if>
<g:else>
    <p class="lead"><g:message code="facilityCustomerMerge.merge.primary.message7"/></p>

    <div class="alert">
        <button type="button" class="close" data-dismiss="alert"><i class="fa fa-times-circle"></i></button>
        <g:message code="facilityCustomerMerge.merge.primary.mergeNote"/>
    </div>

    <g:form>
        <div class="well">
            <table class="table table-transparent">
                <thead>
                <tr>
                    <th width="20"></th>
                    <th width="50"><g:message code="customer.number.label"/></th>
                    <th width="150"><g:message code="default.name.label"/></th>
                    <th width="150"><g:message code="customer.email.label"/></th>
                    <th width="100"><g:message code="customer.telephone.label"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${customers}" var="customer" status="i">
                    <tr>
                        <td>
                            <g:radio name="primary" value="${customer.id}" checked="${i == 0}"/>
                        </td>
                        <td width="50">${customer.number}</td>
                        <td width="150">${customer.fullName()}</td>
                        <td>${customer.email}</td>
                        <td width="50">${customer.telephone}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
        <div class="form-actions">
            <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            <g:submitButton name="submit" type="submit" value="${message(code: 'button.next.label')}" show-loader="${message(code: 'default.loader.label')}"
                            data-toggle="button" class="btn btn-success pull-right"/>
        </div>
    </g:form>

</g:else>
<r:script>
    $(document).ready(function() {
        $(".primary").on("change", function() {
            var check = $(this).is(":checked");

            if ( $(".primary").is(":checked") ) {
                $(".primary").attr('checked', false);
            }
            $(this).attr('checked', check);
        });
    });
</r:script>
</body>
</html>
