<html>
<head>
    <meta name="layout" content="adminLayout"/>
    <title>Adyen notifications</title>
</head>
<body>
<ul class="breadcrumb">
    <li>Adyen notifications (${notifications?.getTotalCount()})</li>
</ul>

<div id="container">
    <div class="content">
        <ul id="errors-tab" class="nav nav-tabs">
            <li><g:link action="index">Orders</g:link></li>
            <li><g:link action="errors">Errors</g:link></li>
            <li class="active"><g:link action="notifications">Notifications</g:link></li>
        </ul>

        <g:form action="errors" name="filterForm" class="form-inline well" style="padding:12px 10px 4px 10px;">
            <fieldset>
                <div class="control-group">
                    <ul class="inline filter-list">
                        <li>
                            <g:textField tabindex="1" name="q" placeholder="Search" value="${params.q}"/>
                        </li>
                        <li class="pull-right">
                            <button tabindex="3" id="filterSubmit" class="btn" type="submit">
                                <g:message code="button.filter.label"/>
                            </button>
                        </li>
                    </ul>
                </div>
            </fieldset>
        </g:form>

        <div class="row">
            <div class="span12">
                <table class="table" style="border-collapse:collapse;">
                    <thead>
                    <tr height="34">
                        <th>Date created</th>
                        <th>EventCode</th>
                        <th>PspReference</th>
                        <th>Success</th>
                        <th>Reason</th>
                    </tr>
                    </thead>
                    <tbody data-provides="rowlink">
                    <g:each in="${notifications}" var="notification">
                        <tr>
                            <td>
                                <g:link action="index" params="[q: notification.pspReference]" class="rowlink">
                                        <g:formatDate date="${notification.dateCreated}" format="yyyy-MM-dd HH:mm"/>
                                </g:link>
                            </td>
                            <td>${notification.eventCode}</td>
                            <td>${notification.pspReference}</td>
                            <td>
                                <g:if test="${notification.success}">
                                    <span class="badge badge-success">
                                        True
                                    </span>
                                </g:if>
                                <g:else>
                                    <span class="badge badge-danger">
                                        False
                                    </span>
                                </g:else>
                            </td>
                            <td>${notification.reason}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>

            </div>
        </div>
        <div class="row">
            <div class="span12">
                <g:paginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                                            maxsteps="0" max="${cmd.max}" action="index" total="${notifications?.getTotalCount()}"
                                            params="[q: params.q]"/>
            </div>
        </div>
        <div class="row">
            <div class="span12 center-text">
                <span class="">
                    ${notifications?.getTotalCount()}<g:message code="unit.st"/>
                </span>
            </div>
        </div>
    </div>
</div>

<r:script>
    $(document).ready(function() {
        $("#q").focus();

        $("[rel=tooltip]").tooltip();
    })
</r:script>
</body>
</html>
