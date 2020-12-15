<div class="row">
    <div class="col-sm-6 form-group ${hasErrors(bean: trainerInstance, field: 'firstName', 'error')} required">
        <label for="firstName"><g:message code="trainer.firstName.label"/>*</label>
        <g:textField class="form-control" name="firstName" value="${trainerInstance?.firstName}" />
    </div>
    <div class="col-sm-6 form-group ${hasErrors(bean: trainerInstance, field: 'lastName', 'error')} required">
        <label for="lastName"><g:message code="trainer.lastName.label"/>*</label>
        <g:textField class="form-control" name="lastName" value="${trainerInstance?.lastName}" />
    </div>
</div>

<div class="row">
    <div class="col-sm-6 form-group ${hasErrors(bean: trainerInstance, field: 'phone', 'error')}">
        <label for="phone"><g:message code="trainer.phone.label"/></label>
        <g:textField class="form-control" name="phone" value="${trainerInstance?.phone}" />
    </div>
    <div class="col-sm-6 form-group ${hasErrors(bean: trainerInstance, field: 'email', 'error')}">
        <label for="email"><g:message code="trainer.email.label"/></label>
        <g:textField class="form-control" name="email" value="${trainerInstance?.email}" />
    </div>
</div>

<div class="row">
    <div class="col-sm-6 form-group ${hasErrors(bean: trainerInstance, field: 'sport', 'error')} required">
        <label for="sport"><g:message code="trainer.sport.label"/>*</label>
        <g:select class="form-control many-to-one" id="sport" name="sport.id" from="${com.matchi.Sport.coreSportAndOther.list()}"
                  optionKey="id" optionValue="${ {g.message(code:'sport.name.'+it.id) } }" required="" value="${trainerInstance?.sport?.id}" />
    </div>
    <div class="col-sm-6 form-group ${hasErrors(bean: trainerInstance, field: 'profileImage', 'error')}">
        <label for="profileImage"><g:message code="trainer.profileImage.label"/></label>
        <input id="profileImage" type="file" name="profileImage"/>
        <g:fileArchiveAdminPreviewImage file="${trainerInstance.profileImage}" deleteAction="deleteImage" parameters="[id: trainerInstance.id]" class="form-control"/>
    </div>
</div>

<div class="space-40 clearfix"></div>

<div class="row">
    <div class="col-sm-12 form-group ${hasErrors(bean: trainerInstance, field: 'description', 'error')} ">
        <label for="description"><g:message code="trainer.description.label"/></label>
        <g:textArea class="form-control" rows="10" name="description" value="${trainerInstance?.description}" />
    </div>
</div>


<!-- When using sync to IdrottOnline all trainers need to have a customer reference which is synced -->
<g:if test="${facility?.hasIdrottOnlineActivitySync() || facility?.hasBookATrainer()}">
    <div class="row">
        <div class="col-sm-6 form-group">
            <label for="customerSearch"><g:message code="customer.label"/></label><br>
            <input type="hidden" id="customerSearch" name="customer" class="required" value="${trainerInstance?.customer?.id}" />
            <g:if test="${facility?.hasIdrottOnlineActivitySync()}">
                <p class="help-block"><g:message code="trainer.customer.help.iol"/></p>
            </g:if>
            <g:if test="${facility?.hasBookATrainer()}">
                <p class="help-block"><g:message code="trainer.customer.help.trainer"/></p>
            </g:if>
        </div>
        <div class="col-sm-6 form-group">

        </div>
    </div>
</g:if>

<div class="form-group ${hasErrors(bean: trainerInstance, field: 'isActive', 'error')}">
    <label for="isActive"><g:message code="trainer.isActive.label"/></label><br>
    <input id="isActive" type="checkbox" name="isActive" value="${trainerInstance?.isActive}" ${trainerInstance?.isActive ? 'checked':''}>
    <p class="help-block"><g:message code="trainer.isActive.help"/></p>
</div>

<div class="form-group ${hasErrors(bean: trainerInstance, field: 'showOnline', 'error')}">
    <label for="showOnline"><g:message code="trainer.showOnline.label"/></label><br>
    <g:checkBox name="showOnline" value="${trainerInstance?.showOnline}"/>
    <p class="help-block"><g:message code="trainer.showOnline.help"/></p>
</div>

<g:if test="${facility?.hasBookATrainer()}">
    <div class="form-group ${hasErrors(bean: trainerInstance, field: 'isBookable', 'error')}">
        <label for="isBookable"><g:message code="trainer.bookable.label"/></label><br>
        <g:checkBox name="isBookable" value="${trainerInstance?.isBookable}"/>
        <p class="help-block"><g:message code="trainer.bookable.help"/></p>
    </div>
</g:if>

<g:render template="../templates/trainer/availability" model="[ trainer: trainerInstance ]"/>

<div class="space-40 clearfix"></div>
<r:script>

    var $customerSearch;

    $(document).ready(function() {
        $('#description').wysihtml5({
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });

        $('#sport').selectpicker({
            title: '<g:message code="default.choose.sport"/>'
        });
        $('.selectPicker:visible').selectpicker();

        $('[name="isActive"],[name="showOnline"],[name="isBookable"]').bootstrapSwitch({
            onColor: 'success',
            onSwitchChange: function(event, state) {
                $(this).val(state);

                if ($(this).prop('name') === 'isBookable') {
                    console.log(state);
                    if (state) {
                        $('#trainer-availabilities').show();
                    } else {
                        $('#trainer-availabilities').hide();
                    }
                }
            }
        });

        $customerSearch = $('#customerSearch').matchiCustomerSelect({ width:'100%' });
    });
</r:script>
