<%@ page import="com.matchi.activities.Activity"%>
<r:require modules="matchi-selectpicker, bootstrap-tagsinput, bootstrap-slider"/>
<r:script>

    $("input[data-role=tagsinput]").tagsinput({
        tagClass: "label label-success"
    });

    $(function() {
        updateDisabledPropForMaxSubmissionsField();
        $("#noLimit").on('change', function(){
            updateDisabledPropForMaxSubmissionsField();
        });
        $("#form").submit(function(){
          if($("#noLimit").is(":checked")){
            $("#form\\.maxSubmissions").val("")
          }
        });
        $('#description').wysihtml5({
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });

        $('#extendedEmailMessage').wysihtml5({
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });

        $("#trainers").allselectpicker({
            selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
            countSelectedText: "{0} ${message(code: 'facilityCourseParticipant.index.trainers.selectedText')}",
            selectedTextFormat: 'count'
        });
        var trainerIds = getTrainerIds();

        var relatedFormTemplate = $("#relatedFormTemplate");
        relatedFormTemplate.selectpicker();
        relatedFormTemplate.change(function(){
            $("#formField").html("");
            toggleFormOptions();
            $.post('${g.forJavaScript(data: createLink(action: 'getFormTemplate'))}', {formTemplateId: $(this).val()})
                .done(function(data){
                    $("#formField").html(data);
                    initBootstrapSwitch();
                    toggleFormOptions('show');
                });
        });
        var startDatePicker = $("#showStartDate");
        var endDatePicker = $("#showEndDate");

        startDatePicker.datepicker({
            autoSize: true,
            dateFormat: '<g:message code="date.format.dateOnly.small"/>',
            altField: '#startDate',
            altFormat: 'yy-mm-dd'
        });
        var startDate = new Date(startDatePicker.val());
        endDatePicker.datepicker({
            autoSize: true,
            dateFormat: '<g:message code="date.format.dateOnly.small"/>',
            altField: '#endDate',
            altFormat: 'yy-mm-dd',
            minDate: new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())
        });

        startDatePicker.on('change', function() {
            endDatePicker.datepicker( "option", "minDate", new Date($('#startDate').val()));
        });

        var activeFromDatePicker = $("#showActiveFrom");
        var activeToDatePicker = $("#showActiveTo");
        activeFromDatePicker.datepicker({
            autoSize: true,
            dateFormat: '<g:message code="date.format.dateOnly.small"/>',
            altField: '#activeFrom',
            altFormat: 'yy-mm-dd',
            defaultDate: new Date()
        });
        var activeFromDate = new Date(activeFromDatePicker.val());
        activeToDatePicker.datepicker({
            autoSize: true,
            dateFormat: '<g:message code="date.format.dateOnly.small"/>',
            altField: '#activeTo',
            altFormat: 'yy-mm-dd',
            minDate: new Date(activeFromDate.getFullYear(), activeFromDate.getMonth(), activeFromDate.getDate())
        });

        activeFromDatePicker.on('change', function(){
            activeToDatePicker.datepicker( "option", "minDate", new Date($('#activeFrom').val()));
        });

        $("[rel=tooltip]").tooltip({ delay: { show: 1000, hide: 100 } });
        $('#membershipRequired').bootstrapSwitch({
            onColor: 'success',
            onSwitchChange: function(event, state) {
                $(this).val(state);
            }
        });
        $('#paymentRequired').bootstrapSwitch({
            onColor: 'success',
            onSwitchChange: function(event, state) {
                $(this).val(state);
                $("#price-wrapper").toggle(state);
                $("#form\\.price").prop("required", state);
            }
        });
        initBootstrapSwitch();
        $("form").submit(function() {
            var $relatedFormTemplate = $("#relatedFormTemplate");
            var $activeFrom = $("#showActiveFrom");
            var $activeTo = $("#showActiveTo");

            var $maxSubmissions = $("#form\\.maxSubmissions");
            $maxSubmissions.val($maxSubmissions.val().replace(/[^\d]/,''));
            if($relatedFormTemplate.val() != "" && $activeFrom.val() == "" ) {
                $activeFrom.focus();
                return false
            } else if($relatedFormTemplate.val() !== "" && $activeTo.val() === "") {
                $activeTo.focus();
                return false
            }
            return true
        });
        $("#form\\.maxSubmissions").on('keyup', function(){
            this.value=this.value.replace(/[^\d]/,'')
        });

        $("#formField").on("click", ".add-form-field-value", function() {
            addFormFieldValue($(this).closest(".optionSection"));
        }).on("click", ".delete-form-field-value", function() {
            var fieldValues = $(this).closest(".field-values");
            if (fieldValues.find(".form-field-value").length > 1) {
                $(this).closest(".form-field-value").remove();
                fieldValues.find("> div").each(function(index) {
                    $(this).find(":input").each(function() {
                        $(this).attr("name", $(this).attr("name").replace(
                                /predefinedValues\[\d+\]/, "predefinedValues[" + index + "]"));
                    });
                });
            } else {
                $(this).closest(".form-field-value").find(":input").each(function() {
                    $(this).val("");
                });
            }
        }).on("click", ".move-up", function() {
            var currentRow = $(this).closest(".form-field-row");
            var prevRow = currentRow.prev();
            if (prevRow.length) {
                prevRow.before(currentRow);
                reinitWysihtml5(currentRow);
                updateFieldsPositions();
            }
        }).on("click", ".move-down", function() {
            var currentRow = $(this).closest(".form-field-row");
            var nextRow = currentRow.next();
            if (nextRow.length) {
                nextRow.after(currentRow);
                reinitWysihtml5(currentRow);
                updateFieldsPositions();
            }
        });

        $("input[data-role=tagsinput]").on("itemAdded", function(event) {
            var pattern = /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/;
            if (!pattern.test(event.item)) {
                $(event.target).tagsinput("remove", event.item);
            }
        });

        $("input[data-role=tagsinput]").on("itemRemoved", function(event) {
            var trainerId = getTrainerIdByEmail(event.item);
            if (trainerId) {
                var idx = trainerIds.indexOf(trainerId);
                if (idx != -1) {
                    trainerIds.splice(idx, 1);
                }
            }
        });

        $("#trainers").change(function(event) {
            var availableTrainerIds = $(event.target).val();
            var extraTrainerIds = [];
            if (availableTrainerIds && (!trainerIds || trainerIds.length <= availableTrainerIds.length)) {
                extraTrainerIds = trainerIds ? availableTrainerIds.filter(function(x) { return trainerIds.indexOf(x) < 0 }) : availableTrainerIds;
                trainerIds = trainerIds.concat(extraTrainerIds);
            } else if (trainerIds && (!availableTrainerIds || trainerIds.length > availableTrainerIds.length)) {
                extraTrainerIds = availableTrainerIds ? trainerIds.filter(function(x) { return availableTrainerIds.indexOf(x) < 0 }) : trainerIds;
                trainerIds = trainerIds.filter(function(x) { return extraTrainerIds.indexOf(x) < 0 });
            }

            if (extraTrainerIds) {
                extraTrainerIds.forEach(function(trainerId) {
                    var trainerEmail = $("#trainer-" + trainerId).val();
                    if (trainerEmail) {
                        var tagEmails = $("#emails").tagsinput("items");
                        var idx = tagEmails.indexOf(trainerEmail);
                        if (idx != -1) {
                            $("#emails").tagsinput("remove", trainerEmail);
                        } else {
                            $("#emails").tagsinput("add", trainerEmail);
                        }
                    }
                });
            }
        });

        $('.level-slider').slider({
            range: true,
            min: ${Activity.LEVEL_RANGE_MIN},
            max: ${Activity.LEVEL_RANGE_MAX},
            step: 1,
            change: function( event, ui ) {
                updateLevelValues(ui.values)
            },
            slide: function( event, ui ) {
                updateLevelValues(ui.values)
            }
        });

        $('.level-slider').slider("values", 0, "${g.forJavaScript(data: courseInstance.levelMin ?: Activity.LEVEL_RANGE_MIN)}");
        $('.level-slider').slider("values", 1, "${g.forJavaScript(data: courseInstance.levelMax ?: Activity.LEVEL_RANGE_MAX)}");

        $("#useLevel").on("change", function() {
            if ($(this).is(":checked")) {
                $("#useLevelContainer").show();
            } else {
                $("#useLevelContainer").hide();
            }
        }).trigger("change");

        $('#showOnline').bootstrapSwitch({
            onColor: 'success',
            onSwitchChange: function(event, state) {
                $(this).val(state);
            }
        });
    });

    function updateLevelValues(values) {
        $('#level').val(values);
        $('.level-slider-display').text(values[0] + " - " + values[1]);
    }

    function updateFieldsPositions() {
        $("#formField").find(":hidden[name^=fieldPosition]").each(function() {
            $(this).val($(this).closest(".form-field-row").index());
        });
    }

    function reinitWysihtml5(row) {
        var ta = row.find("textarea.wysihtml5");
        if (ta.length) {
            ta.prop('disabled', false);
            ta.parent().find(".wysihtml5-toolbar").remove().end()
                    .find("iframe").remove().end()
                    .find(':hidden[name="_wysihtml5_mode"]').remove();
            ta.show();
            ta.wysihtml5({
                stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
            });
            if(ta.closest("section.optionSection").hasClass('disabled')) {
                ta.parent().find("a.btn").hide();
            }
        }
    }

    function addFormFieldValue(formField) {
        var template = formField.find(".form-field-value").last().clone(true, true);
        var index = formField.find(".form-field-value").length;
        template.find(":input").each(function() {
            $(this).val("").attr("name", $(this).attr("name").replace(
                    /predefinedValues\[\d+\]/, "predefinedValues[" + index + "]"));
        });
        formField.find(".field-values").append(template);
    }

    function toggleParentAndCheckbox(jqueryObject){
        var parent = $(jqueryObject).parents("section.optionSection");
        if(parent.hasClass('disabled')) {
            parent.removeClass('disabled');
            parent.find('input[type="checkbox"]').prop('disabled', false);
            parent.find('textarea').prop('disabled', false);
            parent.find(".field-values").find("input[id$=isActive]:checked").each(function() {
                $(this).closest(".form-field-value").find("[id^=timeRangeSlider]").slider("enable");
            });
            parent.find(".field-values").find("input").prop("disabled", false);
            parent.find("a.btn").show();
        } else {
            parent.addClass('disabled');
            parent.find('input[type="checkbox"]').prop('disabled', true);
            parent.find('textarea').prop('disabled', true);
            parent.find("[id^=timeRangeSlider]").slider("disable");
            parent.find(".field-values").find("input").prop("disabled", true);
            parent.find("a.btn").hide();
        }
    }
    function initBootstrapSwitch(){
        $('.checkbox-fields').bootstrapSwitch({
            onColor: 'success',
            onSwitchChange: function(event, state) {
                toggleParentAndCheckbox($(this));
                $(this).val(state);
            }
        });
        $('.checkbox-fields-disabled').bootstrapSwitch({
            onColor: 'success',
            disabled: true,
            onSwitchChange: function(event, state) {
                toggleParentAndCheckbox($(this));
                $(this).val(state);
            }
        });
    }
    function toggleFormOptions(method){
        var activeFromDiv = $("#activeFromDiv");
        var formOptions = $("#formOptions");
        if(method == 'show') {
          activeFromDiv.show();
          $("#activeFrom").prop('required',true);
          $("#activeTo").prop('required',true);
          formOptions.show();
        } else {
          activeFromDiv.hide();
          $("#activeFrom").prop('required',false);
          $("#activeTo").prop('required',false);
          formOptions.hide();
        }
    }
  function updateDisabledPropForMaxSubmissionsField(){
    var maxSubmissions = $("#form\\.maxSubmissions");
           if($("#noLimit").is(":checked")){
                maxSubmissions.prop("disabled", true)
           }else{
                maxSubmissions.prop("disabled", false)
           }
  }

  function getTrainerIdByEmail(email) {
      var $inputTrainer = $("input[value='" + email + "']");
      return $inputTrainer.length > 0 ? $inputTrainer.attr("id").split("-")[1] : null;
  }

  function getTrainerIds() {
      var result = [];
      var tagEmails = $("#emails").tagsinput("items");
      if (tagEmails) {
          tagEmails.forEach(function(email) {
              var trainerId = getTrainerIdByEmail(email);
              if (trainerId) {
                  result.push(trainerId);
              }
          });
          return result;
      }
      return result;
  }
</r:script>
<style>
/**
  Use in template _formFields.gsp
*/
.optionSection.disabled {
    background: none repeat scroll 0 0 #ebebeb;
}

.optionSection {
    border-top: 1px solid #ddd;
    margin-bottom: 0;
    padding: 20px 10px;
    position: relative;
}

.form-field-text {
    display: inline;
}

.field-help-text {
    color: #808080;
    font-size: 0.9em;
}

.isActive-switch {
    float: left;
    margin-right: 20px;
}

.panel-body {
    padding: 0 15px;
}

.no-form {
    display: none;
}
.noLimit{
    margin-top: 40px;
}

.bootstrap-tagsinput {
    width: 100%;
}

.bootstrap-tagsinput input {
    border: 0;
}
</style>

<div class="row">
    <div class="col-sm-6 form-group">
        <label for="name"><g:message code="course.name.label"/>*</label>
        <g:textField name="name" maxlength="255" required="required" class="form-control" value="${courseInstance?.name}"/>
    </div>
    <div class="col-sm-6 form-group">
        <g:each in="${trainers}">
            <g:hiddenField name="trainer-${it.id}" value="${it.email}"/>
        </g:each>
        <label for="trainers"><g:message code="course.trainers.label"/></label>
        <g:select class="form-control" name="trainers" from="${trainers}"
                  optionKey="id" multiple="true" value="${courseInstance?.trainers}"/>
    </div>
</div>

<div class="row">
    <div class="col-sm-6 form-group">
        <div class="form-group">
            <label for="showStartDate"><g:message code="course.startDate.label"/>*</label>

            <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                <input class="form-control" readonly="true" type="text" name="showStartDate" id="showStartDate" required="required"
                       value="${formatDate(date: courseInstance?.startDate, formatName: 'date.format.dateOnly')}"/>
                <g:hiddenField name="startDate" id="startDate" value="${formatDate(date: courseInstance?.startDate, format: 'yyyy-MM-dd')}"/>
            </div>
        </div>
    </div>

    <div class="col-sm-6">
        <div class="form-group">
            <label for="showEndDate"><g:message code="course.endDate.label"/>*</label>

            <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                <input class="form-control" readonly="true" type="text" name="showEndDate" id="showEndDate" required="required"
                       value="${formatDate(date: courseInstance?.endDate, formatName: 'date.format.dateOnly')}"/>
                <g:hiddenField name="endDate" id="endDate" value="${formatDate(date: courseInstance?.endDate, format: 'yyyy-MM-dd')}"/>
            </div>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-sm-12 form-group">
        <label for="description"><g:message code="trainer.description.label"/></label>
        <g:textArea class="form-control" rows="10" name="description" value="${courseInstance?.description}"/>
    </div>
</div>

<div class="row">
    <div class="col-sm-12 form-group">
        <label for="extendedEmailMessage"><g:message code="trainer.extendedEmailMessage.label"/></label>
        <g:textArea class="form-control" rows="10" name="extendedEmailMessage" value="${courseInstance?.extendedEmailMessage}"/>
    </div>
</div>

<div class="space-40 clearfix"></div>

<div class="row">
    <div class="col-sm-6 form-group">
        <label for="emails"><g:message code="course.emails.label"/></label> <g:inputHelp
            title="${g.message(code: 'course.emails.tooltip')}"/>
        <g:textField type="text" name="emails" data-role="tagsinput" value="${courseInstance.emails}" />
    </div>
</div>

<div class="row">
    <div class="col-sm-6 form-group">
        <div class="checkbox">
            <g:checkBox name="useLevel" value="${courseInstance?.levelMin ? true : false}"/>
            <label for="useLevel"><g:message code="classActivity.changeLevel.label"/></label>
        </div>
    </div>
</div>
<div class="row" id="useLevelContainer">
    <div class="col-sm-6 form-group">
        <label class="control-label" for="level">
            <g:message code="course.level.label"/>
            <g:inputHelp title="${message(code: 'course.level.tooltip')}"/>
        </label>

        <div class="level-slider span4" style="display: inline-block; width: 50%; margin: 0 15px;"></div><span class="level-slider-display"></span>
        <g:field type="hidden" name="level" class="top-margin10"
                 value="${courseInstance.levelMin ?: Activity.LEVEL_RANGE_MIN}, ${courseInstance.levelMax ?: Activity.LEVEL_RANGE_MAX}" />
        <p class="top-margin10" style="color: #676767"><g:message code="activity.help.aboutLevels" /></p>
    </div>
</div>

<div class="row">
    <div class="col-sm-6">

        <!-- SELECT FORM -->

        <div class="form-group">
            <label><g:message code="course.form.label"/></label>* <g:inputHelp
                title="${g.message(code: 'form.relatedFormTemplate.tooltip')}"/>
        <g:set var="noSelectionMessage"
               value="${formTemplates ? "${message(code: 'course.select.form')}" : "${message(code: 'no.forms.available')}"}"/>
        <g:select class="form-control" id="relatedFormTemplate" name="form.relatedFormTemplate.id" from="${formTemplates}"
                  optionKey="id"
                  optionValue="name" noSelection="['': '' + noSelectionMessage]"
                  value="${courseInstance?.form?.relatedFormTemplate?.id}"/>
        </div>

    </div>

    <div class="col-sm-6 ${courseInstance?.form ? '' : 'no-form'}" id="activeFromDiv">
        <div class="form-group">
            <label for="showActiveFrom"><g:message code="course.publish.date"/>*</label> <g:inputHelp
                title="${g.message(code: 'course.publish.date.tooltip')}"/>

            <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                <input class="form-control" readonly="true" type="text" name="showActiveFrom" id="showActiveFrom" required="required"
                       value="${formatDate(date: courseInstance?.form?.activeFrom, formatName: 'date.format.dateOnly')}"/>
                <g:hiddenField name="form.activeFrom" id="activeFrom" value="${formatDate(date: courseInstance?.form?.activeFrom, format: 'yyyy-MM-dd')}"/>
            </div>
        </div>
    </div>

</div>

<div class="${courseInstance?.form ? '' : 'no-form'}" id="formOptions">
    <div class="row">
        <div class="col-sm-3">
            <div class="form-group">
                <label for="form.maxSubmissions"><g:message code="form.maxSubmissions.label"/></label> <g:inputHelp
                    title="${g.message(code: 'form.maxSubmissions.tooltip')}"/>
            <g:field type="number" name="form.maxSubmissions" value="${courseInstance?.form?.maxSubmissions}"
                         maxlength="9" onkeyup=""
                         class="form-control"/>
            </div>
        </div>

        <div class="col-sm-3">
            <div class="checkbox noLimit">
                <g:checkBox name="noLimit" checked="${!courseInstance?.form?.maxSubmissions}" value="${courseInstance?.form?.maxSubmissions == null}"/>
                <label for="noLimit"><g:message code="form.noLimit.label"/></label>
            </div>
        </div>

        <div class="col-sm-6">
            <div class="form-group">
                <label for="showActiveTo"><g:message code="course.application.deadline"/>*</label> <g:inputHelp
                    title="${g.message(code: 'course.application.deadline.tooltip')}"/>

                <div class="input-group">
                    <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                    <input class="form-control" readonly="true" type="text" name="showActiveTo" id="showActiveTo" required="required"
                           value="${formatDate(date: courseInstance?.form?.activeTo, formatName: 'date.format.dateOnly')}"/>
                <g:hiddenField name="form.activeTo" id="activeTo" value="${formatDate(date: courseInstance?.form?.activeTo, format: 'yyyy-MM-dd')}"/>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-sm-12">
            <div class="form-group">
              <g:checkBox name="form.membershipRequired" id="membershipRequired"
                          value="${courseInstance?.form?.membershipRequired ?: false}"/>
                <label for="membershipRequired" class="left-margin20"><g:message code="course.membershipRequired.label"/></label>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12">
            <div class="form-group">
                <g:checkBox name="showOnline"
                            value="${courseInstance?.showOnline ?: false}"/>
                <label for="showOnline" class="left-margin20"><g:message code="eventActivity.showOnline.label"/></label>
            </div>
        </div>
    </div>
    <g:render template="/templates/dynamicForms/priceWrapper" model="[paymentRequired: courseInstance?.form?.paymentRequired, price: courseInstance?.form?.price]" />
    <section class="panel panel-default" id="applyFormEdit">
        <div class="panel-body no-bottom-padding" id="formField">
            <g:render template="formFields" model="[formFields: courseInstance?.form?.fields]"/>
        </div>
    </section>
</div>