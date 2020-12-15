<%@ page import="com.matchi.Customer; com.matchi.facility.FilterCustomerCommand; com.matchi.membership.Membership; com.matchi.invoice.Invoice; com.matchi.FacilityProperty" %>

<form method="GET" id="filterForm" class="form-search well" style="padding:7px 10px 4px 10px;">
    <g:hiddenField name="reset" value="true"/>
    <fieldset>
        <div class="control-group">
            <ul class="inline filter-list">
                <li><g:textField id="customer-search-input" class="search span3" name="q" value="${filter?.q}" placeholder="${message(code: 'customer.search.placeholder')}" style="width: 202px;"/></li>
                <li>
                    <select id="gender" name="gender" multiple="true">
                        <option value="NULL" ${ filter.gender.contains(Customer.CustomerType.NULL) ? "selected" : ""}>
                            <g:message code="facilityCustomer.index.noGender"/>
                        </option>
                        <g:each in="${Customer.CustomerType.list()}">
                            <option value="${it}" ${ filter.gender.contains(it) ? "selected":""}><g:message code="customer.type.${it}" /></option>
                        </g:each>
                    </select>
                </li>
                <g:if test="${facilityGroups.size() > 0}">
                    <li>
                        <select id="group" name="group" multiple="true">
                            <option value="0" ${ filter.group.contains(0L) ? "selected" : ""}>
                                <g:message code="facilityCustomer.index.noGroup"/>
                            </option>
                            <g:each in="${facilityGroups}">
                                <option value="${it.id}" ${ filter.group.contains(it.id) ? "selected":""}>${it.name}</option>
                            </g:each>
                        </select>
                    </li>
                </g:if>
                <li>
                    <select id="members" name="members" multiple="true">
                        <g:each in="${FilterCustomerCommand.ShowMembers.list()}">
                            <option value="${it}" ${ filter.members.contains(it) ? "selected":""}>
                                <g:message code="filterCustomerCommand.members.showmembers.choise.${it}"/>
                            </option>
                        </g:each>
                    </select>
                </li>
                <g:if test="${types.size() > 0}">
                    <li>
                        <select id="type" name="type" multiple="true">
                            <option value="0" ${ filter.type.contains(0L) ? "selected":""}>
                                <g:message code="facilityCustomer.index.noMembershipType"/>
                            </option>
                            <g:each in="${types}">
                                <option value="${it.id}" ${ filter.type.contains(it.id) ? "selected":""}>${it.name}</option>
                            </g:each>
                        </select>
                    </li>
                </g:if>
                <li>
                    <select id="status" name="status" multiple="true">
                        <g:each in="${FilterCustomerCommand.MemberStatus.list(facility)}">
                            <option value="${it}" ${ filter.status.contains(it) ? "selected":""}>
                                <g:message code="facilityCustomerMembers.index.status.${it}"/>
                            </option>
                        </g:each>
                    </select>
                </li>
                <li>
                    <select id="birthyear" name="birthyear" multiple="true">
                        <option value="0" ${filter.birthyear.contains(0) ? "selected" : ""}>
                            <g:message code="facilityCustomer.index.birthyearSelect.noBirthyear"/>
                        </option>
                        <g:each in="${birthyears}">
                            <option value="${it}" ${filter.birthyear.contains(it) ? "selected" : ""}>${it}</option>
                        </g:each>
                    </select>
                </li>
                <li>
                    <select id="seasons" name="seasons" multiple="true">
                        <option ${filter.seasons.contains(0l) ? "selected" : ""}>
                            <g:message code="facilityCustomer.index.seasons.noSeason"/>
                        </option>
                        <g:each in="${seasons}">
                            <option value="${it?.id}" ${filter.seasons.contains(it?.id) ? "selected" : ""}>${it?.name}</option>
                        </g:each>
                    </select>
                </li>
                <g:if test="${facility.hasApplicationInvoice()}">
                    <li>
                        <select id="invoiceStatus" name="invoiceStatus" multiple="true">
                            <g:each in="${Invoice.InvoiceStatus.values()}">
                                <option value="${it}" ${(filter.invoiceStatus?.contains(it) ? "selected" : "")}><g:message code="invoice.status.${it}"/></option>
                            </g:each>
                        </select>
                    </li>
                </g:if>
                <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_TRAINING_PLANNER.name()}">
                    <li>
                        <select id="courses" name="courses" multiple="true">
                            <option ${filter.courses.contains(0l) ? "selected" : ""}>
                                <g:message code="facilityCustomer.index.courses.noCourse"/>
                            </option>
                            <g:each in="${courses}">
                                <option value="${it?.id}" ${filter.courses.contains(it?.id) ? "selected" : ""}>${ it.isArchived() ? "${message(code: 'facilityActivity.tabs.archived.label.singular')} - ${it.name}" : "${it.name}" }</option>
                            </g:each>
                        </select>
                    </li>
                </g:ifFacilityPropertyEnabled>
                <li class="pull-right">
                    <g:if test="${filter.isActive()}">
                        <g:link params="[reset: true]" class="btn btn-danger">
                            <g:message code="button.filter.remove.label"/>
                        </g:link>
                    </g:if>
                    <g:else>
                        <a href="javascript: void(0)" class="btn btn-default disabled">
                            <g:message code="button.filter.remove.label"/>
                        </a>
                    </g:else>
                    <button id="filterSubmit" tabindex="3" class="btn" type="submit"><g:message code="button.filter.label"/></button>
                </li>
                <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name()}">
                    <li>
                        <select id="clubs" name="clubs" multiple="true">
                            <option ${filter.clubs.contains(message(code: "facilityCustomer.index.clubs.noClub")) ? "selected" : ""}>
                                <g:message code="facilityCustomer.index.clubs.noClub"/>
                            </option>
                            <g:each in="${clubs}">
                                <option value="${it}" ${filter.clubs.contains(it) ? "selected" : ""}>${it}</option>
                            </g:each>
                        </select>
                    </li>
                </g:ifFacilityPropertyEnabled>
                <li>
                    <select id="lastActivity" name="lastActivity" multiple="false">
                        <option ${filter.lastActivity == 0 ? "selected" : ""}>
                            <g:message code="facilityCustomer.index.lastActivity.noLastActivity"/>
                        </option>
                        <g:each var="i" in="${ (1..<8) }">
                            <option value="${i}" ${filter.lastActivity == i ? "selected" : ""}> <g:message code="facilityCustomer.index.lastActivity.selectedLastActivity" args="[i]"/></option>
                        </g:each>
                    </select>
                </li>
                <g:if test="${facility.isMemberFacility()}">
                    <li>
                        <select id="dontIncludeMemberFacilitysCustomer" name="dontIncludeMemberFacilitysCustomer" multiple="false">
                            <option ${filter.dontIncludeMemberFacilitysCustomer == 0 ? "selected" : ""} value="0">
                                <g:message code="facilityCustomer.index.includeGlobalFacilitysCustomer.doInclude"/>
                            </option>
                            <option value="1" ${filter.dontIncludeMemberFacilitysCustomer == true ? "selected" : ""}> <g:message code="facilityCustomer.index.includeGlobalFacilitysCustomer.dontInclude"/>
                        </select>
                    </li>
                </g:if>
                <g:if test="${facility.isMasterFacility()}">
                    <li>
                        <select id="localFacilities" name="localFacilities" multiple="true">
                            <g:each in="${localFacilities}">
                                <option value="${it.id}" ${ filter.localFacilities.contains(it.id) ? "selected":""}>${it.name}</option>
                            </g:each>
                        </select>
                    </li>
                </g:if>
            </ul>
        </div>
    </fieldset>
</form>

<r:script>
    $(function() {
        $("#gender").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'customer.type.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'customer.type.multiselect.selectedText')}"
        });
        $("#group").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'group.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'group.multiselect.selectedText')}"
        });
        $("#type").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'membershipType.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'membershipType.multiselect.selectedText')}"
        });
        $("#members").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityCustomer.index.membersSelect.noneSelectedText')}",
            selectedText: "${message(code: 'filterCustomerCommand.members.multiselect.selectedText')}"
        });

        $("#status").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'membership.status.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'membership.status.multiselect.selectedText')}"
        });

        $("#birthyear").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityCustomer.index.birthyearSelect.noneSelectedText')}",
            selectedText: "${message(code: 'facilityCustomer.index.birthyearSelect.selectedText')}"
        });

        $("#seasons").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityCustomer.index.seasons.noneSelectedText')}",
            selectedText: "${message(code: 'facilityCustomer.index.seasons.selectedText')}"
        });

        $("#invoiceStatus").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityCustomer.index.invoiceStatus.noneSelectedText')}",
            selectedText: "${message(code: 'invoice.status.multiselect.selectedText')}"
        });

        $("#courses").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityCustomer.index.courses.noneSelectedText')}",
            selectedText: "${message(code: 'facilityCustomer.index.courses.selectedText')}"
        });

        $("#lastActivity").multiselect({
            create: function() {$(this).next().width(210);},
            minWidth: 200,
            classes: "multi",
            multiple: false,
            selectedList: 1,
            showCheckAll: true,
            showUncheckAll: true,
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityCustomer.index.lastActivity.noLastActivity')}"
        });
        <g:if test="${facility.isMasterFacility()}">
        $("#localFacilities").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityLocal.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'facility.multiselect.selectedText')}"
        });
        </g:if>
        <g:if test="${facility.isMemberFacility()}">
        $("#dontIncludeMemberFacilitysCustomer").multiselect({
            create: function() {$(this).next().width(210);},
            minWidth: 200,
            classes: "multi",
            multiple: false,
            selectedList: 1,
            showCheckAll: false,
            showUncheckAll: false,
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityCustomer.index.includeGlobalFacilitysCustomer.doInclude')}"
        });
        </g:if>

        <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name()}">
        $("#clubs").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityCustomer.index.clubs.noneSelectedText')}",
            selectedText: "${message(code: 'facilityCustomer.index.clubs.selectedText')}"
        });
        </g:ifFacilityPropertyEnabled>
    });
</r:script>