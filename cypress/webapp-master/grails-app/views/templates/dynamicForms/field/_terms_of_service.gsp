<div class="block">
    <div class="form-group col-sm-12">
        <div class="checkbox checkbox-success checkbox-lg">
            <input type="checkbox" id="${formField.id}" name="${formField.id}" value="true"/>
            <label for="${formField.id}">
                ${formField.label.encodeAsHTML()}
                <a href="javascript: void(0)" data-toggle="modal"
                   data-target="#termsModal${formField.id}"><g:message code="form.show.openLinkText"/></a>
            </label>
        </div>
    </div>
</div>

<div class="modal fade" id="termsModal${formField.id}" tabindex="-1" role="dialog"
     aria-labelledby="termsModalLabel${formField.id}" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="termsModalLabel${formField.id}"><g:message code="formField.type.TERMS_OF_SERVICE"/></h4>
            </div>
            <div class="modal-body">
                ${g.toRichHTML(text: formField.fieldText)}
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-md btn-default" data-dismiss="modal"><g:message code="button.close.label"/></button>
            </div>
        </div>
    </div>
</div>
