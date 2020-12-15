<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="facilityCustomer.iosync.label"/></title>
    <r:require modules="jquery-multiselect-widget, matchi-selectall"/>
</head>

<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><g:message code="facilityCustomer.iosync.label"/></li>
    </ol>

    <div class="panel panel-default panel-admin">
        <div class="panel-heading no-padding">
            <div class="tabs tabs-style-underline" style="width: 100%;">
                <nav>
                    <ul>
                        <li>
                            <g:link controller="facilityCustomer">
                                <g:message code="customer.select.all"/>
                            </g:link>
                        </li>
                        <li>
                            <g:link action="index">
                                <g:message code="default.member.label.plural"/>
                            </g:link>
                        </li>
                        <li>
                            <g:link controller="facilityCustomerArchive" action="index">
                                <g:message code="default.archive.label"/>
                            </g:link>
                        </li>
                        <li class="active tab-current pull-right">
                            <g:link action="iOSyncStatusMembers">
                                <g:message code="facilityCustomer.iosync.label"/>
                            </g:link>
                        </li>
                        <g:if test="${facility.hasIdrottOnlineActivitySync()}">
                            <li class="pull-right">
                                <g:link action="iOSyncStatusActivities">
                                    <g:message code="facilityCustomer.iosync.activities.label"/>
                                </g:link>
                            </li>
                        </g:if>
                    </ul>
                </nav>
            </div>
        </div>

        <div class="panel-body">
            <div class="row vertical-padding30">
                <div class="col-sm-6 text-center text-success">
                    <h1>${validated?.size()} <i class="fas fa-thumbs-up"></i></h1>
                    <h3><g:message code="facilityCustomer.iosync.validated"/></h3>
                </div>
                <div class="col-sm-6 text-center text-danger">
                    <h1>${notValidated?.size()} <i class="fas fa-thumbs-down"></i></h1>
                    <h3><g:message code="facilityCustomer.iosync.notvalidated"/></h3>
                </div>
            </div>
        </div>


        <div class="col-sm-12">
            <h3><g:message code="facilityCustomer.iosync.notvalidated.label"/></h3>
        </div>
        
        <table class="table table-striped table-hover table-bordered">
            <tbody data-link="row" class="rowlink">
            <g:each in="${notValidated}">
                <tr>
                    <td width="30%">
                        <g:link controller="facilityCustomer" action="edit" id="${it.customerId}"
                                params="[returnUrl: createLink(controller: params.controller, action: params.action)]">
                            ${it.firstName} ${it.lastName}
                        </g:link>
                    </td>
                    <td width="70%">
                        <ul>
                            <g:each in="${it.errors.allErrors}" var="e">
                                <li>${message(message: e)}</li>
                            </g:each>
                        </ul>
                    </td>
                </tr>
            </g:each>
            </tbody>
            <g:if test="${!notValidated}">
                <tfoot>
                <tr>
                    <td colspan="5" class="vertical-padding20">
                        <span class="text-muted text-md"><g:message code="default.noElements"/></span>
                    </td>
                </tr>
                </tfoot>
            </g:if>
        </table>

    </div>
</div>
<r:script>
    $("[rel='tooltip']").tooltip();
</r:script>
</body>
</html>
