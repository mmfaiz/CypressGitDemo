<%@ page import="com.matchi.sportprofile.SportProfile" %>
    <div class="panel panel-default">
        <header class="panel-heading">
            <div class="row">
                <div class="col-sm-6 col-xs-6">
                    <h3 class="h4 no-margin">
                        <i class="text-md icon-sport ma ma-${profile?.sport?.id} right-margin5"></i>
                        <g:message code="sport.name.${profile?.sport?.id}"/>&nbsp;
                    </h3>
                </div>
                <div class="col-sm-6 col-xs-6 text-right">
                    <g:if test="${profile.user.id == currentUser.id}">
                        <g:remoteLink name="editSport" role="form" update="sportProfileModal"
                                      url="[controller:'userProfile', action:'sportEdit']"
                                      class="btn btn-link btn-xs no-padding"
                                      params="[sport: profile?.sport?.id]"
                                      onSuccess="showLayer('sportProfileModal')"><i class="fas fa-edit"></i> <g:message code="button.edit.label"/></g:remoteLink>
                        <g:link action="removeSport" id="${profile.id}"
                                class="btn btn-link btn-xs no-padding"
                                onclick="return confirm('${message(code: 'templates.profile.sportProfileExtended.message10')}')">
                            <i class="fa fa-trash-o"></i> <g:message code="button.delete.label"/></g:link>
                    </g:if>
                </div>
            </div>
        </header>

        <% def r = SportProfile.skillLevelRange.toInt  %>
        <div class="panel-body">
            <div class="row">
                <div class="col-sm-8">

                    <g:each in="${profile.sportProfileAttributes}" var="attr" status="j">
                        <% def l = attr ? attr.skillLevel : null %>
                        <div class="">
                            <div class="">
                                <span class="text-sm"><g:message code="sportattribute.name.${attr.sportAttribute.name}"/></span>
                            </div>
                            <div class="">
                                <div class="progress progress-straight progress-thin vertical-margin5">
                                    <div class="progress-bar progress-bar-yellow" role="progressbar" data-transitiongoal="${l}" aria-valuenow="${l}" aria-valuemin="0" aria-valuemax="10"></div>
                                </div>
                            </div>
                        </div>
                    </g:each>

                    <div class="top-margin10">
                        <div class="">
                            <span class="text-sm"><g:message code="templates.profile.sportProfileExtended.message11"/></span>
                        </div>
                        <div class="">
                            <g:if test="${profile.frequency}">
                                <span class="text-muted"><g:message code="sportprofile.frequency.${profile.frequency}"/></span>
                            </g:if>
                            <g:else>
                                <span class="text-muted"><g:message code="templates.profile.sportProfileExtended.message12"/></span>
                            </g:else>
                        </div>
                    </div>

                    <div class="top-margin10">
                        <div class="">
                            <span class="text-sm"><g:message code="templates.profile.sportProfileExtended.message2"/></span>
                        </div>
                        <div class="">
                            <g:if test="${profile?.mindSets?.size() > 0}">
                                <g:each in="${profile?.mindSets}">
                                    <span class="label label-outline label-${it.badgeColor}"><g:message code="sportprofile.mindset.${it}"/></span>
                                </g:each>
                            </g:if>
                            <g:else>
                                <span class="text-muted"><g:message code="templates.profile.sportProfileExtended.message12"/></span>
                            </g:else>
                        </div>
                    </div>

                </div>
                <div class="col-sm-4 text-center skilllevelbox">
                    <div class="block bg-grey-light text-center vertical-padding20">
                        <span class="block h1 weight700" style="font-size:5em">${profile?.skillLevel}</span>
                        <span class="block text-sm no-margin"><g:message code="templates.profile.sportProfileExtended.message1"/></span>
                    </div>
                </div>

            </div>
        </div>
    </div>
<r:script>
$('.progress .progress-bar').progressbar({
    transition_delay: 1000
});
</r:script>
