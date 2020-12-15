<%@ page import="com.matchi.User; com.matchi.Role; org.joda.time.DateTime; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="adminUser.index.title"/></title>
    <r:require modules="matchi-selectpicker"/>
    <r:require modules="jquery-validate"/>
    <r:script>
        $(function() {
            $("#roles").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'adminUser.edit.roles')}",
                selectedTextFormat: 'count'
            });
        });
    </r:script>
</head>

<body>

<div class="container content-container">
    <ol class="breadcrumb">
        <li><g:message code="user.label.plural"/> (${numUsers} <g:message code="unit.st"/>)</li>
    </ol>

    <g:form method="get" name="query" class="form well no-bottom-padding">
        <div class="row">
            <div class="form-group col-sm-2 no-top-margin">
                <g:textField name="q" value="${filter?.q}" class="form-control"
                             placeholder="${message(code: 'adminUser.index.search.placeholder')}" minlength="1"/>
            </div>

            <div class="form-group col-sm-2 no-top-margin">
                <g:select from="${Role.listOrderByAuthority()}" name="roles" value="${filter?.roles}"
                          optionKey="id" optionValue="${{ message(code: 'auth.roles.' + it.authority) }}"
                          multiple="multiple" title="${message(code: 'adminUser.edit.roles')}"/>
            </div>

            <div class="form-group col-sm-2 no-top-margin pull-right">
                <button type="submit" name="submit" value="1" class="btn btn-block btn-info"><g:message
                        code="button.filter.label"/></button>
            </div>
        </div>
    </g:form>

    <div class="panel panel-default panel-admin">
        <div class="panel-heading table-header">
            <div class="row">
                <div class="col-sm-6">
                    &#160;
                </div>

                <div class="col-sm-6">
                </div>
            </div>
        </div>

        <table class="table table-striped table-hover table-bordered">
            <thead>
            <tr height="34">
                <th><g:message code="user.email.label"/></th>
                <th><g:message code="adminUser.index.city"/></th>
                <th><g:message code="facility.label"/></th>
                <th class="center-text"><g:message code="adminUser.index.registered"/></th>
                <th class="center-text"><g:message code="adminUser.index.activated"/></th>
                <th class="center-text"><g:message code="user.enabled.label"/></th>
                <th class="center-text"><g:message code="bookingGroup.name.BLOCKED"/></th>
                <th width="140" class="center-text"><g:message code="adminUser.index.lastLoggedIn"/></th>
            </tr>
            </thead>
            <tbody data-link="row" class="rowlink">
            <g:each in="${users}" var="user">
                <tr>
                    <td>
                        ${user.fullName()}<br><g:link action="edit" id="${user.id}">${user.email}</g:link></td>

                    <td>${user.city ?: '-'}</td>
                    <td>${user.facility ?: '-'}</td>

                    <td class="center-text">${user.dateCreated ? new DateTime(user.dateCreated).toString('yyyy-MM-dd') : '-'}</td>
                    <td class="center-text">${user.dateActivated ? new DateTime(user.dateActivated).toString('yyyy-MM-dd') : '-'}</td>

                    <td class="center-text">
                        <span class="label label-${user.enabled ? 'success' : 'danger'}">${user.enabled ? message(code: 'default.yes.label') : message(code: 'default.no.label')}</span>
                    </td>
                    <td class="center-text">
                        <span
                                class="label label-${!user.accountLocked ? 'success' : 'danger'}">${!user.accountLocked ? message(code: 'default.no.label') : message(code: 'default.yes.label')}</span>
                    </td>
                    <td class="center-text">${user.lastLoggedIn ? new DateTime(user.lastLoggedIn).toString('yyyy-MM-dd HH:mm') : '-'}</td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="row">
        <div class="col-sm-12 text-center bottom-margin20">
            <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                                          maxsteps="0" max="50" action="index" total="${numUsers}" params="${params}"/>

            <div class="col-sm-12 center-text">
                <span class="">
                    (<g:message code="adminUser.index.total"/> ${User.count}<g:message code="unit.st"/>)
                </span>
            </div>
        </div>
    </div>
</div>
<r:script>
    $(document).ready(function () {
        $("#q").focus();


        $.validator.addMethod('userQueryCheck', function (value, element, params) {
            var roles = $('select[name="roles"]').val();
            var q = $('input[name="q"]').val();
            return !(roles && roles.includes("2") && !q);
        }, "User query must be limited by query string");

        $("#query").validate({
            rules: {
                q: {
                    userQueryCheck: true
                }
            }
        });
    })
</r:script>
</body>
</html>
