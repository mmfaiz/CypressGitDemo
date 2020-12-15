<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <h4 class="modal-title" id="addOccasionModalLabel"><g:message code="course.planning.occasion.add"/></h4>
</div>
<g:formRemote name="addOccasion" class="form" role="form" url="[action: 'saveOccasion']" method="POST" onSuccess="refreshOccasions(data)">
    <g:hiddenField name="date" value=""/>
    <div class="modal-body">
        <div class="row">
            <div class="form-group col-sm-6">
                <label><g:message code="course.label"/></label>
                <g:select from="${activeCourses}" name="course" optionKey="id" optionValue="name" noSelection="['': message(code: 'facilityCourseParticipant.index.court.noneSelectedText')]"/>
            </div>
            <div class="form-group col-sm-6">
                <label><g:message code="trainer.label"/></label><br>
                <g:select from="${trainers}" name="trainer" optionKey="id" optionValue="firstName" noSelection="['': message(code: 'facilityCourseParticipant.index.trainers.noneSelectedText')]"/>
            </div>
        </div>
        <div class="row">
            <div class="form-group col-sm-6">
                <label><g:message code="facilityCourse.planning.message6"/></label>
                <g:select from="${timeRange}" name="startTime" class="form-control"/>
            </div>
            <div class="form-group col-sm-6">
                <label><g:message code="facilityCourse.planning.message7"/></label>
                <g:select from="${timeRange}" name="endTime" class="form-control"/>
            </div>
        </div>
        <div class="row">
            <div class="form-group col-sm-6">
                <label><g:message code="court.label"/></label>
                <g:select from="${com.matchi.activities.trainingplanner.TrainingCourt.findAllByFacility(facility)}" name="courtId" optionKey="id" optionValue="name"/>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-md btn-default" data-dismiss="modal"><g:message code="button.cancel.label"/></button>
        <g:submitButton name="submit" class="btn btn-md btn-success" value="${message(code: "button.save.label")}"/>
    </div>
</g:formRemote>