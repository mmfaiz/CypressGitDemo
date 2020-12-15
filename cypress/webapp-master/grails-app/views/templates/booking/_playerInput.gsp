<div class="form-group player-row">
    <div class="row">
        <div class="col-sm-9 col-xs-9">
            <input type="text" name="playerEmailLabel" value="${customerName ?: emailValue}" maxlength="255"
                    class="form-control" placeholder="${message(code: 'default.email.label')}"
                    autocomplete="off" ${customerName ? 'readonly' : ''}/>
            <g:hiddenField name="playerEmail" value="${emailValue}"/>
        </div>
        <div class="col-sm-3 col-xs-3">
            <a href="javascript: void(0)" class="btn btn-link text-danger delete-player-row">
                <i class="ti-close"></i>
            </a>
        </div>
    </div>
</div>