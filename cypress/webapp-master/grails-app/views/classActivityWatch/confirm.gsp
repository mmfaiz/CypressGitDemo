<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>

<div class="modal-dialog">
    <div class="modal-content">
        <g:form name="confirmForm" class="no-margin">
            <g:hiddenField name="activityId" value="${occasion.activity.id}"/>
            <g:hiddenField name="fromDateTime" value="${occasion.date.toString()} ${occasion.startTime.toString('HH:mm')}"/>

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title"><g:message code="activityQueueForm.modal.title"/></h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-sm-6">
                        <h1 class="h6">
                            <span class="fa-stack fa-lg text-danger">
                              <i class="fa fa-circle fa-stack-2x"></i>
                              <i class="fa fa-users fa-stack-1x fa-inverse"></i>
                            </span> ${occasion.activity.name}
                        </h1>
                    </div>
                    <div class="col-sm-6">
                        <h2 class="h6">
                            <span class="fa-stack fa-lg text-grey-light">
                              <i class="fa fa-circle fa-stack-2x"></i>
                              <i class="fa fa-map-marker fa-stack-1x fa-inverse"></i>
                            </span>
                            ${occasion.activity.facility.name}
                        </h2>
                    </div>
                </div>

                <hr/>

                <div class="row">
                    <div class="col-sm-6">
                        <h3 class="h6 weight400">
                            <span class="fa-stack text-grey-light">
                              <i class="fa fa-circle fa-stack-2x"></i>
                              <i class="fa fa-calendar fa-stack-1x fa-inverse"></i>
                          </span> ${occasion.date.toString("${message(code: 'date.format.dateOnly')}")}
                        </h3>
                    </div>
                    <div class="col-sm-6">
                        <h3 class="h6 weight400">
                            <span class="fa-stack text-grey-light">
                              <i class="fa fa-circle fa-stack-2x"></i>
                              <i class="fa fa-clock-o fa-stack-1x fa-inverse"></i>
                          </span> <span rel="tooltip" data-original-title="${occasion.lengthInMinutes()}${message(code: 'unit.min')}">${occasion.startTime.toString("HH:mm")}</span>
                        </h3>
                    </div>
                </div>

                <hr/>

                <div class="row">
                    <div class="col-sm-4 col-xs-4">
                        <p class="right-margin10 top-margin10"><g:message code="queueForm.notification.label"/></p>
                    </div>
                    <div class="col-sm-8 col-xs-8">
                        <div class="form-group">
                            <div class="checkbox">
                                <g:checkBox id="emailNotify" name="emailNotify" checked="true" disabled="true" />
                                <label for="emailNotify"><g:message code="queueForm.notification.email"/></label>
                            </div>
                            <div class="checkbox">
                                <g:checkBox id="smsNotify" name="smsNotify" disabled="${!user.telephone}"/>
                                <label for="smsNotify">
                                    <g:message code="queueForm.notification.sms"/>
                                    <sec:ifLoggedIn>
                                        <g:if test="${user.telephone}">
                                            ( ${user.telephone} )
                                        </g:if>
                                        <g:else>
                                            ( <g:link controller="userProfile" action="edit"><g:message code="queueForm.notification.sms.phonemissing"/></g:link> )
                                        </g:else>
                                    </sec:ifLoggedIn>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn btn-default" data-dismiss="modal" aria-hidden="true"><g:message code="button.cancel.label"/></button>
                <g:submitButton id="btnSubmit" name="confirm" class="btn btn-success"
                        value="${message(code: 'button.confirm.label')}"/>
            </div>
        </g:form>
    </div>
</div>
<r:script>
    $(document).ready(function() {
        // Disable enter on form (since we submit remote)
        $("#confirmForm").bind("keypress", function(e) {
            if (e.keyCode == 13) return false;
        });

        $("#confirmForm").submit(function() {
            $.ajax({
                type: "POST",
                data: $(this).serialize(),
                url: "${createLink(mapping: 'activityWatch', method: 'POST')}",
                success: function (data) {
                    $('#occasion-watch_${occasion.id}').hide();
                    $('#occasion-unwatch_${occasion.id}').show();
                    $('#occasion-unwatch_${occasion.id}').attr("data-watch-id", data.id);
                    $('#userBookingModal').modal('hide');
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    if (jqXHR.status == 401) {
                        location.href = "${createLink(controller: 'login', action: 'auth',
                                params: [returnUrl: createLink(controller: 'facility', action: 'show',
                                        params: [name: occasion.activity.facility.shortname])])}";
                    } else {
                        handleAjaxError(jqXHR, textStatus, errorThrown);
                    }
                }
            });
            return false;
        });
    });
</r:script>
</body>
</html>
