<!-- Modal for accepting consent -->
<div class="modal ${!bootstrap3 ? 'hide' : ''} fade" id="acceptConsentModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" data-keyboard="false" data-backdrop="static">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <g:if test="${!existingUserMode}">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                </g:if>
                <h4 class="modal-title" id="myModalLabel">
                    <g:if test="${existingUserMode}"><g:message code="updatedTerms.header"/></g:if>
                    <g:else><g:message code="userRegistration.consent.header"/></g:else>
                </h4>
            </div>
            <div class="modal-body" ${!bootstrap3 ? 'style="max-height: 400px;"' : ''}>

                <g:if test="${existingUserMode}">
                    <!-- Existing user, approve new terms -->
                    <p><b><g:message code="updatedTerms.message1"/></b></p>

                    <p><g:message code="updatedTerms.message2"/></p>

                    <p><g:message code="updatedTerms.message3"/></p>

                    <p><g:message code="updatedTerms.message4"/></p>

                    <p><g:message code="updatedTerms.message5"/></p>

                    <p><g:message code="updatedTerms.message6"/></p>

                    <ul>
                        <li><p><g:message code="home.integritypolicy.message1b"/></p></li>
                        <li><p><g:message code="home.integritypolicy.message1c"/></p></li>
                        <li><p><g:message code="home.integritypolicy.message1d"/></p></li>
                        <li><p><g:message code="home.integritypolicy.message1e"/></p></li>
                    </ul>

                </g:if>
                <g:else>
                    <!-- New users, accept consent -->
                    <p><g:message code="userRegistration.consent.message1"/></p>

                    <p><g:message code="userRegistration.consent.message2"/></p>

                    <p><g:message code="userRegistration.consent.message3"/></p>
                    <ul>
                        <li><g:message code="userRegistration.consent.message3bullet1"/></li>
                        <li><g:message code="userRegistration.consent.message3bullet2"/></li>
                    </ul>

                    <p><g:message code="userRegistration.consent.message4"/></p>
                    <ul>
                        <li><g:message code="userRegistration.consent.message4bullet1"/></li>
                        <li><g:message code="userRegistration.consent.message4bullet2"/></li>
                        <li><g:message code="userRegistration.consent.message4bullet3"/></li>
                        <li><g:message code="userRegistration.consent.message4bullet4"/></li>
                        <li><g:message code="userRegistration.consent.message4bullet5"/></li>
                    </ul>

                    <p><g:message code="userRegistration.consent.message5"/></p>
                </g:else>

                <g:message code="userRegistration.consent.integritypolicylink" args="[createLink(controller: 'home', action: 'privacypolicy')]"/>.<br/>
                <g:message code="userRegistration.consent.useragreementlink" args="[createLink(controller: 'home', action: 'useragreement')]"/>.

                <br/><br/>

                <div class="checkbox">
                    <input type="checkbox" name="acceptConsentModalCheckBoxTerms" id="acceptConsentModalCheckBoxTerms">
                    <label for="acceptConsentModalCheckBoxTerms">
                        <g:if test="${existingUserMode}"><g:message code="updatedTerms.message7"/></g:if>
                        <g:else><g:message code="userRegistration.consent.consenttopersonaldata"/></g:else>
                    </label>
                </div>
                <g:if test="${!existingUserMode}">
                    <div class="checkbox">
                        <input type="checkbox" name="acceptConsentModalCheckBoxReceiveNewsletter" id="acceptConsentModalCheckBoxReceiveNewsletter">
                        <label for="acceptConsentModalCheckBoxReceiveNewsletter"><g:message code="userRegistration.consent.receivenewsletter"/></label>
                    </div>
                    <div class="checkbox">
                        <input type="checkbox" name="acceptConsentModalCheckBoxReceiveCustomerSurveys" id="acceptConsentModalCheckBoxReceiveCustomerSurveys">
                        <label for="acceptConsentModalCheckBoxReceiveCustomerSurveys"><g:message code="userRegistration.consent.customersurveys"/></label>
                    </div>
                </g:if>

            </div>
            <div class="modal-footer">
                <g:if test="${!existingUserMode}">
                    <button type="button" class="btn btn-md btn-default" data-dismiss="modal"><g:message code="button.quit.label"/></button>
                </g:if>
                <g:if test="${bootstrap3}">
                    <a href="#" id="acceptConsentModalSubmit" class="btn btn-md btn-success disabled"><g:message code="button.accept.label"/></a>
                </g:if>
                <g:else>
                    <button id="acceptConsentModalSubmit" class="btn btn-md btn-success disabled" disabled="disabled"><g:message code="button.accept.label"/></button>
                </g:else>
            </div>
        </div>
    </div>
</div>


<r:script>
    $(document).ready(function () {
        // When the terms checkbox is ticked enable the accept button.
        $('#acceptConsentModalCheckBoxTerms').change(function() {
            if(this.checked) {
                $('#acceptConsentModalSubmit').removeClass('disabled').removeAttr('disabled');
            } else {
                $('#acceptConsentModalSubmit').addClass('disabled').attr('disabled', 'disabled');
            }
        });

        <g:if test="${!overrideBehaviour}">
            $('#acceptConsentModalSubmit').click(function(){
                $.ajax({
                    url: "${g.forJavaScript(data: createLink(controller: 'userProfile', action: 'updateAccountConsents'))}",
                    method: 'POST',
                    data: {
                        acceptTerms: $('#acceptConsentModalCheckBoxTerms').is(":checked"),
                        newsletters: $('#acceptConsentModalCheckBoxReceiveNewsletter').is(":checked"),
                        customerSurveys: $('#acceptConsentModalCheckBoxReceiveCustomerSurveys').is(":checked")
                    },
                    success: function() {
                        $('#acceptConsentModal').modal('hide');
                    }
                });
            });
        </g:if>

        <g:if test="${showTerms}">
            $('#acceptConsentModal').modal('show');
        </g:if>
    });
</r:script>