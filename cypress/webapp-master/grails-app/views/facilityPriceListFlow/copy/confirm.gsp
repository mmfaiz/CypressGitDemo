<%@ page import="org.joda.time.LocalTime" %>
<html>
    <head>
        <meta name="layout" content="facilityLayout">
        <title><g:message code="facilityPriceList.index.copy"/></title>
    </head>

    <body>
        <ul class="breadcrumb">
            <li>
                <g:link controller="facilityPriceList" action="index">
                    <g:message code="priceList.label.plural"/>
                </g:link>
                <span class="divider">/</span>
            </li>
            <li class="active"><g:message code="facilityPriceList.index.copy"/></li>
        </ul>

        <g:render template="/templates/wizard"
                model="[steps: [ message(code: 'facilityPriceList.copy.wizard.step1')], current: 0]"/>

        <g:if test="${error}">
            <div class="alert alert-error">
                <a class="close" data-dismiss="alert" href="#">×</a>
                <h4 class="alert-heading">
                    ${new LocalTime().toString("HH:mm:ss")}:  <g:message code="default.error.heading"/>
                </h4>
                ${error}
            </div>
        </g:if>

        <h3><g:message code="facilityPriceList.copy.wizard.step1"/></h3>
        <p class="lead">
            <g:message code="facilityPriceList.copy.wizard.step1.desc"/>
        </p>

        <g:form>
            <div class="well">
                <table class="table table-transparent">
                    <thead>
                        <tr>
                            <th></th>
                            <th class="span4"><g:message code="priceList.name.label"/></th>
                            <th class="span3"><g:message code="priceList.startDate.label"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${cmd?.items}" var="item" status="i">
                            <g:hiddenField name="items[$i].priceListId" value="${item.priceListId}"/>
                            <g:hiddenField name="items[$i].priceListName" value="${item.priceListName}"/>
                            <tr>
                                <td>${item.priceListName.encodeAsHTML()}</td>
                                <td>
                                    <g:textField name="items[$i].name" class="span3" value="${item.name}" maxlength="255"/>
                                </td>
                                <td>
                                    <g:textField name="items[$i].startDate" class="startDate center-text span2"
                                                 value="${formatDate(date: item.startDate,  formatName: 'date.format.dateOnly')}"/>
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </div>

            <div class="form-actions">
                <g:link event="cancel" class="btn btn-danger">
                    <g:message code="button.cancel.label"/>
                </g:link>
                <g:submitButton name="submit" type="submit" class="btn btn-success pull-right"
                        value="${message(code: 'facilityPriceList.copy.button.label')}" data-toggle="button"
                        show-loader="${message(code: 'default.loader.label')}"/>
            </div>
        </g:form>

        <g:javascript>
            $(document).ready(function() {
                $(".startDate").datepicker({
                    firstDay: 1,
                    autoSize: true,
                    dateFormat: "${message(code: 'date.format.dateOnly.small')}"
                });
            });
        </g:javascript>
    </body>
</html>
