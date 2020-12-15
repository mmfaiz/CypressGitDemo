<%@ page import="com.matchi.activities.Participant" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <g:set var="entityName" value="${message(code: 'course.label', default: 'Kurs')}"/>
    <title><g:message code="course.planning.simple.label"/></title>
    <r:require modules="jquery-sortable,datejs,matchi-selectpicker,jquery-multiselect-widget,bootstrap-timepicker,pick-a-color,jquery-fastLiveFilter,bootstrap3-wysiwyg"/>
    <r:script>
        $(function() {
            $('#seasonIds').allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.seasons.selectedText')}",
                selectedTextFormat: 'count'
            });
            $("#courseIds").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.courses.selectedText')}",
                selectedTextFormat: 'count'
            });
            $("#trainerIds").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.trainers.selectedText')}",
                selectedTextFormat: 'count'
            });

            // Participant filters
            $("#courses").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.courses.selectedText')}",
                selectedTextFormat: 'count'
            });
            $("#statuses").allselectpicker({
                selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
                countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.statuses.selectedText')}",
                selectedTextFormat: 'count'
            });

            var getParticipants = function(/*Obj*/ filter) {
                var $participantList       = $("#sortableParticipants");
                var $participantListLoader = $("#sortableParticipantsLoader");

                $participantList.hide();
                $participantListLoader.show();

                $.post("${g.forJavaScript(data: createLink(action: 'filterParticipants'))}", filter)
                    .success(function(data){
                        $("#sortableParticipants").find("ol").html(data);
                        $('input#q').fastLiveFilter('.list-participants');

                        $participantListLoader.hide();
                        $participantList.show();
                    })
                    .error(function() {
                        $participantListLoader.hide();
                    });

            };

            $("#filterParticipants").click(function() {
                var courseIds = $("#courses").val();
                var statuses  = $("#statuses").val();
                getParticipants({courses: courseIds, statuses: statuses, max: 1000000000});
            });

            getParticipants({courses: "${g.forJavaScript(data: filter.courseIds)}", statuses: [], max: 1000000000});
        });
    </r:script>
</head>

<body>
<g:if test="${!facility?.isMasterFacility()}">
    <aside id="sidebar" class="col-sm-3 no-padding panel panel-default panel-admin">
        <div class="sidebar-wrap">
            <!-- LIST COURSES -->
            <div class="panel no-border no-box-shadow">
                <div class="panel-heading bg-grey-light vertical-padding10">
                    <h3 class="panel-title toggle-collapse-folder">
                        <a data-toggle="collapse" href="#list-courses" aria-expanded="true" class="collapsed" aria-controls="list-courses">
                            <g:message code="course.label.plural"/> (${activeCourses?.size()}<g:message code="unit.st"/>)
                        </a>
                    </h3>
                </div>
                <div id="list-courses" class="panel-collapse collapse">
                    <ul class="list-group">
                        <g:each in="${activeCourses}">
                            <li id="${it.id}" class="list-group-item no-horizontal-padding">
                                <span class="badge badge-pill badge-warning" rel="tooltip" data-placement="top" title="<g:message code="facilityCourse.planning.applications"/>">${it.form.submissions?.findAll{ it?.status == it?.status?.WAITING }?.size()}</span><span class="badge badge-pill badge-success" rel="tooltip" data-placement="top" title="<g:message code="facilityCourse.planning.participants"/>">${it.participants?.size()}</span>
                                <!-- <span class="course-color-indicator course-hint-${it.hintColor}"></span> -->
                                <input type="text" value="${it.hintColor}" name="hint-color" class="pick-a-color form-control inline">
                                <span class="text-sm">${it.name}</span>
                            </li>
                        </g:each>
                    </ul>
                </div><!-- /#list-courses -->
            </div><!-- /.panel -->

        <!-- LIST TRAINERS -->
            <div class="panel no-border no-box-shadow">
                <div class="panel-heading bg-grey-light vertical-padding10">
                    <h3 class="panel-title toggle-collapse-folder">
                        <a data-toggle="collapse" href="#list-trainers" aria-expanded="true" class="collapsed" aria-controls="list-courses">
                            <g:message code="default.trainer.plural"/> (${trainers?.size()}<g:message code="unit.st"/>)
                        </a>
                    </h3>
                </div>
                <div id="list-trainers" class="panel-collapse collapse">
                    <ul class="list-group droppable-item source-list">
                        <g:each in="${trainers}">
                            <li data-trainer-id="${it.id}" class="list-group-item no-horizontal-padding draggable-item">
                                <div class="media">
                                    <div class="media-left right-padding10">
                                        <div class="avatar-circle-xxs">
                                            <img class="img-responsive" src="${it?.profileImage?.thumbnailAbsoluteURL ?: resource(dir: 'images', file: 'avatar_default.png')}"/>
                                        </div>
                                    </div>
                                    <div class="media-body full-width weight400">
                                        <g:link controller="trainer" action="edit" id="${it.id}" class="draggable-handle text-sm ellipsis">
                                            ${it.toString().encodeAsHTML()}
                                        </g:link>
                                    </div>
                                </div>
                            </li>
                        </g:each>
                    </ul>
                </div>
            </div>

            <!-- LIST PARTICIPANTS -->
            <div class="panel no-border no-box-shadow">
                <div class="panel-heading bg-grey-light vertical-padding10">
                    <h3 class="panel-title toggle-collapse-folder">
                        <a data-toggle="collapse" href="#list-course-participants" aria-expanded="true" aria-controls="list-course-participants">
                            <g:message code="courseParticipantAndSubmission.label.plural"/>
                        </a>
                    </h3>
                </div>
                <div id="list-course-participants" class="panel-collapse collapse in">
                    <div class="panel-body no-padding">
                        <!-- FILTERS -->
                        <div class="row padding10">
                            <div class="col-xs-12 no-margin">
                                <g:select from="${activeCourses}" name="courses" value="${filter?.courseIds}"
                                          optionKey="id" optionValue="name" multiple="multiple" data-style="btn-default btn-xs"
                                          title="${message(code: 'facilityCourse.planning.selectCourse')}"/>
                            </div>
                            <div class="col-xs-12 no-margin">
                                <g:select from="${Participant.Status.listUsed()}" name="statuses" value=""
                                          valueMessagePrefix="courseParticipant.status"  multiple="multiple" data-style="btn-default btn-xs"
                                          title="${message(code: 'facilityCourseParticipant.index.statuses.noneSelectedText')}"/>
                            </div>
                            <div class="col-xs-12 no-margin">
                                <button id="filterParticipants" class="btn btn-block btn-xs btn-info"><g:message code="button.filter.label"/></button>
                            </div>
                        </div>

                        <div id="sortableParticipants" style="display: none;">
                            <div class="row padding5">
                                <div class="col-xs-12">
                                    <g:textField name="q" value="" class="col-xs-12" placeholder="${message(code: 'facilityCourse.planning.message3')}" autocomplete="off"/>
                                </div>
                            </div>
                            <ol id="sortable" class="list-participants horizontal-padding15 droppable-item source-list">
                                <g:render template="participants" model="[participants: participants]"/>
                            </ol>
                        </div>

                        <div id="sortableParticipantsLoader">
                            <div class="row padding5">
                                <div class="col-xs-12 text-center">
                                    <p class="lead"><i class="fas fa-spinner fa-spin"></i></p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div><!-- /.panel-body -->
            </div><!-- /.panel -->
        </div><!-- /.sidebar-wrap -->
    </aside><!-- /#sidebar -->

    <div id="main-content" class="col-sm-9 vertical-padding20">
        <!--<ol class="breadcrumb">
            <li><i class=" ti-write"></i><g:link action="index"><g:message code="course.label.plural"/></g:link></li>
            <li class="active">Planera</li>
        </ol>-->

        <form method="GET" class="form well">
            <div class="row">
                <div class="form-group col-sm-3 no-margin">
                    <g:select from="${facility.seasons}" name="seasonIds" value="${filter.seasonIds}"
                              optionKey="id" optionValue="name" multiple="multiple"
                              title="${message(code: 'season.multiselect.noneSelectedText')}"/>
                </div>
                <div class="form-group col-sm-3 no-margin">
                    <g:select from="${activeCourses}" name="courseIds" value="${filter?.courseIds}"
                              optionKey="id" optionValue="name" multiple="multiple"
                              title="${message(code: 'facilityCourse.planning.selectCourse')}"/>
                </div>
                <div class="form-group col-sm-3 no-margin">
                    <g:select from="${trainers}" name="trainerIds" optionKey="id"
                              value="${filter?.trainerIds}" multiple="multiple"
                              title="${message(code: 'facilityCourseParticipant.index.trainers.noneSelectedText')}"/>
                </div>
                <div class="form-group col-sm-2 no-margin">
                    <button type="submit" class="btn btn-block btn-info"><g:message code="button.filter.label"/></button>
                </div>
            </div>
        </form>

        <div class="well well-sm text-right">
            <div class="btn-group">
                <button class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    <i class="fas fa-print"></i>  <g:message code="dropdown.print.label"/> <span class="caret"></span>
                </button>
                <ul class="dropdown-menu dropdown-menu-right">
                    <g:each in="[false, true]">
                        <li>
                            <g:link action="printOccasions" params="[seasonIds:filter.seasonIds, courseIds:filter.courseIds, trainerIds:filter.trainerIds, view: it]" target="_blank">
                                <g:message code="${it ? 'button.print.view.label' : 'button.print.list.label'}"/>
                            </g:link>
                        </li>
                    </g:each>
                </ul>
            </div>
        </div>

        <g:each in="${1..7}" var="day">
            <g:set var="expandedByDefault" value="${occasions.find {it.value.find { o -> o.day() == day}}}"/>
            <div class="panel panel-day">
                <div class="panel-heading panel-day-heading">
                    <button class="btn btn-white btn-xs pull-right" onclick="addOccasionModal('${day}')" style="margin-top:-5px;">
                        <i class="ti ti-plus"></i> <g:message code="button.add.label"/>
                    </button>
                    <h6 class="panel-title toggle-collapse-chevron">
                        <a data-toggle="collapse" href="#weekday_${day}" aria-controls="#weekday_${day}"
                                aria-expanded="${expandedByDefault ? 'true' : 'false'}"
                                class="${expandedByDefault ? '' : 'collapsed'}">
                            <g:message code="time.weekDay.plural.${day}"/>
                        </a>
                    </h6>
                </div>

                <div id="weekday_${day}" class="panel-court panel-collapse collapse ${expandedByDefault ? 'in' : ''}">
                    <div class="panel-body no-padding">
                        <g:if test="${occasions.size() > 0}">
                            <g:each in="${0..24}" var="hour">
                                <g:render template="/templates/trainingPlanner/occasions" model="[occasions: occasions[hour]?.findAll { it.day() == day }, hour: hour]"/>
                            </g:each>
                        </g:if>
                    </div>
                </div>
            </div>
        </g:each>
    </div>

    <!-- Occasion Modal -->
    <div class="modal fade" id="addOccasionModal" tabindex="-1" role="dialog" aria-labelledby="addOccasionModal" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title" id="addOccasionModalLabel"><g:message code="course.planning.occasion.add"/></h4>
                </div>
                <g:formRemote name="addOccasion" class="form" role="form" url="[action: 'saveOccasion']" method="POST" onSuccess="refreshOccasions(data)">
                    <g:hiddenField name="occasionId" value=""/>
                    <div class="modal-body">
                        <div class="row">
                            <div class="form-group col-sm-6">
                                <label><g:message code="course.label"/></label>
                                <g:select from="${activeCourses}" name="course" optionKey="id" optionValue="name"/>
                                <g:hiddenField name="activeCourse" value=""/>
                            </div>
                            <div class="form-group col-sm-6">
                                <label><g:message code="trainer.label"/></label><br>
                                <g:select from="${trainers}" name="trainers" class="form-control" multiple="multiple" optionKey="id" title="${message(code: 'facilityCourseParticipant.index.trainers.noneSelectedText')}"/>
                            </div>
                        </div>
                        <div class="row">
                            <div class="form-group col-sm-6">
                                <label><g:message code="facilityCourse.planning.message6"/></label>
                                <div class="input-group bootstrap-timepicker">
                                    <input type="text" class="form-control" id="startTimePicker" value="18:00"/>
                                    <div class="input-group-addon"><i class="ti-time"></i></div>
                                    <g:hiddenField name="startTime" />
                                </div>
                            </div>
                            <div class="form-group col-sm-6">
                                <label><g:message code="facilityCourse.planning.message7"/></label>
                                <div class="input-group bootstrap-timepicker">
                                    <input type="text" class="form-control" id="endTimePicker" value="19:00"/>
                                    <div class="input-group-addon"><i class="ti-time"></i></div>
                                    <g:hiddenField name="endTime" />
                                </div>

                            </div>
                        </div>
                        <div class="row">
                            <div class="form-group col-sm-6">
                                <label><g:message code="court.label"/></label>
                                <g:select from="${com.matchi.activities.trainingplanner.TrainingCourt.findAllByFacility(facility)}" name="courtId" optionKey="id" optionValue="name"/>
                            </div>
                            <div class="form-group col-sm-6">
                                <label><g:message code="facilityCourse.planning.dayOfWeek"/></label>
                                <g:select from="${1..7}" name="dayOfWeek" valueMessagePrefix="time.weekDay"/>
                            </div>
                        </div>
                        <div class="row">
                            <div class="form-group col-sm-12">
                                <label><g:message code="activityOccasion.message.label"/></label>
                                <g:textArea name="message" maxlength="255" class="form-control"/>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-md btn-default" data-dismiss="modal"><g:message code="button.cancel.label"/></button>
                        <button type="button" class="btn btn-md btn-danger left" data-dismiss="modal" id="remove"><g:message code="button.delete.label"/></button>
                        <g:submitButton name="submit" class="btn btn-success" value="${message(code: "button.save.label")}"/>
                    </div>
                </g:formRemote>
            </div>
        </div>
    </div>

    <!-- Court occasion grid template -->
    <div id="hour-grid-template" class="panel panel-default no-bottom-margin panel-court" style="display: none;">
        <div class="panel-header panel-heading court-heading">
            <h4 class="no-margin"></h4>
        </div>
        <div class="occasion-grid ui-droppable">

        </div>
    </div>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
<r:script>
    var $addOccasionModal;
    var $sourceContainer;
    var $sourceItem;

    var sortableOptions = {
        group: '.droppable-item',
        pullPlaceholder: true,
        handle: '.draggable-item',
        itemSelector: "li.draggable-item",
        exclude: '.none-draggable-item',
        // animation on drop
        onDrop: function  (item, targetContainer, _super) {
            var itemId = item.attr("id");
            var trainerId = item.attr("data-trainer-id");

            var sourceCourseId = $sourceContainer ? getCourseId($sourceContainer) : item.attr("rel");
            var targetCourseId = getCourseId(targetContainer.el);

            var sourceItemIdString = $sourceItem.prop("id");
            var objectType, objectId;

            if(sourceItemIdString.split('-').length === 2) {
                objectType = sourceItemIdString.split('-')[0];
                objectId = sourceItemIdString.split('-')[1];
            } else {
                objectType = 'participant';
                objectId = sourceItemIdString.split('-')[0];
            }

            var addFunction = objectType === 'submission' ? addParticipantFromSubmission : addParticipant

            if (trainerId) {
                for (var i = 0; i < targetContainer.items.length; i++) {
                    if ($(targetContainer.items[i]).attr("data-trainer-id") == trainerId) {
                        item.remove();
                        _super(item);
                        return;
                    }
                }
                item.removeClass("no-horizontal-padding").addClass("text-xs");
                var itemContent = item.find(".media");
                itemContent.replaceWith("<strong>" + itemContent.text() + "</strong>");
            } else {

                if (targetCourseId && sourceCourseId != targetCourseId) {
                    if (!confirm("${message(code: 'facilityCourse.planning.participant.error')}")) {
                        item.remove();
                        _super(item);
                        if ($sourceContainer.length) {
                            copyParticipant(objectId, targetCourseId, function(participantId) {
                                addFunction($sourceContainer.prop("id"), participantId, function () {
                                    $sourceContainer.append($sourceItem);
                                    item.attr('id', participantId);
                                });
                            });
                        }
                        return;
                    }
                }

                for (var i = 0; i < targetContainer.items.length; i++) {
                    if ($(targetContainer.items[i]).attr("id") == itemId) {
                        item.remove();
                        _super(item);
                        return;
                    }
                }
                item.find(".media-body a").addClass("pull-left").css("width", "85%");
                item.find(".media-body").find("a.submission").hide();
                item.find(".media-body > div").show();
            }

            var clonedItem = $('<li/>').css({height: 0});
            item.before(clonedItem);

            clonedItem.detach();
            _super(item);

            if(!targetContainer.el.hasClass("source-list")) {
                var trainersEls = targetContainer.el.find("li[data-trainer-id]");
                trainersEls.detach().prependTo(targetContainer.el);
                if (trainerId) {
                    $.post("${g.forJavaScript(data: createLink(action: 'addOccasionTrainer'))}?occasionId="
                                + targetContainer.el.prop("id") + "&trainerId=" + trainerId);
                    targetContainer.el.parent().find("input[name=trainerIds]").val(
                            trainersEls.map(function() {return $(this).attr("data-trainer-id")}).get().join());
                } else {

                    if(targetCourseId && sourceCourseId != targetCourseId) {

                        if(objectType === 'participant') {
                            copyParticipant(objectId, targetCourseId, function (participantId) {
                                addParticipant(targetContainer.el.prop("id"), participantId, function () {
                                    item.attr('id', participantId);

                                    var $participantsEls = targetContainer.el.find("li").not("[data-trainer-id]");
                                    targetContainer.el.parent().find("input[name=participantCustomerIds]").val(
                                            $participantsEls.map(function() {return $(this).attr("customerId")}).get().join());
                                });

                            }.bind(this));
                        } else {
                            addParticipantFromSubmission(targetContainer.el.prop("id"), objectId, function (participantId) {
                                var $participantsEls = targetContainer.el.find("li").not("[data-trainer-id]");
                                targetContainer.el.parent().find("input[name=participantCustomerIds]").val(
                                        $participantsEls.map(function() {return $(this).attr("customerId")}).get().join());
                                item.attr('id', participantId);
                            });

                        }

                    } else {
                        addFunction(targetContainer.el.prop("id"), objectId, function (participantId) {
                            var $participantsEls = targetContainer.el.find("li").not("[data-trainer-id]");
                            targetContainer.el.parent().find("input[name=participantCustomerIds]").val(
                                    $participantsEls.map(function() {return $(this).attr("customerId")}).get().join());

                            item.attr('id', participantId);
                        });
                    }

                }
            }

            $('.occasion-grid').trigger("ss-rearrange");

            $('input#q').unbind('change');
            $('.list-participants').find('li').show();
            $('input#q').fastLiveFilter('.list-participants').trigger('change');
        },

        // set item relative to cursor position
        onDragStart: function ($item, container, _super) {
            $sourceContainer = container.el.hasClass("list-matches") ? container.el : null;
            $sourceItem = $item;

            var offset  = $item.offset(),
                pointer = container.rootGroup.pointer;

            adjustment = {
                left: pointer.left - offset.left,
                top: pointer.top - offset.top
            };

            if (container.el.hasClass("source-list")) {
                $item.clone().insertAfter($item);
            } else {
                var trainerId = $item.attr("data-trainer-id");
                if (trainerId) {
                    $.post("${g.forJavaScript(data: createLink(action: 'removeOccasionTrainer'))}?occasionId="
                                + container.el.prop("id") + "&trainerId=" + trainerId);
                    var trainersEls = container.el.find("li[data-trainer-id!=" + trainerId +"]");
                    container.el.parent().find("input[name=trainerIds]").val(
                            trainersEls.map(function() {return $(this).attr("data-trainer-id")}).get().join());
                } else {
                    removeParticipant(container.el.prop("id"), $item.prop("id"));
                    var participantId = $item.attr("id");
                    var $participantsEls = container.el.find("li").not("[data-trainer-id]").not("[id=" + participantId + "]");
                    container.el.parent().find("input[name=participantCustomerIds]").val(
                            $participantsEls.map(function() {return $(this).attr("customerId")}).get().join());
                }
            }

            _super($item, container);
        },
        onDrag: function ($item, position) {
            $item.css({
                left: position.left - adjustment.left,
                top: position.top - adjustment.top
            })
        }
    };

    $(document).ready(function() {
        $('input#q').fastLiveFilter('.list-participants');

        $addOccasionModal = $('#addOccasionModal');

        $('.droppable-item').sortable(sortableOptions);

        initPickers($addOccasionModal);

        $addOccasionModal.on('hide.bs.modal', function(event){
            if (event.target.id == 'addOccasionModal') {
                var $formAddOccasion =  $("form#addOccasion");
                $formAddOccasion.find("#occasionId").val("");
                $formAddOccasion.find('#course').selectpicker('val', "");
                $formAddOccasion.find('#course').removeAttr('disabled');
                $formAddOccasion.find('#course').selectpicker('refresh');
                $formAddOccasion.find('#startTime').selectpicker('val', "");
                $formAddOccasion.find('#startTime').selectpicker('refresh');
                $formAddOccasion.find('#endTime').selectpicker('val', "");
                $formAddOccasion.find('#endTime').selectpicker('refresh');
                $formAddOccasion.find('#courtId').selectpicker('val', "");
                $formAddOccasion.find('#courtId').selectpicker('refresh');
                $formAddOccasion.find('#dayOfWeek').selectpicker('val', "");
                $formAddOccasion.find('#dayOfWeek').selectpicker('refresh');
                $formAddOccasion.find('#trainers').selectpicker('val', "");
                $formAddOccasion.find('#trainers').selectpicker('refresh');
            }
        });

        $(".panel-occasion").css("position", "");

        // Color Picker
        // http://lauren.github.io/pick-a-color/

        var basicColors = {
            YELLOW: 'eaf725',
            ORANGE: 'f39c12',
            RED: 'ff3333',
            PURPLE: 'aa0077',
            BLUE: '19b5fe',
            GREEN: '85b20b',
            BLACK: '000000',
            BROWN: 'a52a2a',
            PINK: 'ffc0cb',
            GREY: 'bfbfbf'};

		$(".pick-a-color").pickAColor({
            showSpectrum: false,
            showSavedColors: false,
            saveColorsPerElement: false,
            fadeMenuToggle: true,
            showAdvanced: false,
            showBasicColors: false,
            showHexInput: false,
            allowBlank: false,
            inlineDropdown: true,
            basicColors: basicColors
		}).on("change", function () {
		    var self = $(this);
            var courseId  = self.closest('li').prop('id');
            var hintColor = _.invert(basicColors)[self.val()];

            updateCourseHintColor(courseId, hintColor);
		});

        $addOccasionModal.find('#message').wysihtml5({
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });

        $("#main-content").popover({
            selector: ".occasion-message",
            html: true,
            placement: "top",
            trigger: "hover",
            template: '<div class="popover" role="tooltip"><div class="arrow"></div><div class="popover-content" style="color: #2c2c2c"></div></div>'
        });

        $('[rel=tooltip]').tooltip({container: 'body'});

        $("#filterParticipants").click();

        $(document).ajaxError(function(event, jqxhr, settings, thrownError) {
            if (!jqxhr || !jqxhr.readyState) {
                alert("${message(code: 'facilityCourse.planning.offline')}");
            } else {
                alert("${message(code: 'facilityCourse.planning.error')}");
            }
        });
    });

    var initPickers = function($obj) {
        var defaultStartTime = '18:00';
        var defaultEndTime   = '19:00';

        // init occasion modal selects
        $obj.find('#startTimePicker').timepicker({
            minuteStep: 5,
            showSeconds: false,
            showMeridian: false,
            defaultTime: defaultStartTime
        }).on('changeTime.timepicker', function(e) {
            $obj.find('#startTime').val(e.time.value);
        });
        $obj.find('#startTime').val(defaultStartTime);

        $obj.find('#endTimePicker').timepicker({
            minuteStep: 5,
            showSeconds: false,
            showMeridian: false,
            defaultTime: defaultEndTime
        }).on('changeTime.timepicker', function(e) {
            $obj.find('#endTime').val(e.time.value);
        });
        $obj.find('#endTime').val(defaultEndTime);
        $obj.find("#course").selectpicker();
        $obj.find("#courtId").selectpicker();
        $obj.find("#dayOfWeek").selectpicker();
        $obj.find("#trainers").selectpicker({
            selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
            countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.trainers.selectedText')}",
            selectedTextFormat: 'count'
        });
    };

    var addOccasionModal = function(weekDay) {
        var $modal = $addOccasionModal;
        $modal.find(".modal-title").text("<g:message code="course.planning.occasion.add"/>");
        $("form#addOccasion").trigger("reset");
        $modal.modal({show: true, dynamic: true});

        $modal.find('#dayOfWeek').selectpicker('val', weekDay);
        $modal.find('#dayOfWeek').selectpicker('refresh');

        initPickers($modal);
    };

    var editOccasionModal = function(occasionId) {
        var $modal = $addOccasionModal;
        $modal.find(".modal-title").text("<g:message code="course.planning.occasion.edit"/>");
        var $modalForm = $("form#addOccasion");
        $modalForm.trigger("reset");

        var $occasion               = $("#"+occasionId+"_occasion");
        var $startTime              = $occasion.find("#startTime");
        var $endTime                = $occasion.find("#endTime");
        var $courseId               = $occasion.find("#courseId");
        var $trainerIds             = $occasion.find("#trainerIds");
        var participantCustomerIds  = $occasion.find("#participantCustomerIds");
        var $courtId                = $occasion.find("#court");
        var $date                   = $occasion.find("#occasionDate");
        var $msg                    = $occasion.find("#message");

        $modalForm.find('#course').selectpicker('val', $courseId.val());
        $modalForm.find('#course').selectpicker('refresh');
        $modalForm.find('#startTimePicker').timepicker('setTime', $startTime.val());
        $modalForm.find('#startTime').val($startTime.val());
        $modalForm.find('#endTimePicker').timepicker('setTime', $endTime.val());
        $modalForm.find('#endTime').val($endTime.val());
        $modalForm.find('#courtId').selectpicker('val', $courtId.val());
        $modalForm.find('#courtId').selectpicker('refresh');
        $modalForm.find('#dayOfWeek').selectpicker('val', Date.parse($date.val()).getDay() || 7);
        $modalForm.find('#dayOfWeek').selectpicker('refresh');
        $modalForm.find('#occasionId').val(occasionId);
        $modalForm.find('#message').val($msg.val());

        var trainersIds = [];
        $modalForm.find('#trainers option:selected').removeAttr("selected");
        $.each($trainerIds.val().split(','), function(index, value) {
            if (value) {
                $modalForm.find('[name=trainers] option[value=' + value + ']').attr('selected', true);
                trainersIds.push(value)
            }
        });
        $modalForm.find('#trainers').selectpicker('val',trainersIds);
        $modalForm.find('#trainers').selectpicker('refresh');

        switchCourseState($modalForm, $trainerIds, participantCustomerIds);

        $modal.modal({show: true, dynamic: true});
    };

    var notifyParticipants = function(occasionId, messageType) {
        var smsLink         = "${createLink(controller: 'facilityCustomerSMSMessage', action: 'message')}";
        var emailLink       = "${createLink(controller: 'facilityCustomerMessage', action: 'message')}";
        var $occasion       = $("#"+occasionId+"_occasion");
        var participantCustomerIds = $occasion.find("#participantCustomerIds").val();
        var returnUrl = $occasion.find("#returnUrl").val();
        var customerIdsString = "";
        if (participantCustomerIds) {
            participantCustomerIds.split(",").forEach(function(element) {
              customerIdsString += "&customerId=" + element;
            });
        }

        location.href = (messageType === 'sms' ? smsLink : emailLink) + "?originTitle=course.planning.label"+customerIdsString+"&returnUrl="+encodeURI(returnUrl);
    };

    $("#remove").click(function() {
        var courseId = $("#course").val();
        var occasionId = $("#occasionId").val();
        $.ajax({
            method: "DELETE",
            cache: false,
            url: "${g.forJavaScript(data: createLink(action: 'removeOccasion'))}?id="+courseId+"&occasionId="+occasionId,
            success: function () {
                removeOldOccasionPlace(occasionId);
            },
            error: function() {

            }
        });
    });

    var refreshOccasions = function(occasion) {
        $occasion = $(occasion);

        var occasionDayVal   = $occasion.find("#weekDay").val();
        var occasionCourtVal = $occasion.find("#court").val();
        var oldOccasionId    = $occasion.find("ol").attr('id');
        var occasionHourVal  = $occasion.find("#startHour").val();

        removeOldOccasionPlace(oldOccasionId);
        var $occasionDay     = $("#weekday_"+occasionDayVal);
        var $occasionCourt   = $occasionDay.find("#"+occasionCourtVal+"_court");
        var $occasionHour    = $occasionDay.find("#"+occasionHourVal+"_hour");
        var $occasionGrid    = $occasionHour.find(".occasion-grid");


        if($occasionGrid[0]) {
            $occasionGrid.append(occasion);
        } else {
            var newHour  = $(occasion).find("#startHour").val();

            var $newHourGrid = $("#hour-grid-template").clone();
            $newHourGrid.attr("id", newHour + "_hour");
            $newHourGrid.find(".panel-heading h4").text("${message(code: "facilityCourse.planning.message8")} " + newHour);

            $newHourGrid.find(".occasion-grid").append(occasion);
            $occasionDay.find(".panel-body").append($newHourGrid);
            $newHourGrid.show();
        }

        // Handle add occasion modal (hide and reset)
        $addOccasionModal.modal("hide");

        $droppableItem = $('.droppable-item');

        $droppableItem.sortable(sortableOptions);
        $droppableItem.sortable("refresh");

        if (!$occasionDay.hasClass("in")) {
            $occasionDay.collapse("show");
        }
    };

    var addParticipant = function(occasionId, participantId, callback) {
        $.ajax({
            method: "POST",
            cache: false,
            url: "${g.forJavaScript(data: createLink(action: 'addOccasionParticipation'))}?occasionId="+occasionId+"&participantId="+participantId,
            success: function () {
                try {
                    updateParticipantNrOccasionsColor(participantId, true);
                } catch (e) {

                }

                callback(participantId);
            },
            error: function() {

            }
        });
    };

    var addParticipantFromSubmission = function(occasionId, submissionId, callback) {
        $.ajax({
            method: "POST",
            cache: false,
            url: "${g.forJavaScript(data: createLink(action: 'addOccasionParticipationFromSubmission'))}?occasionId="+occasionId+"&submissionId="+submissionId,
            success: function (res) {
                var participantId = res.id

                $('#submission-' + submissionId).attr('id', 'participant-' + participantId)
                                                .removeClass('course-' + res.oldHintColor).addClass('course-' + res.hintColor);
                $('#submission-' + submissionId + '_nrOccasions').attr('id', 'participant-' + participantId + '_nrOccasions');

                try {
                    updateParticipantNrOccasionsColor(participantId, true);
                } catch (e) {

                }

                callback(participantId);
            },
            error: function(err) {
                if(err.status === 401) {
                    alert('<g:message code="form.maxSubmissions.error"/>');
                }
            }
        });
    };

    var copyParticipant = function(participantId, courseId, callback) {
        $.ajax({
            method: "POST",
            cache: false,
            url: "${g.forJavaScript(data: createLink(action: 'addParticipant'))}?courseId="+courseId+"&participantId="+participantId,
            success: function (res) {
                var newId = res.id;
                var $copyOfRow = $('#participant-' + participantId).clone();

                $copyOfRow.attr('id', 'participant-' + newId);
                $copyOfRow.find('#participant-' + participantId + '_nrOccasions').attr('id', 'participant-' + newId + '_nrOccasions').find('.plannedOccasions').html('0');
                $copyOfRow.attr('rel', courseId);
                $copyOfRow.removeClass('course-' + res.oldHintColor);
                $copyOfRow.addClass('course-' + res.hintColor);
                $copyOfRow.insertAfter('#participant-' + participantId).ready(function () {
                   callback(newId);
                });
            },
            error: function(err) {
                if(err.status === 409) {
                    alert('<g:message code="facilityCourse.planning.participant.error.result.anotherParticipant" />');
                } else {
                    alert('<g:message code="facilityCourse.planning.participant.error.result.other" />');
                }
            }
        });
    }

    var removeParticipant = function(occasionId, participantId) {
        $.ajax({
            method: "DELETE",
            cache: false,
            url: "${g.forJavaScript(data: createLink(action: 'removeOccasionParticipation'))}?occasionId="+occasionId+"&participantId="+participantId,
            success: function () {
                updateParticipantNrOccasionsColor(participantId, false);
            },
            error: function() {

            }
        });
    };

    function removeOldOccasionPlace(oldOccasionId) {
        var $oldOccasion = $("#"+oldOccasionId+"_occasion");
        if($oldOccasion != null && $oldOccasion != "undefined") {
            if($oldOccasion.parent().children().length == 1) {
                $oldOccasion.parent().parent().remove()
            } else {
                $oldOccasion.remove()
            }
        }
    }

    function updateParticipantNrOccasionsColor(participantId, add) {
        var $nrOccasionsBadge  = $('#participant-'+participantId+'_nrOccasions');
        var $plannedOccasions  = $nrOccasionsBadge.find('.plannedOccasions');
        var currentOccasions   = parseInt($plannedOccasions.text());
        var wantedOccasions    = parseInt($nrOccasionsBadge.find('.wantedOccasions').text());

        if(!add) {
            currentOccasions = currentOccasions-1;
        } else {
            currentOccasions = currentOccasions+1;
        }
        $plannedOccasions.text(currentOccasions);

        var classes = $nrOccasionsBadge.attr('class').split(/\s+/);
        var indexOfClass;

        _.each(classes, function(c){
            if(c.startsWith('badge-')) {
                indexOfClass = _.indexOf(classes, c);
            }
        });

        if(indexOfClass) {
            var badgeColor = (currentOccasions == wantedOccasions) ? 'success':'warning';
            $nrOccasionsBadge.removeClass(classes[indexOfClass]);
            $nrOccasionsBadge.addClass('badge-'+badgeColor);
        }
    }

    function updateCourseHintColor(courseId, hintColor) {
        $.ajax({
            method: "PUT",
            cache: false,
            url: "${g.forJavaScript(data: createLink(action: 'updateCourseHintColor'))}?courseId="+courseId+"&hintColor="+hintColor,
            success: function () {
                var occasions  = $('.panel-occasion input[name=courseId][value='+courseId+']').closest('.panel-occasion');

                _.each(occasions, function(occasion) {
                    var $occasion = $(occasion);
                    var classes   = $occasion.prop('class').split(/\s+/);
                    var indexOfClass;

                    _.each(classes, function(c){
                        if(c.startsWith('panel-occasion-')) {
                            indexOfClass = _.indexOf(classes, c);
                        }
                    });

                    if(indexOfClass) {
                        $occasion.removeClass(classes[indexOfClass]);
                        $occasion.addClass('panel-occasion-'+hintColor);
                    }
                });

                var participants  = $('.list-participants li[rel='+courseId+']');

                _.each(participants, function(participant) {
                    var $participant = $(participant);
                    var classes = $participant.prop('class').split(/\s+/);
                    var indexOfClass;


                    _.each(classes, function(c){
                        if(c.startsWith('course-')) {
                            indexOfClass = _.indexOf(classes, c);
                        }
                    });

                    if(indexOfClass) {
                        $participant.removeClass(classes[indexOfClass]);
                        $participant.addClass('course-'+hintColor);
                    }
                });
            },
            error: function() {

            }
        });
    }

    function switchCourseState($modalForm, $trainerIds, participantCustomerIds) {
        var courseId = $modalForm.find("#course").val();
        $modalForm.find("#activeCourse").val(courseId);
        if ($trainerIds.val() || participantCustomerIds.val()) {
            $modalForm.find("#course").attr("disabled", "disabled");
            $modalForm.find("#course").selectpicker("refresh");
        } else {
            $modalForm.find("#course").removeAttr("disabled");
            $modalForm.find("#course").selectpicker("refresh");
        }
    }

    function getCourseId($container) {
        return $container.parent().find("#courseId").val();
    }
</r:script>
</body>
</html>
