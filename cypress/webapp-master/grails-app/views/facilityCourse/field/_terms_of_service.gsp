<div class="row no-left-margin no-right-margin">
    <div class="col-xs-10 top-margin20">
        <g:textArea name="field.${fieldIdx}.fieldText" rows="5" class="form-control wysihtml5" rel="textarea_${fieldIdx}"
                value="${g.toRichHTML(text: field.fieldText ?: (templateField?.fieldText ?: ''))}"
                disabled="${!field.isEditable}"/>
    </div>
</div>
<script type="text/javascript">
    $(function() {
        $('[rel=textarea_${g.forJavaScript(data: fieldIdx)}]').wysihtml5({
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });
    });
</script>