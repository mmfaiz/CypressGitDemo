<div class="translatable-field bottom-margin40">
    <div class="row">
        <div class="col-sm-6 form-group">
            <g:select name="language" from="${grailsApplication.config.i18n.availableLanguages}"
                    optionKey="key" optionValue="value" value="${translation?.key ?: 'en'}"
                    class="form-control"/>
        </div>
        <div class="col-sm-6 form-group">
            <a href="javascript: void(0)" class="btn btn-link text-danger pull-right remove-translatable">
                <i class="ti-close"></i>
                <g:message code="button.delete.label"/>
            </a>
        </div>
    </div>

    <div class="textarea-wrapper">
        <g:textArea name="${propertyName}.translations[${translation?.key ?: 'en'}]"
                value="${translation?.value}" class="form-control" rows="10"/>
    </div>
</div>