<g:if test="${image}">
    <div class="row">
        <div class="col-sm-2">
            <g:fileArchiveImage file="${image}"/>
        </div>
        <div class="col-sm-6">
            <h4 class="media-heading">${image.originalFileName}</h4>
            <p><g:message code="label.created.image"/> <g:formatDate date="${image.dateCreated}" formatName="date.format"/></p>
            <a href="javascript:void(0)" id="${inputId}_link" class="btn btn-xs btn-success"><g:message code="templates.filearchive.fileArchiveImageUpload.message1"/></a>
            <g:link controller="${removeCallback.controller}" action="${removeCallback.action}" params="${parameters}" href="#" class="btn btn-xs btn-danger"><g:message code="button.delete.label"/></g:link>
        </div>
    </div>
<div class="clearfix"></div>
</g:if>
<g:else>
    <div class="media">
        <div class="media-body">
            <h4 class="media-heading"><g:message code="templates.filearchive.fileArchiveImageUpload.message3"/></h4>
            <p><g:message code="templates.filearchive.fileArchiveImageUpload.message4"/></p>
            <a href="javascript:void(0)" id="${inputId}_link" class="btn btn-xs btn-success"><g:message code="templates.filearchive.fileArchiveImageUpload.message5"/></a>
        </div>
    </div>
    <div class="clearfix"></div>
</g:else>

<div id="${inputId}" class="modal fade">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="label.upload.image"/></h4>
            </div>
            <g:uploadForm controller="${callback.controller}" action="${callback.action}" name="upload-archive-form-${inputId}" enctype="multipart/form-data">
                <g:each in="${parameters}">
                    <g:hiddenField name="${it.key}" value="${it.value}"/>
                </g:each>
                <g:hiddenField name="fileParameterName" value="file_${inputId}"/>
                <div class="modal-body">
                    <g:message code="templates.filearchive.fileArchiveImageUpload.message12"/>
                    <input id="profileImage" type="file" name="file_${inputId}" />
                </div>
                <div class="modal-footer">
                    <div class="pull-left">
                        <input type="submit" class="btn btn-md btn-success" value="${message(code: 'button.save.label')}">
                        <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label"/></a>
                    </div>
                </div>
            </g:uploadForm>
        </div>
    </div>
</div>

<r:script>

    $(document).ready(function() {
        $("#${g.forJavaScript(data: inputId)}_link").on("click", function() {
            $("#${g.forJavaScript(data: inputId)}").modal("show");
        });
    });


</r:script>