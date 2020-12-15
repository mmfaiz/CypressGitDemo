<div class="panel panel-info top-margin20">
    <div class="panel-heading bg-white vertical-padding10" data-toggle="collapse" href="#classActivityWatch" aria-expanded="false" aria-controls="classActivityWatch">
        <i class="fas fa-info-circle text-primary"></i> <g:message code="activityQueueForm.heading"/> <strong><a href="javascript:void(0)"><g:message code="queueForm.text2"/></a></strong>
    </div>
    <div class="panel-body collapse" id="classActivityWatch">
        <div class="row">
            <div class="col-sm-12">
                <span id="activityWatchLoader"><i class="fas fa-spinner fa-spin"></i> <g:message code="activityQueueForm.loader"/></span>
                <strong id="activityWatchLabel" style="display: none;"><i class="fas fa-hourglass-half"></i> <g:message code="activityQueueForm.waitingList"/>:</strong>
                <ul id="userClassActivityWatch" class="list-unstyled">
                </ul>
            </div>
        </div>

        <g:message code="activityQueueForm.text1"/><br><br>

        <form id="activityWatchForm" name="activityWatchForm" class="top-margin10">
            <ul class="list-inline">
                <li class="form-group col-sm-3 col-xs-6">
                    <label><g:message code="default.activity.label"/></label>
                    <g:select id="watchActivityId" name="activityId" class="form-control" from="${activities}" optionKey="id"
                              optionValue="name" disabled="${!user}"/>
                </li>
                <li class="form-group col-sm-3 col-xs-6">
                    <label><g:message code="facilityActivity.occasions.message6"/></label>
                    <g:select id="watchFromDateTime" name="fromDateTime" class="form-control" from="${[]}" disabled="${!user}"/>
                </li>
                <li class="form-group col-sm-6 col-xs-12">
                    <label><g:message code="queueForm.notification.label"/></label>
                    <div class="form-control no-border top-padding1">
                        <div class="checkbox">
                            <g:checkBox id="emailNotify" name="emailNotify" checked="true" disabled="true" />
                            <label for="emailNotify"><g:message code="queueForm.notification.email"/></label>
                        </div>
                        <div class="checkbox">
                            <g:checkBox id="smsNotify" name="smsNotify" disabled="${!user?.telephone}"/>
                            <label for="smsNotify">
                                <g:message code="queueForm.notification.sms"/>
                                <sec:ifLoggedIn>
                                    <g:if test="${user?.telephone}">
                                        ( ${user?.telephone} )
                                    </g:if>
                                    <g:else>
                                        ( <g:link controller="userProfile" action="edit"><g:message code="queueForm.notification.sms.phonemissing"/></g:link> )
                                    </g:else>
                                </sec:ifLoggedIn>
                            </label>
                        </div>
                    </div>
                </li>
            </ul>
            <div class="row">
                <div class="col-sm-12 col-xs-12 top-margin5">
                    <sec:ifLoggedIn>
                        <input type="submit" class="btn btn-primary col-sm-12 col-xs-12" value="${message(code: "queueForm.submit.label")}"/>
                    </sec:ifLoggedIn>
                    <sec:ifNotLoggedIn>
                        <input type="submit" class="btn btn-warning col-sm-12 col-xs-12" disabled value="${message(code: "form.show.loginRequired.tooltip")}"/>
                    </sec:ifNotLoggedIn>
                </div>
            </div>
        </form>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        var $userActivityWatch = $('#userClassActivityWatch');
        var $loader = $('#activityWatchLoader');
        var $label = $('#activityWatchLabel');
        var $form = $('#activityWatchForm');

        var fetchedActivityWatches = false;
        var activityEntryPrefix = 'activityWatch_';
        var activityEntryRemovePrefix = 'activityWatchRemove_';

        var dateTimeData = {};
        <g:each in="${activities}" var="activity">
            dateTimeData["${activity.id}"] = ["${raw(activity.occasions.join('", "'))}"];
        </g:each>

        $form.on('submit', function(e) {
            e.preventDefault();

            $.ajax({
                type:'POST',
                data: $form.serialize(),
                dataType: 'json',
                url: '${g.forJavaScript(data: createLink(mapping: 'activityWatch', method: 'POST'))}',
                success:function(result) {
                    if (!$userActivityWatch.find("#" + activityEntryPrefix + result.id).length) {
                        var html = '<li id="' + activityEntryPrefix + result.id + '" data-from="'+result.from+'">';
                        html += '<i class="fa fa-clock-o"></i> <em>' + result.from + '</em>';
                        html += ' - ' + result.activity.name;
                        html += '&nbsp;&nbsp;<a id="' + activityEntryRemovePrefix + result.id +'" class="text-danger" rel="tooltip" title="${message(code: "button.delete.label")}" href="javascript:void(0)"><i class="fa fa-remove"></i></a>';
                        html += '</li>';

                        hideLoader();
                        $label.show();
                        $userActivityWatch.append(html);
                    }
                }
            });
        });

        $('#classActivityWatch').on('show.bs.collapse', function () {
            if(!fetchedActivityWatches) {
                $.ajax({
                    type:'GET',
                    data: {facility: ${facility.id}},
                    dataType: 'json',
                    url: '${g.forJavaScript(data: createLink(mapping: 'activityWatch', method: 'GET'))}',
                    success:function(arr) {
                        var html = "";
                        if(arr.length > 0) {
                            $.each(arr, function (index, entry) {
                                html += '<li id="' + activityEntryPrefix + entry.id + '" class="vertical-padding2" data-from="'+entry.from+'">';
                                html += '<i class="fa fa-clock-o"></i> <em>' + entry.from + '</em>';
                                html += ' - ' + entry.activity.name;
                                html += '&nbsp;&nbsp;<a id="' + activityEntryRemovePrefix + entry.id +'" class="text-danger" rel="tooltip" title="${message(code: "button.delete.label")}" href="javascript:void(0)"><i class="fa fa-remove"></i></a>';
                                html += '</li>';
                            });

                            $label.show();
                            $userActivityWatch.append(html);
                        }
                        hideLoader();
                    }
                });
            }
        });

        $userActivityWatch.on('click', '[id^='+activityEntryRemovePrefix+']', function () {
            if(confirm("${message(code: 'default.confirm')}")) {
                var id = $(this).prop('id').substring(activityEntryRemovePrefix.length);
                removeActivityWatch(id);
            }
        });

        var removeActivityWatch = function (id) {
            var $watch = $('#' + activityEntryPrefix + id);

            $.ajax({
                type:'DELETE',
                dataType: 'json',
                url: '${g.forJavaScript(data: createLink(controller: 'classActivityWatch', method: 'DELETE'))}?id=' + id,
                success:function(data) {
                    $watch.remove();
                },
                error: function (error) {
                    $watch.remove();
                    console.log(error);
                }
            });
        };

        var hideLoader = function () {
            $loader.hide();
            fetchedActivityWatches = true;
        };

        $("#watchActivityId").on("change", function() {
            $("#watchFromDateTime").html("");
            for (var i = 0; i < dateTimeData[$(this).val()].length; i++) {
                var value = dateTimeData[$(this).val()][i];
                $("#watchFromDateTime").append('<option value="' + value + '">' + value + '</option>');
            }
        }).trigger("change");
    });
</script>