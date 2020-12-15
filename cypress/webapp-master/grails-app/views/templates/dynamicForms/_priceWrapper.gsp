<div class="row">
    <div class="col-sm-12">
        <div class="form-group">
            <g:checkBox name="form.paymentRequired" id="paymentRequired"
                        value="${paymentRequired ?: false}"/>
            <label for="paymentRequired" class="left-margin20"><g:message code="course.paymentRequired.label"/></label>
        </div>
    </div>
</div>
<div id="price-wrapper" class="row" style="${paymentRequired ? '' : 'display: none'}">
    <div class="col-sm-3">
        <div class="form-group">
            <label for="form.price"><g:message code="form.price.label"/></label>
            <input type="number" id="form.price" name="form.price" min="1" value="${price?.encodeAsHTML()}"
                   maxlength="9" class="form-control" ${paymentRequired ? 'required' : ''}/>
        </div>
    </div>
</div>
