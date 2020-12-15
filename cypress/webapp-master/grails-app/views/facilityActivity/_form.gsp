<%@ page import="com.matchi.activities.Activity"%>
<r:require modules="bootstrap3-wysiwyg, bootstrap-switch"/>
<div class="row">
    <div class="col-sm-8 form-group ${hasErrors(bean: classActivityInstance, field: 'name', 'has-error')} required">
        <label><g:message code="default.name.label"/> <g:inputHelp title="${message(code: 'facilityActivity.create.message8')}"/></label>
        <g:textField class="form-control" name="name" value="${classActivityInstance?.name}" />
        <p class="help-block">${message(code: 'facilityActivity.create.headline.helptext')}<br/>${message(code: 'facilityActivity.create.globalsearch.helptext')}</p>
    </div>
</div>
<div class="row">
    <div class="col-sm-8 form-group ${hasErrors(bean: classActivityInstance, field: 'description', 'has-error')}">
        <label><g:message code="classActivity.description.label"/> <g:inputHelp title="${message(code: 'classActivity.description.hint')}"/></label><br/>
        <g:textArea rows="3" cols="30" class="form-control" name="description" value="${classActivityInstance?.description}"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-8 form-group ${hasErrors(bean: classActivityInstance, field: 'teaser', 'has-error')} required">
        <label><g:message code="facilityActivity.create.message13"/> <g:inputHelp title="${message(code: 'facilityActivity.create.message14')}"/></label><br/>
        <g:textArea rows="3" cols="30" class="form-control" name="teaser" value="${classActivityInstance?.teaser}"/>
        <p class="help-block">${message(code: 'facilityActivity.create.teaser.helptext')} ${message(code: 'facilityActivity.create.globalsearch.helptext')}</p>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group ${hasErrors(bean: classActivityInstance, field: 'terms', 'has-error')}">
        <label><g:message code="classActivity.terms.label"/> <g:inputHelp title="${message(code: 'classActivity.terms.hint')}"/></label><br/>
        <g:textArea name="terms" rows="7" cols="40" class="form-control" value="${toRichHTML(text: classActivityInstance?.terms)}"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-8 form-group ${hasErrors(bean: classActivityInstance, field: 'email', 'has-error')}">
        <label><g:message code="facilityActivity.create.message15"/> <g:inputHelp title="${message(code: 'facilityActivity.create.message16')}"/></label><br/>
        <g:textField name="email" class="form-control" value="${classActivityInstance?.email}"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group">
        <label><g:message code="classActivity.notifyWhenSignUp.label"/></label><br/>
        <g:checkBox name="notifyWhenSignUp" class="form-control" checked="${classActivityInstance ? classActivityInstance.notifyWhenSignUp : false}" value="${classActivityInstance ? classActivityInstance.notifyWhenSignUp : false}"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group">
        <label><g:message code="classActivity.notifyWhenCancel.label"/></label><br/>
        <g:checkBox name="notifyWhenCancel" class="form-control" checked="${classActivityInstance ? classActivityInstance.notifyWhenCancel : false}" value="${classActivityInstance ? classActivityInstance.notifyWhenCancel : false}"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-8 form-group ${hasErrors(bean: classActivityInstance, field: 'userMessageLabel', 'has-error')}">
        <label><g:message code="classActivity.userMessageLabel.label"/> <g:inputHelp title="${message(code: 'classActivity.userMessageLabel.hint')}"/></label><br/>
        <g:textField name="userMessageLabel" class="form-control" value="${classActivityInstance?.userMessageLabel}"
                     maxlength="255"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-2 form-group ${hasErrors(bean: classActivityInstance, field: 'price', 'has-error')}">
        <label><g:message code="activity.price.label"/> <g:inputHelp title="${message(code: 'activity.price.label.help')}"/></label><br/>
        <g:field type="number" name="price" value="${classActivityInstance?.price}"
                 min="0" max="${Integer.MAX_VALUE}" class="form-control"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-3 form-group">
        <label><g:message code="classActivity.changeLevel.label"/></label><br/>
        <g:checkBox name="useLevel" class="form-control" checked="${classActivityInstance?.levelMin ?: params.level}" value="${classActivityInstance?.levelMin ?: params.level}"/>
    </div>
    <div class="col-sm-4 form-group" id="useLevelContainer" ${classActivityInstance?.levelMin ? "visible" : "hidden"}>
        <label><g:message code="activity.level.label"/></label> <g:inputHelp title="${message(code: 'activity.level.label.help')}"/>
        <div class="level-slider"></div>
        <g:field type="hidden" name="level" class="top-margin10 ${hasErrors(bean: classActivityInstance, field: 'level', 'has-error')}" style="width: 40px;"
                 value="${classActivityInstance?.levelMin ?: Activity.LEVEL_RANGE_MIN}, ${classActivityInstance?.levelMax  ?: Activity.LEVEL_RANGE_MAX}" />
        <span class="col-sm-3 level-slider-display" style="margin-top: 5px"></span>
        <p class="help-block" style="clear:both"><g:message code="activity.help.aboutLevels" /></p>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group ${hasErrors(bean: classActivityInstance, field: 'signUpDaysInAdvanceRestriction', 'has-error')}">
        <label><g:message code="activity.signUpDaysInAdvanceRestriction.label"/> <g:inputHelp title="${message(code: 'activity.signUpDaysInAdvanceRestriction.label.help')}"/></label><br/>
        <g:field type="number" name="signUpDaysInAdvanceRestriction"
                 value="${classActivityInstance?.signUpDaysInAdvanceRestriction}"
                 min="0" max="${Integer.MAX_VALUE}" class="form-control"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group ${hasErrors(bean: classActivityInstance, field: 'signUpDaysUntilRestriction', 'has-error')}">
        <label><g:message code="activity.signUpDaysUntilRestriction.label"/> <g:inputHelp title="${message(code: 'activity.signUpDaysUntilRestriction.label.help')}"/></label><br/>
        <g:field type="number" name="signUpDaysUntilRestriction"
                 value="${classActivityInstance?.signUpDaysUntilRestriction}"
                 min="0" max="${Integer.MAX_VALUE}" class="form-control"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group">
        <label><g:message code="activity.onlineByDefault.label"/></label><br/>
        <g:checkBox name="onlineByDefault" class="form-control" checked="${classActivityInstance ? classActivityInstance.onlineByDefault : true}" value="${activity ? classActivityInstance.onlineByDefault : true}"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group">
        <label><g:message code="activity.membersOnly.label"/></label><br/>
        <g:checkBox name="membersOnly" class="form-control" checked="${classActivityInstance?.membersOnly ?: false}" value="${classActivityInstance?.membersOnly ?: false}"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group">
        <label><g:message code="classActivity.cancelByUser.label"/></label><br/>
        <g:checkBox name="cancelByUser" class="form-control" checked="${classActivityInstance ? classActivityInstance.cancelByUser : true}" value="${classActivityInstance ? classActivityInstance.cancelByUser : true}"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group ${hasErrors(bean: classActivityInstance, field: 'cancelLimit', 'has-error')}">
        <label><g:message code="classActivity.cancelLimit.label"/> <g:inputHelp title="${message(code: 'classActivity.cancelLimit.hint')}"/></label><br/>
        <g:field type="number" name="cancelLimit" min="0" max="${Integer.MAX_VALUE}" class="form-control"
                 value="${classActivityInstance ? classActivityInstance.cancelLimit : getUserFacility().getBookingCancellationLimit()}"/>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group ${hasErrors(bean: classActivityInstance, field: 'cancelHoursInAdvance', 'has-error')}">
        <label><g:message code="classActivity.cancelHoursInAdvance.label"/></label>
        <g:field type="number" name="cancelHoursInAdvance" min="0" max="${Integer.MAX_VALUE}" class="form-control"
                 value="${classActivityInstance ? classActivityInstance.cancelHoursInAdvance : ''}"/>
        <p class="help-block">${message(code: 'classActivity.cancelHoursInAdvance.helptext')}</p>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group ${hasErrors(bean: classActivityInstance, field: 'minNumParticipants', 'has-error')}">
        <label><g:message code="classActivity.minNumParticipants.label"/></label>
        <g:field type="number" name="minNumParticipants" min="0" max="${Integer.MAX_VALUE}" class="form-control"
                 value="${classActivityInstance ? classActivityInstance.minNumParticipants : ''}"/>
        <p class="help-block">${message(code: 'classActivity.minNumParticipants.helptext')}</p>
    </div>
</div>
<div class="row">
    <div class="col-sm-12 form-group ${hasErrors(bean: classActivityInstance, field: 'maxNumParticipants', 'has-error')}">
        <label><g:message code="activity.maxNumParticipants.label"/> <g:inputHelp title="${message(code: 'activity.maxNumParticipants.label.help')}"/></label>
        <g:field type="number" name="maxNumParticipants" min="1" max="${Integer.MAX_VALUE}" class="form-control"
                 value="${classActivityInstance?.maxNumParticipants}"/>
    </div>
</div>

<r:script>
    $(function() {
        $("#terms").wysihtml5({
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });

        $('[name="useLevel"],[name="onlineByDefault"],[name="membersOnly"],[name="cancelByUser"],[name="notifyWhenSignUp"],[name="notifyWhenCancel"]').bootstrapSwitch({
            onColor: 'success',
            onSwitchChange: function(event, state) {
                $(this).val(state);

                if ($(this).prop('name') === 'useLevel') {
                    if (state) {
                        $('#useLevelContainer').toggle();
                    } else {
                        $('#useLevelContainer').toggle();
                    }
                }

                if ($(this).prop('name') === 'cancelByUser') {
                    if (state) {
                        $('#cancelLimit').prop("disabled", false);
                    } else {
                        $('#cancelLimit').prop("disabled", true).val("");
                    }
                }
            }
        });

        if($('#email').val() == '') {
            $("[name='notifyWhenSignUp']").bootstrapSwitch('disabled',true);
            $("[name='notifyWhenCancel']").bootstrapSwitch('disabled',true);
        }

        $('#email').on('input',function(){
            if($('#email').val() != '') {
                $("[name='notifyWhenSignUp']").bootstrapSwitch('disabled',false);
                $("[name='notifyWhenCancel']").bootstrapSwitch('disabled',false);
            } else {
                $("[name='notifyWhenSignUp']").bootstrapSwitch("disabled", true);
                $("[name='notifyWhenCancel']").bootstrapSwitch('disabled',true);
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
        $('.level-slider').slider("values", 0, "${g.forJavaScript(data: classActivityInstance?.levelMin ?: Activity.LEVEL_RANGE_MIN)}");
        $('.level-slider').slider("values", 1, "${g.forJavaScript(data: classActivityInstance?.levelMax ?: Activity.LEVEL_RANGE_MAX)}");
    });

function updateLevelValues(values) {
    $('#level').val(values);
    $('.level-slider-display').text(values[0] + " - " + values[1]);
}

</r:script>