<!doctype html>
<html>
<head>
    <meta name="layout" content="facilityLayout">
    <title><g:message code="facilityCourse.copy.title"/></title>
    <r:script>
        $(function() {
            $("#showStartDate").datepicker({
                autoSize: true,
                dateFormat: '${message(code: 'date.format.dateOnly.small')}',
                altField: '#startDate',
                altFormat: 'yy-mm-dd'
            });

            $("#showEndDate").datepicker({
                autoSize: true,
                dateFormat: '${message(code: 'date.format.dateOnly.small')}',
                altField: '#endDate',
                altFormat: 'yy-mm-dd'
            });

            $("#showActiveFrom").datepicker({
                autoSize: true,
                dateFormat: '${message(code: 'date.format.dateOnly.small')}',
                altField: '#activeFrom',
                altFormat: 'yy-mm-dd'
            });

            $("#showActiveTo").datepicker({
                autoSize: true,
                dateFormat: '${message(code: 'date.format.dateOnly.small')}',
                altField: '#activeTo',
                altFormat: 'yy-mm-dd'
            });
        });
    </r:script>
</head>

<body>
<g:render template="copy/breadcrumb" model="[wizardStep: 0]"/>

<h3><g:message code="facilityCourse.copy.step1.title"/></h3>
<p class="lead"><g:message code="facilityCourse.copy.step1.desc"/></p>

<g:form class="form-horizontal">
    <g:hiddenField name="srcCourseId" value="${cmd?.srcCourseId ?: params.id}"/>

    <div class="well">
        <div class="control-group">
            <label class="control-label" for="name">
                <g:message code="course.name.label"/>*
            </label>
            <div class="controls">
                <g:textField name="name" value="${cmd?.name}" class="span8"/>
            </div>
        </div>
        <div class="control-group form-inline">
            <label class="control-label" for="showStartDate">
                <g:message code="course.startDate.label"/>*
            </label>
            <div class="controls controls-row">
                <g:textField name="showStartDate" class="span2 center-text" readonly="true"
                             value="${formatDate(date: cmd?.startDate, formatName: 'date.format.dateOnly')}"/>
                <g:hiddenField name="startDate"
                               value="${formatDate(date: cmd?.startDate, format: 'yyyy-MM-dd')}"/>
                <label class="span1 control-label left-margin15" for="showEndDate" style="width: auto">
                    <g:message code="default.to.label"/>*
                </label>
                <g:textField name="showEndDate" class="span2 center-text" readonly="true"
                             value="${formatDate(date: cmd?.endDate, formatName: 'date.format.dateOnly')}"/>
                <g:hiddenField name="endDate"
                               value="${formatDate(date: cmd?.endDate, format: 'yyyy-MM-dd')}"/>
            </div>
        </div>
        <div class="control-group form-inline">
            <label class="control-label" for="showActiveFrom">
                <g:message code="course.form.label"/> <g:message code="default.from.label"/>*
            </label>
            <div class="controls controls-row">
                <g:textField name="showActiveFrom" class="span2 center-text" readonly="true"
                             value="${formatDate(date: cmd?.activeFrom, formatName: 'date.format.dateOnly')}"/>
                <g:hiddenField name="activeFrom"
                               value="${formatDate(date: cmd?.activeFrom, format: 'yyyy-MM-dd')}"/>
                <label class="span1 control-label left-margin15" for="showActiveTo" style="width: auto">
                    <g:message code="default.to.label"/>*
                </label>
                <g:textField name="showActiveTo" class="span2 center-text" readonly="true"
                             value="${formatDate(date: cmd?.activeTo, formatName: 'date.format.dateOnly')}"/>
                <g:hiddenField name="activeTo"
                               value="${formatDate(date: cmd?.activeTo, format: 'yyyy-MM-dd')}"/>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <label class="checkbox">
                    <g:checkBox name="copyTrainers" value="${cmd?.copyTrainers}"/>
                    <g:message code="copyCourseCommand.copyTrainers.label"/>
                </label>
                <label class="checkbox">
                    <g:checkBox name="copySettings" value="${cmd?.copySettings}"/>
                    <g:message code="copyCourseCommand.copySettings.label"/>
                </label>
                <label class="checkbox">
                    <g:checkBox name="copyParticipants" value="${cmd?.copyParticipants}"/>
                    <g:message code="copyCourseCommand.copyParticipants.label"/>
                </label>
                <label class="checkbox">
                    <g:checkBox name="copyWaitingSubmissions" value="${cmd?.copyWaitingSubmissions}"/>
                    <g:message code="copyCourseCommand.copyWaitingSubmissions.label"/>
                </label>
                <label class="checkbox">
                    <g:checkBox name="copyOccasions" value="${cmd?.copyOccasions}"/>
                    <g:message code="copyCourseCommand.copyOccasions.label"/>
                </label>
            </div>
        </div>
    </div>

    <div class="form-actions">
        <g:submitButton name="submit" type="submit" class="btn btn-success pull-right"
                        value="${message(code: 'button.next.label')}" data-toggle="button"
                        show-loader="${message(code: 'default.loader.label')}"/>
        <g:link event="cancel" class="btn btn-danger pull-right" style="margin-right: 5px;">
            <g:message code="button.cancel.label"/>
        </g:link>
    </div>
</g:form>
</body>
</html>
