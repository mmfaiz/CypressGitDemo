<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="facilityForm.create.title"/></title>
    <r:script>
        $(function() {
            $("#relatedFormTemplate\\.id").change(function() {
                if ($(this).val()) {
                    $("#form-fields").load("${g.forJavaScript(data: createLink(action: 'loadTemplate'))}/" + $(this).val());
                    $("#name").val($(this).find("option:selected").text());
                }
            });
            $("[rel=tooltip]").tooltip({ delay: { show: 1000, hide: 100 } });
            $("#selectFormTemplateSelector").selectpicker();
        });
    </r:script>
</head>

<body>
    <div class="container content-container">

        <ol class="breadcrumb">
            <li><i class=" ti-write"></i> <g:link action="index"><g:message code="form.label.plural"/></g:link></li>
            <li class="active"><g:message code="facilityForm.create.title"/></li>
        </ol>

        <g:b3StaticErrorMessage bean="${formInstance}"/>

        <span class="block text-muted top-margin5"><g:message code="default.form.create.instructions"/></span>
        <hr/>
        <g:form action="save" class="form vertical-margin10">
            <div class="row">
                <div class="col-sm-6">

                    <!-- SELECT FORM -->
                    <g:if test="${formTemplates}">
                        <div class="form-group ${hasErrors(bean: formInstance, field: 'relatedFormTemplate', 'has-error')}">
                            <label><g:message code="form.relatedFormTemplate.label"/></label>
                            <g:select id="selectFormTemplateSelector" from="${formTemplates}" name="relatedFormTemplate.id" value="${formInstance?.relatedFormTemplate?.id}"
                                      optionKey="id" optionValue="name"
                                      title="${message(code: 'form.relatedFormTemplate.label')}"/>

                            <!-- <g:select class="form-control" name="relatedFormTemplate.id" from="${formTemplates}" optionKey="id" optionValue="name"
                            value="${formInstance?.relatedFormTemplate?.id}" noSelection="['': '']"/> -->
                        </div>
                    </g:if>
                </div>

                <div class="col-sm-6">
                </div>

            </div><!-- /.row -->

            <!-- LOAD FORM -->
            <g:render template="form"/>

            <!-- SAVE/ CANCEL BUTTONS -->
            <div class="top-margin20 text-right">
                <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                <g:link action="index" class="btn btn-danger"><i class="ti-close"></i> <g:message code="button.cancel.label"/></g:link>
            </div>

        </g:form>
    </div><!-- /.container -->

</body>
</html>
