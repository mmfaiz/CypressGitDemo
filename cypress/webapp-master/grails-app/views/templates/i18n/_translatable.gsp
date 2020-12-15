<div class="translatable-wrapper">
    <g:if test="${translatable?.translations}">
        <g:each in="${translatable.translations}">
            <g:render template="/templates/i18n/translatableTextArea"
                    model="[propertyName: propertyName, translation: it]"/>
        </g:each>
    </g:if>
    <g:else>
        <g:render template="/templates/i18n/translatableTextArea"
                model="[propertyName: propertyName]"/>
    </g:else>
</div>

<div>
    <a href="javascript: void(0)" class="btn btn-sm btn-success pull-right add-translatable">
        <i class="ti-plus"></i> ${addBtnLabel}
    </a>
</div>