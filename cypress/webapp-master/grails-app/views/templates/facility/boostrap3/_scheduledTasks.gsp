<div id="scheduled-tasks" class="alert alert-info alert-notification" style="display: none">
    <table class="notification-content">
        <tr>
            <td class="notification-icon" width="10%">
                <i class="fas fa-spinner fa-spin"></i>
            </td>
            <td class="notification-message" width="80%">
                <span id="scheduled-tasks-label"><g:message code="default.loader.label"/></span>
            </td>
            <td class="notification-close" width="10%"></td>
        </tr>
    </table>
</div>
<div id="scheduled-tasks-errors" class="alert alert-notification ${errorClass ?: 'alert-error'} alert-dismissible" style="display: none">
    <table class="notification-content">
        <tr>
            <td class="notification-icon" width="10%">
                <i class="fas fa-bullhorn"></i>
            </td>
            <td class="notification-message" width="80%">

            </td>
            <td class="notification-close" width="10%">
                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            </td>
        </tr>
    </table>
</div>
<div id="scheduled-tasks-files" class="alert alert-success alert-notification alert-dismissible" style="display: none">
    <table class="notification-content">
        <tr>
            <td class="notification-icon" width="10%">
                <i class="fas fa-bullhorn"></i>
            </td>
            <td class="notification-message" width="80%">

            </td>
            <td class="notification-close" width="10%">
                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            </td>
        </tr>
    </table>
</div>
<div id="scheduled-tasks-success-messages" class="alert alert-success alert-notification alert-dismissible" style="display: none">
    <table class="notification-content">
        <tr>
            <td class="notification-icon" width="10%">
                <i class="fas fa-bullhorn"></i>
            </td>
            <td class="notification-message" width="80%">
            </td>
            <td class="notification-close" width="10%">
                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            </td>
        </tr>
    </table>
</div>

<r:script>
    function checkCurrentTasks() {
        $.ajax({
            url: "${g.forJavaScript(data: createLink(controller: 'scheduledTask', action: 'getCurrentlyRunningTasks'))}",
            method: 'GET',
            cache: false,
            success: function (tasks) {
                if (tasks.length > 0) {
                    var taskElems = "";
                    var errorTaskElems = "";
                    var fileTaskElems = "";
                    var successMsgTaskElems = "";

                    $.each(tasks, function( index, value ) {
                        if(value.error){
                            errorTaskElems += value.text + " " + value.error + "<br/>"
                        } else if (value.file) {
                            fileTaskElems += value.text + "<br/>";
                        } else if (value.successMessage) {
                            successMsgTaskElems += '<div>' + value.text + ' <a href="' + value.closeLink +
                                    '" class="mark-as-read">${message(code: 'scheduledTask.getCurrentlyRunningTasks.markAsReadLink')}</a>' + '</div>';
                        } else {
                            taskElems += value.text + "<br/>"
                        }
                    });
                    if (taskElems) {
                        $('#scheduled-tasks').slideDown("fast").find('.notification-message').html(taskElems);
                    } else {
                        $('#scheduled-tasks').slideUp("fast");
                    }
                    if (errorTaskElems) {
                        $('#scheduled-tasks-errors').slideDown("fast").find('.notification-message').html(errorTaskElems);
                    } else {
                        $('#scheduled-tasks-errors').slideUp("fast");
                    }
                    if (fileTaskElems) {
                        $('#scheduled-tasks-files').slideDown("fast").find('.notification-message').html(fileTaskElems);
                    } else {
                        $('#scheduled-tasks-files').slideUp("fast");
                    }

                    if (successMsgTaskElems) {
                        $('#scheduled-tasks-success-messages').slideDown("fast").find('.notification-message').html(successMsgTaskElems);
                    } else {
                        $('#scheduled-tasks-success-messages').slideUp("fast");
                    }

                    //if tasks available check every 10 sec
                    setTimeout(checkCurrentTasks, 10000);
                } else {
                    $("#scheduled-tasks").slideUp("fast");
                    //if nothing check only each 120 sec
                    setTimeout(checkCurrentTasks, 120000);
                }
            }
        });
    }

    $(document).ready(function() {
        checkCurrentTasks();

        $(".alert-notification").on("click", ".mark-as-read", function() {
            var el = $(this);
            var container = el.closest(".alert-notification");
            $.post(el.attr("href"), function(data) {
                el.parent().remove();
                if (!container.find(".notification-message").text().trim().length) {
                    container.slideUp("fast");
                }
            });
            return false;
        });
    });
</r:script>