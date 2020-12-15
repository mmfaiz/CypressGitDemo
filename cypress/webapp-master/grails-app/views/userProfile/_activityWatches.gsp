<%@ page import="org.joda.time.DateTime" %>
<div class="panel panel-default">
    <header class="panel-heading">
        <h4 class="h5 no-margin">
            <i class="fa fa-clock-o"></i> <g:message code="default.activityQueueSlots"/>
        </h4>
    </header>
    <div class="table-responsive">
        <table class="table table-striped text-sm">
            <thead>
            <tr>
                <th width="40%"><g:message code="default.date.place"/></th>
                <th width="20%"><g:message code="default.date.label"/> / <g:message code="default.date.time"/></th>
                <th width="30%"><g:message code="default.activity.label"/></th>
                <th width="10%"></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${0..(activityWatches.size() > 5 ? 4 : activityWatches.size() - 1)}" var="it">
                <tr id="activitywatch_${activityWatches[it]?.id}">
                    <td class="vertical-padding10">
                        <div class="media">
                            <g:set var="swFacility" value="${activityWatches[it]?.facility}"/>
                            <div class="media-left">
                                <div class="avatar-square-xs avatar-bordered">
                                    <g:link controller="facility" action="show" params="[name: swFacility?.shortname]">
                                        <g:fileArchiveFacilityLogoImage file="${swFacility?.facilityLogotypeImage}" alt="${swFacility?.name}"/>
                                    </g:link>
                                </div>
                            </div>
                            <div class="media-body">
                                <h6 class="media-heading">
                                    <g:link controller="facility" action="show" params="[name: swFacility?.shortname]">${swFacility?.name}</g:link>
                                </h6>
                                <span class="block text-sm text-muted"><i class="fas fa-map-marker"></i> ${swFacility?.municipality}</span>
                            </div>
                        </div>
                    </td>
                    <td class="vertical-padding10">
                        <g:humanDateFormat date="${new DateTime(activityWatches[it]?.fromDate)}"/>
                        <span class="block text-sm text-muted">
                            <g:formatDate format="HH:mm" date="${activityWatches[it]?.fromDate}" />
                        </span>
                    </td>
                    <td class="vertical-padding10">
                        ${activityWatches[it].classActivity.name}
                    </td>
                    <td class="vertical-padding10 text-right">
                        <g:remoteLink class="btn btn-link btn-xs btn-danger text-danger"
                                      action="remove" method="DELETE" mapping="activityWatch"
                                      onSuccess="\$('#activitywatch_${activityWatches[it]?.id}').remove()"
                                      before="if(!confirm('${message(code: "default.confirm")}')) return false"
                                      id="${activityWatches[it]?.id}"><i class="fas fa-times"></i> <g:message code="button.delete.label"/></g:remoteLink>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div><!-- /.table-responsive -->
</div>