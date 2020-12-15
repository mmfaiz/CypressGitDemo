<r:require modules="tinySort"/>
<g:if test="${slotHours.size() > 0}">
    <div class="panel panel-info top-margin20">
        <div class="panel-heading bg-white vertical-padding10" data-toggle="collapse" href="#slotWatch_${facility.id}" aria-expanded="false" aria-controls="slotWatch_${facility.id}">
            <i class="fas fa-info-circle text-primary"></i> <g:message code="queueForm.text1"/> <strong><a href="javascript:void(0)"><g:message code="queueForm.text2"/></a></strong>
        </div>
        <div class="panel-body collapse" id="slotWatch_${facility.id}">
            <div class="row">
                <div class="col-sm-12">
                    <span id="slotWatchLoader"><i class="fas fa-spinner fa-spin"></i> <g:message code="queueForm.text3"/></span>
                    <strong id="slotWatchLabel" style="display: none;"><i class="fas fa-hourglass-half"></i> <g:message code="queueForm.text4"/></strong>
                    <ul id="userSlotWatch_${facility.id}" class="list-unstyled">
                    </ul>
                </div>
            </div>

            <g:message code="queueForm.text5"/><br><br>

            <g:message code="queueForm.text6"/> <strong><g:humanDateFormat date="${date}"/></strong>
            <form id="slotWatchForm_${facility.id}" name="slotWatchForm_${facility.id}" class="top-margin10">
                <g:hiddenField name="facilityId" class="form-control" value="${facility.id}"/>
                <g:hiddenField name="fromDate" class="form-control" value="${date}"/>
                <g:each in="${watchSports}">
                    <g:hiddenField name="sportIds" value="${it.id}"/>
                </g:each>
                <ul class="list-inline">
                    <li class="form-group col-xs-6">
                        <label><g:message code="default.court.label"/></label>
                        <g:select name="courtId" class="form-control" from="${courts}" optionKey="id"
                                  optionValue="name" noSelection="${['':"${message(code: "queueForm.allCourts")}"]}" disabled="${!user}"/>
                    </li>
                    <li class="form-group col-xs-6">
                        <label><g:message code="default.time.label"/></label>
                        <g:select name="fromTime" class="form-control" from="${slotHours}" disabled="${!user}"/>
                    </li>
                    <li class="col-xs-12">
                        <label><g:message code="queueForm.notification.label"/></label>
                        <div class="no-border top-padding1">
                            <div class="checkbox">
                                <g:checkBox id="emailNotify_${facility.id}" name="emailNotify" checked="true" disabled="true" />
                                <label for="emailNotify_${facility.id}" class="no-padding"><g:message code="queueForm.notification.email"/></label>
                            </div>
                            <div class="checkbox">
                                <g:checkBox id="smsNotify_${facility.id}" name="smsNotify" disabled="${!user?.telephone}"/>
                                <label for="smsNotify_${facility.id}" class="no-padding">
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
    <script>
        $(function () {
            var $userSlotWatch     = $('#userSlotWatch_${g.forJavaScript(data: facility.id)}');
            var $loader            = $('#slotWatchLoader');
            var $label             = $('#slotWatchLabel');

            var $form      = $('#slotWatchForm_${g.forJavaScript(data: facility.id)}');
            var $fromDate  = $form.find('#fromDate');
            var $fromTime  = $form.find('#fromTime');
            var $courtId   = $form.find('#courtId');
            var $smsNotify = $form.find('#smsNotify_${g.forJavaScript(data: facility.id)}');

            var fetchedSlotWatches = false;

            var slotEntryPrefix = 'slotWatch_';
            var slotEntryRemovePrefix = 'slotWatchRemove_';

            $fromTime.selectpicker({
                title: '<g:message code="facilitySubscription.index.courtSelect.noneSelectedText"/>'
            });
            $courtId.selectpicker({
                title: '<g:message code="facilitySubscription.index.courtSelect.noneSelectedText"/>'
            });

            $form.on('submit', function(e) {
                e.preventDefault();

                $.ajax({
                    type:'POST',
                    data: $form.serialize(),
                    dataType: 'json',
                    url: '${g.forJavaScript(data: createLink(controller: 'slotWatch', method: 'POST'))}',
                    success:function(result) {
                        var html = '<li id="' + slotEntryPrefix + result.id + '" data-from="'+result.from+'">';
                        html += '<i class="fa fa-clock-o"></i> <em>' + result.from + ' - ' + result.to + '</em>';
                        if(result.court) {
                            html += ' - ' + result.court.name;
                        } else {
                            html += ' - ' + "${message(code: "queueForm.allCourts")}";
                            if (result.sport) {
                                html += " (" + result.sport.name + ")"
                            }
                        }
                        if(result.sms) {
                            html += '&nbsp;&nbsp;<i class="fa fa-sms"></i>';
                        }

                        html += '&nbsp;&nbsp;<a id="' + slotEntryRemovePrefix + result.id +'" class="text-danger" rel="tooltip" title="${message(code: "button.delete.label")}" href="javascript:void(0)"><i class="fa fa-remove"></i></a>';
                        html += '</li>';

                        hideLoader();
                        $label.show();
                        $userSlotWatch.append(html);
                        sortSlotWatches();
                    },
                    error: function (error) {
                    }
                });
            });

            $('#slotWatch_${g.forJavaScript(data: facility.id)}').on('show.bs.collapse', function () {

                if(!fetchedSlotWatches) {
                    $.ajax({
                        type:'GET',
                        data: {
                            'myDate': "${g.forJavaScript(data: date)}",
                            'facility': "${g.forJavaScript(data: facility.id)}"
                        },
                        dataType: 'json',
                        url: '${g.forJavaScript(data: createLink(controller: 'slotWatch', method: 'GET'))}',
                        success:function(arr) {

                            var html = "";

                            if(arr.length > 0) {
                                $.each(arr, function (index, entry) {
                                    html += '<li id="' + slotEntryPrefix + entry.id + '" class="vertical-padding2" data-from="'+entry.from+'">';
                                    html += '<i class="fa fa-clock-o"></i> <em>' + entry.from + ' - ' + entry.to + '</em>';
                                    if(entry.court) {
                                        html += ' - ' + entry.court.name;
                                    } else {
                                        html += ' - ' + "${message(code: "queueForm.allCourts")}";
                                        if (entry.sport) {
                                            html += " (" + entry.sport.name + ")"
                                        }
                                    }
                                    if(entry.sms) {
                                        html += '&nbsp;&nbsp;<i class="fa fa-sms"></i>';
                                    }

                                    html += '&nbsp;&nbsp;<a id="' + slotEntryRemovePrefix + entry.id +'" class="text-danger" rel="tooltip" title="${message(code: "button.delete.label")}" href="javascript:void(0)"><i class="fa fa-remove"></i></a>';
                                    html += '</li>';
                                });

                                $label.show();
                                $userSlotWatch.append(html);
                            }

                            hideLoader();
                        },
                        error: function (error) {
                            console.log('Error');
                            console.log(error);
                        }
                    });
                }
            });
            
            $userSlotWatch.on('click', '[id^='+slotEntryRemovePrefix+']', function () {
                if(confirm("${message(code: 'default.confirm')}")) {
                    var id = $(this).prop('id').substring(slotEntryRemovePrefix.length);
                    removeSlotWatch(id);
                }
            });

            var removeSlotWatch = function (id) {
                var $slotWatch = $('#' + slotEntryPrefix + id);

                $.ajax({
                    type:'DELETE',
                    dataType: 'json',
                    url: '${g.forJavaScript(data: createLink(controller: 'slotWatch', method: 'DELETE'))}?id=' + id,
                    success:function(data) {
                        $slotWatch.remove();
                    },
                    error: function (error) {
                        $slotWatch.remove();
                        console.log(error);
                    }
                });
            };
            
            var sortSlotWatches = function () {
                var $slotEntries = "ul#userSlotWatch_${g.forJavaScript(data: facility.id)}>li";
                tinysort($slotEntries, { data:'from' });
            };

            var hideLoader = function () {
                $loader.hide();
                fetchedSlotWatches = true;
            };
        });
    </script>
</g:if>