<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="course.settings.label"/></title>
    <r:require modules="matchi-selectpicker"/>
</head>

<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><i class="ti-list"></i> <g:message code="course.settings.label"/></li>
    </ol>

    <g:if test="${!facility?.isMasterFacility()}">
        <div class="panel panel-default panel-admin">
            <div class="panel-heading table-header">
                <span class="block text-muted text-white"><g:message code="course.settings.courts.label"/></span>
            </div>

            <div class="panel-body">
                <form id="courtsForm" class="no-bottom-padding">
                    <g:render template="trainingCourts" model="[trainingCourts: trainingCourts, facilityCourts: facilityCourts]"/>
                </form>
            </div>

            <div class="panel-footer">
                <form id="courtsAddForm" class="no-bottom-padding">
                    <div class="row">
                        <div class="form-group col-sm-5">
                            <g:select from="${facilityCourts}" name="court" optionKey="id" optionValue="name"
                                      class="form-control" title="${message(code: 'course.settings.courts.choose')}"
                                      noSelection="['': message(code: 'course.settings.courts.choose')]"/>
                        </div>

                        <div class="form-group col-sm-5">
                            <g:textField class="form-control" name="name"
                                         placeholder="${message(code: 'course.settings.courts.name')}"/>
                        </div>

                        <div class="form-group col-sm-2 pull-right">
                            <button id="addCourtButton" class="btn btn-block btn-info">
                                <i class="ti ti-plus"></i><g:message code="button.add.label"/>
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </g:if>
    <g:else><g:message code="facility.onlyLocal"/></g:else>
</div>
<r:script>
    var addCourtsFormId = "#courtsAddForm";
    var courtsFormId = "#courtsForm";
    var updateCourtsFormId = "#updateCourtsForm";
    var courtSelectId = [addCourtsFormId, "#court"].join(" ");
    var courtInputId = [addCourtsFormId, "#name"].join(" ");

    $(function () {
        var $selectAddForm = $(courtSelectId);
        $selectAddForm.selectpicker();

        allSelectPicker(courtsFormId);
        showOrHideCourtsInSelect(addCourtsFormId, false);
        initUpdateEvent(getCourts());

        var $inputAddForm = $(courtInputId);
        $inputAddForm.on("change", function () {
            if ($(this).val()) {
                $selectAddForm.attr("disabled", "disabled");
            } else {
                $selectAddForm.removeAttr("disabled");
            }
        });

        $selectAddForm.on("change", function () {
            if ($(this).val()) {
                $inputAddForm.attr("disabled", "disabled");
                $inputAddForm.val($selectAddForm.find("option:selected").text());
            } else {
                $inputAddForm.removeAttr("disabled");
                $inputAddForm.val("");
            }
        });

        $(addCourtsFormId).submit(function (e) {
            e.preventDefault();
            var formData = getFormData(addCourtsFormId);
            $.ajax({
                type: "POST",
                dataType: "html",
                url: "${g.forJavaScript(data: createLink(controller: 'facilityCourse', action: 'addTrainingCourt'))}",
                data: formData
            }).done(function (data) {
                if (data !== "400") {
                    $(courtsFormId).html(data);

                    allSelectPicker(courtsFormId);
                    showOrHideCourtsInSelect(addCourtsFormId, false);
                    resetToInitState(courtSelectId, courtInputId);
                    initUpdateEvent(getCourts());
                }
            });
        });

        $(courtsFormId).submit(function (e) {
            e.preventDefault();
            var $focusButton = $(this).find("button:focus");
            var action = $("#action").val();
            var buttonIndex = $focusButton.attr("id").split("-").pop();
            var formData = getFormData(courtsFormId);
            var data = {id: $focusButton.data("id"), name: formData["name-" + buttonIndex]};
            var url = (action === "remove") ?
                "${g.forJavaScript(data: createLink(controller: 'facilityCourse', action: 'removeTrainingCourt'))}" :
                "${g.forJavaScript(data: createLink(controller: 'facilityCourse', action: 'updateTrainingCourt'))}";
            $.ajax({
                type: "POST",
                dataType: "html",
                url: url,
                data: data
            }).done(function (data) {
                if (data !== "400") {
                    $(courtsFormId).html(data);

                    allSelectPicker(courtsFormId);
                    showOrHideCourtsInSelect(addCourtsFormId, (action === "remove") ? true : false);
                    initUpdateEvent(getCourts());
                } else {
                    location.reload();
                }
            });
        });
    });

    var getFormData = function (formId) {
        var formData = {};
        var form = $(formId).serializeArray();
        $.each(form, function (idx, item) {
            formData[item.name] = item.value;
        })
        return formData;
    }

    var allSelectPicker = function (formId) {
        $(formId).find("select").selectpicker();
    }

    var resetToInitState = function (selectId, inputId) {
        $(selectId).val("").change();
        $(inputId).val("").change();
    }

    var showOrHideCourtsInSelect = function(formId, isShow) {
        var courts = findUsedOptionsInSelect(courtsFormId);

        if (courts) {
            $(formId + " .dropdown-menu.open").find("li").each(function(idx, li) {
                var $currentListItem = $(li);
                var span = $currentListItem.find("span.text");
                if ($.inArray($(span).text(), courts) >= 0) {
                    $currentListItem.css("display", "none");
                } else {
                    if (isShow) {
                        $currentListItem.removeAttr("style");
                    }
                }
            });
        }
    }

    var findUsedOptionsInSelect = function(formId) {
        var courts = []
        $(formId).find("select").each(function(idx, select) {
            if ($(select).val()) {
                courts[idx] = $(select).find("option:selected").text();
            }
        });
        return courts;
    }

    var getCourts = function() {
        var result = [];
        var $courtRows = $([courtsFormId, ".row"].join(" "));
        for (var i = 0; i < $courtRows.length; i++) {
            var $input = $("#name-" + i);
            var $option = $("#court-" + i + " > option:selected");
            var $button = $("#btn-" + i);
            result.push({
                oldValue: $input.val(),
                input: $input,
                button: $button
            });
        }
        return result;
    }

    var bindUpdateEvent = function($toElement, $updatedElement, resetStateValue) {
        $toElement.keyup(function() {
            if (resetStateValue === $toElement.val()) {
                $("#action").val("remove");
                $updatedElement.removeClass("btn-success").addClass("btn-danger");
                $updatedElement.html("<i class='ti ti-trash'></i><g:message code="button.delete.label"/>");
            } else {
                $("#action").val("update");
                $updatedElement.removeClass("btn-danger").addClass("btn-success");
                $updatedElement.html("<i class='ti ti-check'></i><g:message code="button.update.label"/>");
            }
        });
    }

    var initUpdateEvent = function(courts) {
        for (var i = 0; i < courts.length; i++) {
            bindUpdateEvent(courts[i].input, courts[i].button, courts[i].oldValue);
        }
    }
</r:script>
</body>
</html>
