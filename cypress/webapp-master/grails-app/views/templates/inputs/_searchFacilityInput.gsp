<r:require modules="bootstrap-typeahead"/>
<g:textField name="${name}" placeholder="${placeholder}" class="${classes}" value="${value}" autocomplete="off"/>
<r:script>
    $(document).ready(function() {
        $('#${g.forJavaScript(data: name)}').typeahead({
            items: 20,
            source: ${g.forJavaScript(json: source)}
        });
    });
</r:script>