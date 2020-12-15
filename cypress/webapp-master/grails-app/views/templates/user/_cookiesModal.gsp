<div class="modal ${!bootstrap3 ? 'hide' : ''} fade" id="cookiesModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" data-keyboard="false" data-backdrop="static">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel">
                    <g:message code="auth.login.nocookies.title"/>
                </h4>
            </div>
            <div class="modal-body" ${!bootstrap3 ? 'style="max-height: 400px;"' : ''}>
                <g:message code="auth.login.nocookies.message"/>
            </div>
            <div class="modal-footer"></div>
        </div>
    </div>
</div>
