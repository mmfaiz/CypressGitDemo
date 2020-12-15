<div id="matchableNotification" class="modal fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">&times;</button>
        <h3><g:message code="templates.matching.notify.message1"/></h3>
        <div class="clearfix"></div>
    </div>
    <div class="modal-body">
        <h3 style="color: inherit;"><g:message code="templates.matching.notify.message2"/></h3>
        <p>
            <g:message code="templates.matching.notify.message5"/><br>
            <g:message code="templates.matching.notify.message6"/><br><br>
            <g:message code="templates.matching.notify.message7"/>
        </p>
    </div>
    <div class="modal-footer">
        <div class="pull-right">
            <a href="javascript:void(0)" data-dismiss="modal" style="color: #999;text-decoration: underline;font-size: 12px;margin-right: 15px;"><g:message code="templates.matching.notify.message3"/></a>
            <g:link controller="userProfile" action="updateMatchable" class="btn btn-md btn-success"><g:message code="templates.matching.notify.message4"/></g:link>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(document).ready(function() {
        $("#matchableNotification").modal();
        var now = new Date();
        now.setYear(now.getFullYear() + 1);
        setCookie("${g.forJavaScript(data: cookieName)}", "true", now.getFullYear().toString(), now.getMonth().toString(), now.getDay().toString());
    });

    function close() {
        $('#matchableNotification').modal('hide');
    }
</script>