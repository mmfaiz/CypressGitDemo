<div class="btn-toolbar pull-right" style="margin: 0 10px 0 10px;">
    <div class="btn-group hidden-fullscreen" style="white-space:nowrap;">
        <g:link action="index"  class="btn" params="[date: g.formatDate(format: 'yyyy-MM-dd', date: date.toDate())]"><i class="icon-th"></i></g:link>
        <g:link action="list" class="btn" params="[date: g.formatDate(format: 'yyyy-MM-dd', date: date.toDate())]"><i class="icon-list"></i></g:link>
    </div>
    <div class="btn-group hidden-fullscreen" style="white-space:nowrap;">
        <a href="javascript:void(0)" onclick="printPage()" class="btn"><i class="icon-print"></i></a>
    </div>

    <g:if test="${fullscreen}">
        <a href="javascript: void(0)"  class="btn btn-fullscreen"
                title="${message(code: 'button.fullscreen.label')}">
            <i class="icon-fullscreen"></i>
        </a>
        <a href="javascript: void(0)"  class="btn btn-fullscreen-exit"
                title="${message(code: 'button.fullscreen.exit.label')}">
            <i class="icon-resize-small"></i>
        </a>

        <r:script>
            $(function() {
                $(".btn-fullscreen").on("click", function() {
                    $("#facility-schedule").fullScreen(true);
                });

                $(".btn-fullscreen-exit").on("click", function() {
                    $("#facility-schedule").fullScreen(false);
                });
            });
        </r:script>
    </g:if>
</div>

<r:script>
    function printPage() {
        if($(".booking-list").length > 0) {
            $(".booking-list").printArea();
        } else {
            window.print();
        }
    }
</r:script>