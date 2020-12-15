<r:require modules="bootstrap3-wysiwyg"/>

<div class="col-sm-12 form-group ${hasErrors(bean: globalNotificationInstance, field: 'title', 'error')}">
    <label for="notificationText">
        <g:message code="globalNotification.title.label"/>*
    </label>
    <div class="controls">
        <g:textField class="form-control" name="title" required="required"
                     value="${globalNotificationInstance?.title}"/>
    </div>
</div>


<div class="col-sm-12 form-group ${hasErrors(bean: globalNotificationInstance, field: 'notificationText', 'error')}">
    <label for="notificationText">
        <g:message code="globalNotification.notificationText.label"/>*
    </label>
    <div class="controls">
        <g:render template="/templates/i18n/translatable"
                model="[translatable: globalNotificationInstance?.notificationText, propertyName: 'notificationText',
                        addBtnLabel: message(code: 'globalNotification.notificationText.add.button')]"/>
    </div>
</div>


<div class="col-sm-6 form-group ${hasErrors(bean: globalNotificationInstance, field: 'publishDate', 'error')}">
    <label for="publishDate">
        <g:message code="globalNotification.publishDate.label"/>*
    </label>
    <div class="controls">
        <div class="input-group">
            <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
            <input class="form-control" readonly="true" type="text" name="publishDate" id="publishDate"
                   value="${formatDate(date: globalNotificationInstance?.publishDate, formatName: 'date.format.dateOnly')}"/>
        </div>
    </div>
</div>

<div class="col-sm-6 form-group ${hasErrors(bean: globalNotificationInstance, field: 'endDate', 'error')}">
    <label for="endDate">
        <g:message code="globalNotification.endDate.label"/>*
    </label>
    <div class="controls">
        <div class="input-group">
            <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
            <input class="form-control" readonly="true" type="text" name="endDate" id="endDate"
                   value="${formatDate(date: globalNotificationInstance?.endDate, formatName: 'date.format.dateOnly')}"/>
        </div>
    </div>
</div>

<div class="col-sm-12 form-group">
    <div class="checkbox">
        <g:checkBox name="isForUsers" value="${globalNotificationInstance?.isForUsers}"/>
        <label for="isForUsers"><g:message code="globalNotification.isForUsers.label"/></label>
    </div>
</div>

<div class="col-sm-12 form-group">
    <div class="checkbox">
        <g:checkBox name="isForFacilityAdmins" value="${globalNotificationInstance?.isForFacilityAdmins}"/>
        <label for="isForFacilityAdmins"><g:message code="globalNotification.isForFacilityAdmins.label"/></label>
    </div>
</div>

<r:script>
    var wysihtml5Stylesheets = "${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}";

    $(function() {
        $(":input[name^=notificationText]").wysihtml5({
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });

        var publishDatePicker = $("#publishDate");
        var endDatePicker = $("#endDate");
        publishDatePicker.datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}",
            minDate: new Date()
        });
        var publishDate = new Date(publishDatePicker.val());
        endDatePicker.datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}",
            minDate: new Date(publishDate.getFullYear(), publishDate.getMonth(), publishDate.getDate() + 1)
        });
        publishDatePicker.on('change', function(){
          endDatePicker.datepicker( "option", "minDate", new Date($(this).val()));
        });

        $("#isForUsers").on("click", function() {
            $("#isForFacilityAdmins").prop("checked", false);
        });
        $("#isForFacilityAdmins").on("click", function() {
            $("#isForUsers").prop("checked", false);
        });
    });
</r:script>
