<%@ page import="com.matchi.LocaleHelper.Country; com.matchi.FrontEndMessage; com.matchi.admin.AdminFrontEndMessageController" %>
<r:require modules="bootstrap3-wysiwyg"/>

<div>

<div class="panel-body">
    <h3>How to use</h3>
    <p>A message relies on three inputs: html-code, css-code and images. That means those images this will be edited be front end developer.</p>
    <p>Images can be uploaded after one message save and will be stored with a unique name to be inserted in the html block.</p>
    <p>The css editor works with normal vanilla css but should be covered with the message instance's baseId found next to the editor. Otherwise this css might conflict with entire matchi making it a very powerful tool - don't misuse.</p>
    <p>The HTML-editor handles different languages and images and of course native HTML. Se information below editor on how to use.</p>
    <p>Country setting decides for which users the message should be displayed. The country is, until better solution exists, determined by user language. If a user set english as language, the banner will always be shown.</p>
</div>

<g:if test="${FrontEndMessage.exists(frontEndMessageInstance.id)}">
    <g:if test="${frontEndMessageInstance.imagesList}">
        <div class="panel-body">
            <h3>Images</h3>
            <g:each in="${frontEndMessageInstance.imagesList}" var="imageObj">
                <g:set var="image" value="${imageObj.value}" />
                <g:set var="name" value="${imageObj.key}" />
                <div class="media">
                    <a class="pull-left" href="#">
                        <div style="width: 100px;height: 60px">
                            <g:fileArchiveImage file="${image}"/>
                        </div>
                    </a>
                    <div class="media-body">
                        <h4 class="media-heading">${name}</h4>
                        <p><g:message code="label.created.image"/> <g:formatDate date="${image.dateCreated}" formatName="date.format"/></p>
                        <g:link controller="adminFrontEndMessage" action="removeImage" params="${[imageId: image.id, id: frontEndMessageInstance.id]}" href="#" class="btn btn-xs btn-danger image-modifier"><g:message code="button.delete.label"/></g:link>
                    </div>
                </div>
                <div class="clearfix"></div>
            </g:each>
        </div>
    </g:if>

    <div class="modal-header">
        <h3><g:message code="label.upload.image"/></h3>
        <div class="clearfix"></div>
    </div>
    <g:uploadForm action="uploadImage" name="upload-archive-form-${inputId}" enctype="multipart/form-data">
        <g:hiddenField name="id" value="${frontEndMessageInstance?.id}"/>
        <div class="modal-body">
            <g:message code="templates.filearchive.fileArchiveImageUpload.message12"/>
            <input id="profileImage" type="file" name="file" />
        </div>
        <div class="modal-footer">
            <div class="pull-left">
                <input type="submit" class="btn btn-md btn-success image-modifier" value="${message(code: 'button.upload.label')}">
            </div>
        </div>
    </g:uploadForm>
</g:if>
<g:else>
    <div class="panel-body">
        <g:message code="frontEndMessage.saveBeforeImages.description"/>
    </div>
</g:else>

<g:form action="${method}" class="form panel-body contentForm">

<g:if test="${method=="update"}">
    <g:hiddenField name="id" value="${frontEndMessageInstance?.id}"/>
    <g:hiddenField name="version" value="${frontEndMessageInstance?.version}"/>
</g:if>

<div class="front-end-message">
    <div class="col-sm-12 form-group ${hasErrors(bean: frontEndMessageInstance, field: 'name', 'error')}">
        <label for="name">
            <g:message code="frontEndMessage.name.label"/>*
        </label>
        <div class="controls">
            <g:textField class="form-control" name="name" required="required"
                         value="${frontEndMessageInstance?.name}"/>
        </div>
    </div>


    <div class="col-sm-12 form-group ${hasErrors(bean: frontEndMessageInstance, field: 'notificationText', 'error')}">
        <label for="htmlContent">
            <g:message code="frontEndMessage.html.label"/>
        </label>
        <div class="controls">
            <g:textArea class="code-block html" name="htmlContent" value="${frontEndMessageInstance?.htmlContent}" />
        </div>
<pre>
Useful input:
.container - to center content
.fem-collapse - visible without toggle
.fem-expand - visible after toggle

&lt;a class="fem-toggle-collapse" href="#"&gt; - to toggle expand-mode, must be used if .fem-collapse and .fem-expand is used.

use {image=padelcupen-logo.png} to insert images
use {text[se,no]=Swedish or norwegian text} or
{text[!se,no]=Non swedish or norwegian text}
Suppoerted languages: ${Country.list().collect{ it.languages.join(", ") + " (" + it.toString() + ")"}.join(" - ")}
+ en
</pre>
    </div>

    <div class="col-sm-12 form-group ${hasErrors(bean: frontEndMessageInstance, field: 'notificationText', 'error')}">
        <label for="cssCode">
            <g:message code="frontEndMessage.css.label"/>
        </label>
        <div class="controls">
            <g:textArea class="code-block css" name="cssCode" value="${frontEndMessageInstance?.cssCode}" />
        </div>
<pre>
Prepend all your containers with #${frontEndMessageInstance.baseId} to avoid messing with other styles. e.g:
#${frontEndMessageInstance.baseId} .container { background: black; }

#${frontEndMessageInstance.baseId} .open can be used to target when message is expanded
</pre>
    </div>


    <div class="col-sm-12 form-group ${hasErrors(bean: frontEndMessageInstance, field: 'publishDate', 'error')}">
        <label for="countries">
            <g:message code="frontEndMessage.country.label"/>
        </label>
        <div class="controls">
            <div class="input-group">
                <g:select from="${com.matchi.LocaleHelper.Country.list()}" style="height: 150px;" class="form-control" name="countries"
                       value="${frontEndMessageInstance?.countries ?: []}"/>
            </div>
        </div>
    </div>

    <div class="col-sm-6 form-group ${hasErrors(bean: frontEndMessageInstance, field: 'publishDate', 'error')}">
        <label for="publishDate">
            <g:message code="frontEndMessage.publishDate.label"/>*
        </label>
        <div class="controls">
            <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                <g:textField class="form-control" readonly="true" type="text" name="publishDate" id="publishDate" required="required"
                       value="${formatDate(date: frontEndMessageInstance?.publishDate, formatName: 'date.format.dateOnly')}"/>
            </div>
        </div>
    </div>

    <div class="col-sm-6 form-group ${hasErrors(bean: frontEndMessageInstance, field: 'endDate', 'error')}">
        <label for="endDate">
            <g:message code="frontEndMessage.endDate.label"/>*
        </label>
        <div class="controls">
            <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                <g:textField class="form-control" readonly="true" type="text" name="endDate" id="endDate" required="required"
                       value="${formatDate(date: frontEndMessageInstance?.endDate, formatName: 'date.format.dateOnly')}"/>
            </div>
        </div>
    </div>
</div>

    <div class="col-sm-12 vertical-margin40 text-right">
        <g:link action="index" class="btn btn-default"><g:message code="button.cancel.label"/></g:link>
        <g:if test="${method=="update"}">
            <g:actionSubmit action="delete" value="${message(code: 'button.delete.label')}" class="btn btn-danger"
                        onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"/>
        </g:if>
        <g:submitButton name="${method}" value="${message(code: 'button.'+method+'.label')}" class="btn btn-success"/>
    </div>

</g:form>
</div>
<r:script>
    var wysihtml5Stylesheets = "${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}";
    var changed = false

    $(function() {

        $(document).delegate('.code-block', 'keydown', function(e) {
            var keyCode = e.keyCode || e.which;

            if (keyCode == 9) {
                e.preventDefault();
                var start = this.selectionStart;
                var end = this.selectionEnd;

                // set textarea value to: text before caret + tab + text after caret
                $(this).val($(this).val().substring(0, start)
                            + "\t"
                            + $(this).val().substring(end));

                // put caret at right position again
                this.selectionStart = this.selectionEnd = start + 1;
            }
        });

        var publishDatePicker = $("#publishDate");
        var endDatePicker = $("#endDate");
        publishDatePicker.datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}",
            minDate: new Date()
        });
        var publishDate = new Date(publishDatePicker.val());
        endDatePicker.datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}",
            minDate: new Date(publishDate.getFullYear(), publishDate.getMonth(), publishDate.getDate() + 1)
        });
        publishDatePicker.on('change', function(){
          endDatePicker.datepicker( "option", "minDate", new Date($(this).val()));
        });

        $('.contentForm').on('keyup change paste', 'input, select, textarea', function(){
            console.log('Form changed!');
            if (!changed) {
                changed = true
                $('.image-modifier').click(function() {
                    alert("please save changes before this action");
                    return false;
                })
            }
        });

        $("#uploadImage").submit(function() {
            $.ajax({
                type: "POST",
                data: $(this).serialize(),
                url: "${createLink(controller: 'AdminFrontEndMessage', action: 'uploadImage')}",
                success: function (data) {
                    console.log("success");
                    console.log(data);
                },
                error: function (jqXHR) {
                    console.log("error")
                    console.log(jqXHR)
                }
            });
            return false;
        });
  })
</r:script>
