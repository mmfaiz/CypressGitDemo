<div class="modal-loader">
    <p>
        <span id="modal-loader-text"><g:message code="default.loader.text"/></span><br/>
        <i class="fa fa-spin fa-spinner"></i>
    </p>
</div>
<r:script>
    var $timeOut = 0;

    $(document).ready(function() {
        stopLoading();
    });

    function onLoading(/*Element*/ obj) {
        stopLoading();
        $(obj).attr("disabled", "disabled");
        var $modalLoader = $('.modal-loader');
        $(obj).closest('.modal-content').append($modalLoader);
        $modalLoader.show();

        $timeOut = setTimeout(function () {
            $('#modal-loader-text').text('<g:message code="default.load.more.text"/>')
        }, 10000);
    }

    function stopLoading() {
        $('.modal-loader').hide();
        window.clearTimeout($timeOut);
        $('#modal-loader-text').text('<g:message code="default.loader.text"/>')
    }
</r:script>