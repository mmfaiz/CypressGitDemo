<div class="offset1 span5">
    <g:form action="createActivityWatch">
        <table class="table" style="width:100%">
            <thead>
            <tr>
                <th colspan="2">
                    <g:message code="facilityActivityOccasion.edit.watchQueue"/>
                    (${activityWatchQueue.size()}<g:message code="unit.st"/>)
                </th>
                <th></th>
            </tr>
            </thead>
            <tr>
                <td colspan="3">
                    <input type="hidden" name="occasionId" value="${occasion.id}"/>
                    <input type="hidden" name="customerId" id="queueCustomerSearch"/>
                    <g:submitButton id="btnAddWatcher" name="btnAddWatcher"
                            value="${message(code: 'button.add.label')}" class="btn btn-small  left-margin5"/>
                    <p class="vertical-margin5"><small>
                    <i class="fa fa-info-circle text-info"></i>
                    <g:message code="facilityActivityOccasion.edit.watchQueue.customersSearchNote"/></small></p>
                </td>
            </tr>
            <g:each in="${activityWatchQueue}" var="data">
                <tr>
                    <td width="1">
                        <span class="span1" style="margin-left: 0;">
                            <g:fileArchiveUserImage size="small" id="${data.userId}" />
                        </span>
                    </td>
                    <td>
                        <strong>
                            <g:if test="${data.customerId}">
                                <g:link controller="facilityCustomer" action="show" id="${data.customerId}">
                                    ${data.customerNr} - ${data.customerName}
                                </g:link>
                            </g:if>
                            <g:else>
                                ${data.userName}
                            </g:else>
                        </strong>
                        <br/>
                        ${data.customerId ? data.customerEmail : data.userEmail}
                    </td>
                    <td align="right">
                        <div class="btn-group pull-right">
                            <button class="btn btn-small dropdown-toggle" data-toggle="dropdown">
                                <g:message code="facilityActivityOccasion.edit.watchActions"/>
                                <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu">
                                <g:if test="${data.customerId}">
                                    <li>
                                        <g:link controller="facilityCustomerMessage" action="message"
                                                params="[customerId: data.customerId, returnUrl: editOccasionUrl]">
                                            <g:message code="facilityCustomer.index.message25"/>
                                        </g:link>
                                    </li>
                                    <li>
                                        <g:link controller="facilityCustomerSMSMessage" action="message"
                                                params="[customerId: data.customerId, returnUrl: editOccasionUrl]">
                                            <g:message code="facilityCustomer.index.message24"/>
                                        </g:link>
                                    </li>
                                </g:if>
                                <li>
                                    <g:link action="removeActivityWatch" params="[occasionId: occasion.id, userId: data.userId]"
                                            onclick="return confirm('${message(code: 'button.delete.confirm.message')}')">
                                        <g:message code="button.remove.label"/>
                                    </g:link>
                                </li>
                            </ul>
                        </div>
                    </td>
                </tr>
            </g:each>
        </table>
    </g:form>
</div>

<r:script>
    $(document).ready(function() {
        $("#queueCustomerSearch").matchiCustomerSelect({
            width: "210px",
            placeholder: "${message(code: 'facilityActivityOccasion.edit.message33')}",
            onchange: function() { $('#btnAddWatcher').focus() },
            customersWithConnectedUsers: true
        });
    });
</r:script>