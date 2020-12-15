<div id="scheduled-tasks" class="alert alert-info scheduled-tasks" style="display: none">
    <r:img uri="/images/spinner.gif" style="margin-right: 10px;"/><span id="scheduled-tasks-label" class="text"><g:message code="default.loader.label"/></span>
</div>
<div id="scheduled-tasks-errors" class="alert ${errorClass ?: 'alert-error'} scheduled-tasks" style="display: none"></div>
<div id="scheduled-tasks-files" class="alert alert-success scheduled-tasks" style="display: none"></div>
<div id="scheduled-tasks-success-messages" class="alert alert-success scheduled-tasks" style="display: none"></div>

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
                            errorTaskElems += '<div>' + value.text + ": " + value.error + ' <a href="' + value.closeLink +
                                    '" class="mark-as-read">${message(code: 'scheduledTask.getCurrentlyRunningTasks.markAsReadLink')}</a>' + '</div>';
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
                        $('#scheduled-tasks-label').html(taskElems);
                        $("#scheduled-tasks").slideDown("fast");
                    } else {
                        $('#scheduled-tasks').slideUp("fast");
                    }
                    if (errorTaskElems) {
                        $('#scheduled-tasks-errors').html(errorTaskElems).slideDown("fast");
                    } else {
                        $('#scheduled-tasks-errors').slideUp("fast");
                    }
                    if (fileTaskElems) {
                        $('#scheduled-tasks-files').html(fileTaskElems).slideDown("fast");
                    } else {
                        $('#scheduled-tasks-files').slideUp("fast");
                    }

                    if (successMsgTaskElems) {
                        $('#scheduled-tasks-success-messages').html(successMsgTaskElems).slideDown("fast");
                    } else {
                        $('#scheduled-tasks-success-messages').slideUp("fast");
                    }

                    //if tasks available check every 10 sec
                    setTimeout(checkCurrentTasks, 10000);
                } else {
                    $(".scheduled-tasks").slideUp("fast");
                    //if nothing check only each 120 sec
                    setTimeout(checkCurrentTasks, 120000);
                }
            }
        });
    }

    $(document).ready(function() {
        checkCurrentTasks();

        $(".scheduled-tasks").on("click", ".mark-as-read", function() {
            var el = $(this);
            var container = el.closest(".scheduled-tasks");
            $.post(el.attr("href"), function(data) {
                el.parent().remove();
                if (!container.text().trim().length) {
                    container.slideUp("fast");
                }
            });
            return false;
        });
    });
</r:script>