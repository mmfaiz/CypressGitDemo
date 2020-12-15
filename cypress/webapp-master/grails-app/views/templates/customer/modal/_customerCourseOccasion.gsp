<div class="modal hide fade" id="occasionModal-${occasion.id}" tabindex="-1" role="dialog" style="top: 25%;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="occasionModalLabel"><g:message code="facilityCustomer.show.courses.occasion.modal.title"/></h4>
            </div>
            <div class="modal-body">
                <div class="info-box">
                    <p class="lead header"><g:message code="trainer.label"/></p>
                    <g:set var="trainers" value="${occasion.trainers}"/>
                    <g:if test="${trainers}">
                        <ul class="nav nav-list">
                            <g:each in="${trainers}">
                                <li>${it.toString()}</li>
                            </g:each>
                        </ul>
                    </g:if>
                    <g:else>
                        <small><i><g:message code="default.noElements"/></i></small>
                    </g:else>
                </div>
                <div class="info-box">
                    <p class="lead header"><g:message code="eventActivity.participants.label"/></p>
                    <g:set var="participants" value="${occasion.participants?.findAll { it?.customer != customer }}"/>
                    <g:if test="${participants}">
                        <ul class="nav nav-list">
                            <g:each in="${participants}">
                                <li>
                                    <ul class="inline">
                                        <li>${it.customer.toString()}</li>
                                        <g:if test="${it.customer.birthyear}">
                                            <li>-${it.customer.birthyear.toString()[-2..-1]}</li>
                                        </g:if>
                                    </ul>
                                </li>
                            </g:each>
                        </ul>
                    </g:if>
                    <g:else>
                        <small><i><g:message code="default.noElements"/></i></small>
                    </g:else>
                </div>
            </div>
            <div class="modal-footer">
                <div class="pull-left">
                    <button type="button" class="btn btn-md btn-danger" data-dismiss="modal"><g:message code="button.cancel.label"/></button>
                </div>
            </div>
        </div>
    </div>
</div>