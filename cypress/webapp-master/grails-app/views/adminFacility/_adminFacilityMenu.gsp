<% def i = 0 %>
<div class="panel-heading no-padding">
    <div class="tabs tabs-style-underline" style="width: 70%;">
        <nav>
            <ul>
                <li class="${selected==i++?"active tab-current":""}">
                    <g:link controller="adminFacility" action="edit" params="[id: facility.id]">
                        <i class="fa fa-pencil"></i>
                        <span><g:message code="button.edit.label"/></span>
                    </g:link>
                </li>
                <li class="${selected==i++?"active tab-current":""}">
                    <g:link controller="adminFacilityProperties" action="index" params="[id: facility.id]">
                        <i class="fa fa-pencil"></i>
                        <span><g:message code="adminFacility.adminFacilityMenu.settings"/></span>
                    </g:link>
                </li>
                <li class="${selected==i++?"active tab-current":""}">
                    <g:link controller="adminFacilityCourts" action="index" params="[id: facility.id]">
                        <i class="fas fa-list"></i>
                        <span><g:message code="court.label.plural"/></span>
                    </g:link>
                </li>
                <li class="${selected==i++?"active tab-current":""}">
                    <g:link controller="adminFacilityContracts" action="index" params="[id: facility.id]">
                        <i class="fas fa-paperclip"></i>
                        <span><g:message code="adminFacility.adminFacilityMenu.contract"/></span>
                    </g:link>
                </li>
                <li class="${selected==i++?"active tab-current":""}">
                    <g:link controller="adminFacilityOrganizations" action="index" params="[id: facility.id]">
                        <i class="fas fa-briefcase"></i>
                        <span><g:message code="organization.label.plural"/></span>
                    </g:link>
                </li>
                <g:if test="${facility?.hasMPC()}">
                    <li class="${selected==i++?"active tab-current":""}">
                        <g:link controller="adminFacility" action="mpc" params="[id: facility.id]">
                            <i class="fab fa-connectdevelop"></i>
                            <span>MPC</span>
                        </g:link>
                    </li>
                </g:if>
                <li class="${selected==i++?"active tab-current":""}">
                    <g:link controller="adminFacilityBilling" action="index" params="[id: facility.id]">
                        <i class="fas fa-bank"></i>
                        <span><g:message code="adminFacilityBilling.index.title"/></span>
                    </g:link>
                </li>
            </ul>
        </nav>
    </div>
</div>
