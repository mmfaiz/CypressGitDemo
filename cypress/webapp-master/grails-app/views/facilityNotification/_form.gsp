<r:require modules="bootstrap3-wysiwyg"/>

<div class="col-sm-12 form-group ${hasErrors(bean: facilityNotificationInstance, field: 'headline', 'error')}">
    <label for="headline">
        <g:message code="facilityNotification.title.label"/>*
    </label>
    <g:textField class="form-control" name="headline" required="required" maxlength="255"
                 value="${facilityNotificationInstance?.headline}"/>
</div>


<div class="col-sm-12 form-group ${hasErrors(bean: facilityNotificationInstance, field: 'content', 'error')}">
    <label for="content">
        <g:message code="facilityNotification.notificationText.label"/>*
    </label>
    <g:textArea class="form-control" rows="10" name="content" required="required"
                value="${facilityNotificationInstance?.content}"/>
</div>


<div class="col-sm-6 form-group ${hasErrors(bean: facilityNotificationInstance, field: 'validFrom', 'error')}">
    <label for="validFrom">
        <g:message code="facilityNotification.publishDate.label"/>*
    </label>
    <div class="input-group">
        <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
        <input class="form-control" readonly="true" type="text" name="validFrom" id="validFrom"
               value="${formatDate(date: facilityNotificationInstance?.validFrom, formatName: 'date.format.dateOnly')}"/>
    </div>
</div>

<div class="col-sm-6 form-group ${hasErrors(bean: facilityNotificationInstance, field: 'validTo', 'error')}">
    <label for="validTo">
        <g:message code="facilityNotification.endDate.label"/>*
    </label>
    <div class="input-group">
        <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
        <input class="form-control" readonly="true" type="text" name="validTo" id="validTo"
               value="${formatDate(date: facilityNotificationInstance?.validTo, formatName: 'date.format.dateOnly')}"/>
    </div>
</div>

<r:script>
    $(function() {
        $("#content").wysihtml5({
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });

        var publishDatePicker = $("#validFrom");
        var endDatePicker = $("#validTo");
        publishDatePicker.datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}",
            minDate: new Date()
        });
        var validFrom = new Date(publishDatePicker.val());
        endDatePicker.datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}",
            minDate: new Date(validFrom.getFullYear(), validFrom.getMonth(), validFrom.getDate() + 1)
        });
        publishDatePicker.on('change', function(){
          endDatePicker.datepicker( "option", "minDate", $(this).val());
        });
    });
</r:script>
