<%@ page import="com.matchi.sportprofile.SportProfile; java.text.SimpleDateFormat; com.matchi.User" %>
<html>
<head>
    <meta name="layout" content="b3main" />
    <title>${user.fullName()} - MATCHi</title>
    <meta name="showFacebookNagger" content="${true}"/>
    <meta name="hideNotifications" content="${true}"/>
    <meta name="classes" content="splash-page"/>
    <r:require modules="jasny-fileinput,bootstrap-slider,bootstrap-progressbar"/>
</head>
<body>
<!-- TOP COVER STRIPE -->
<section class="block box-shadow user-profile">
    <div class="welcome-wrapper ${isCurrentUser ? 'upload-cover':''}">
        <div class="welcome-wrapper splash splash-container" style="background-image: url(<g:userWelcomeUrl id="${user.id}"/>);">

        </div>
        <g:if test="${isCurrentUser}">
            <div class="absolute-center cover-image-btns">
                <button type="button" class="btn btn-lg btn-block btn-white btn-upload-cover" data-toggle="modal" data-target="#welcomeImageModal"><i class="fas fa-camera"></i> <g:message code="button.upload.cover.image"/></button>
                <!-- <g:if test="${user.welcomeImage}">
                    <button type="button" class="btn btn-sm btn-block btn-danger btn-outline btn-remove-cover"><i class="fas fa-times"></i> <g:message code="button.remove.cover.image"/></button>
                </g:if> -->
            </div>
        </g:if>
    </div>
</section>

<!-- USER PROFILE CONTENT -->
<section class="relative block block-grey">
    <div class="block block-white">
        <div class="container">
            <div class="row flex-center wrap">
                <div class="col-sm-7 col-xs-8 relative flex-center">
                    <!-- USER AVATAR -->
                    <div class="profile-avatar-wrapper ${isCurrentUser ? 'upload-avatar':''}">
                        <div class="profile-avatar-user avatar-circle-lg">
                            <g:fileArchiveUserImage size="large" id="${user.id}"/>
                            <g:if test="${isCurrentUser}">
                                <button type="button" class="btn btn-sm btn-black btn-outline btn-circle btn-upload-avatar absolute-center" data-toggle="modal" data-target="#imageModal"><i class="fas fa-camera"></i></button>
                            </g:if>
                        </div>
                    </div><!-- /.profile avatar-wrapper -->

                <!-- USER NAME & LOCATION -->
                    <div class="profile-name-location vertical-padding30">
                        <h2 class="h4 no-margin">
                            <g:skillLevel id="${user?.id}"/> <span id="userName">${user.fullName()}</span>
                        </h2>
                        <g:if test="${user.municipality}">
                            <span class="block text-xs text-muted top-margin10">
                                <i class="fas fa-map-marker"></i> ${user.municipality}${user.city ? " (" + user.city + ")" : ""} - ${user.municipality.region}
                            </span>
                        </g:if>
                    </div><!-- /.facility name & location-->
                </div>

                <div class="col-sm-5 col-xs-4 text-right">
                <!-- MESSAGE BTN -->
                    <g:if test="${!isCurrentUser}">
                        <g:canSendDirectMessage to="${user}">
                            <g:remoteLink class="btn btn-success no-margin btn-lg" controller="message" update="messageModal" onError="handleAjaxError()" onSuccess="showLayer('messageModal')"
                                          params="['id':user.id, 'returnUrl': g.createLink(absolute: true, controller: 'userProfile', action: 'index', id:user.id)]">
                                <i class="fas fa-envelope hidden-sm"></i> <g:message code="button.message"/></g:remoteLink>
                        </g:canSendDirectMessage>
                    </g:if>
                    <g:if test="${isCurrentUser}">
                        <g:link class="btn btn-primary btn-outline btn-sm" action="edit"><i class="fas fa-edit"></i> <g:message code="button.edit.profile"/></g:link>
                    </g:if>
                </div>
            </div>
        </div><!-- /.container -->
    </div>
    <div class="container vertical-padding50">
        <div class="row">

            <!-- SIDEBAR -->
            <div class="col-md-3">

                <!-- USER AVAILABILITY -->
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="h5">
                            <g:if test="${isCurrentUser}">
                                <a class="btn btn-link no-padding" data-toggle="modal" data-target="#availabilityModal"><i class="fas fa-edit"></i> <g:message code="user.availability"/></a>
                            </g:if>
                            <g:else>
                                <g:message code="user.availability"/>
                            </g:else>
                        </h4>
                    </div>
                    <div class="panel-body">
                        <g:if test="${availabilities?.size() > 0}">
                            <dl class="dl-horizontal dl-info" id="userAvailability">
                                <g:each in="${1..7}" var="day" status="i">
                                    <%
                                        def av = availabilities.find { it.weekday == day }
                                    %>
                                    <g:if test="${av}">
                                        <dt class="wide"><g:message code="time.weekDay.plural.${av.weekday}"/></dt>
                                        <dd>${av.begin.toString("HH:mm")} - ${av.end.toString("HH:mm")}</dd>
                                    </g:if>
                                </g:each>
                            </dl>
                        </g:if>
                        <g:else>
                            <g:message code="user.no.availability"/>
                        </g:else>
                    </div>
                </div><!-- /.USER AVAILABILITY -->

            <!-- USER FAVORITES -->
                <g:if test="${user.favourites?.size() > 0}">
                    <section class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="h5"><g:message code="user.favorites.facilities.label"/></h3>
                        </div>
                        <div id="userFavorites">
                            <ul class="list-favorites horizontal-padding15">
                                <g:each in="${user.favourites}">
                                    <li class="favorite-item">
                                        <div class="media">
                                            <div class="media-left">
                                                <div class="avatar-square-xs avatar-bordered">
                                                    <g:link controller="facility" action="show" params="[name: it.facility.shortname]">
                                                        <g:fileArchiveFacilityLogoImage file="${it.facility.facilityLogotypeImage}" alt="${it.facility.name}"/>
                                                    </g:link>
                                                </div>
                                            </div>
                                            <div class="media-body">
                                                <h6 class="media-heading top-margin5 no-bottom-margin">
                                                    <g:link controller="facility" action="show" params="[name:it.facility.shortname]">${it.facility.name}</g:link>
                                                </h6>
                                                <span class="text-muted text-sm"><i class="fas fa-map-marker"></i> ${it.facility.municipality}</span>
                                            </div>
                                        </div>
                                    </li>
                                </g:each>
                            </ul>
                        </div>
                    </section>
                </g:if>

            </div><!-- /.SIDEBAR -->

        <!-- MAIN CONTENT -->
            <div class="col-md-6">

                <!-- USER DESCRIPTION -->
                <div class="panel panel-default">
                    <div class="panel-heading">
                    <h4 class="h5">
                        <g:if test="${isCurrentUser}">
                            <g:link class="btn btn-link no-padding" action="edit"><i class="fas fa-edit"></i> <g:message code="user.about"/> ${user.firstname}</g:link></h4>
                        </g:if>
                        <g:else>
                            <g:message code="user.about"/> ${user.firstname}
                        </g:else>
                    </h4>
                    </div>
                    <div class="panel-body">
                        <g:if test="${user?.description && user?.description?.trim() != ""}">
                            <p id="userDescription">
                                ${user?.description}
                            </p>
                        </g:if>
                        <g:else>
                            <p class="text-muted">
                                <em><g:message code="user.no.description"/></em>
                            </p>
                        </g:else>
                    </div>
                </div><!-- /.USER DESCRIPTION -->

                <h3><g:message code="user.sport.profiles"/></h3>
                <!-- ADD SPORT PROFILES -->
                <div class="user-sports-profiles">
                    <g:if test="${isCurrentUser && userAvailableSportProfiles?.size() > 0}">
                        <div class="panel panel-default">
                            <div class="panel-body">
                                <g:formRemote name="addSport" role="form"
                                              url="[action:'sportAdd']"
                                              update="sportProfileModal"
                                              class="no-padding"
                                              onSuccess="showLayer('sportProfileModal')">
                                    <div class="form-group no-margin">
                                        <select id="sportSelect" class="btn-group-fit input-group-btn" name="sport" data-style="btn-lg form-control">
                                            <g:each in="${userAvailableSportProfiles}">
                                                <option value="${it.id}"><g:message code="sport.name.${it.id}"/></option>
                                            </g:each>
                                        </select>
                                        <button class="btn btn-primary btn-add-sport btn-lg"><i class="fas fa-plus-circle"></i> <span><g:message code="button.add.label"/></span></button>
                                    </div>
                                </g:formRemote>
                            </div>
                        </div>
                    </g:if>
                </div>
                <!-- SPORT PROFILES -->
                <g:each in="${user.sportProfiles}" var="profile" status="i">
                    <g:sportProfileExtended id="${profile.id}" />
                </g:each>
            </div><!-- /.SPORT PROFILES -->

            <div class="col-md-3">
            <!-- PROFILE COMPLETENESS -->
                <g:if test="${isCurrentUser}">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="h5"><g:message code="user.profile.completeness"/></h4>
                        </div>
                        <div class="panel-body">
                            <div class="bottom-padding20">
                                <div class="progress progress-thin profile-completeness top-margin10 bottom-margin5">
                                    <div class="progress-bar progress-bar-brand" role="progressbar"></div>
                                </div>
                                <span class="block text-xs text-muted"><g:message code="user.your.profile.completeness.level"/> <span id="precentage"></span>% <g:message code="user.your.profile.complete"/></span>
                            </div>
                            <dl class="dl-horizontal dl-info dl-info-profile">

                                <!-- BIRTHDAY -->
                                <dt class="icon"><i class="fa fa-calendar"></i></dt>
                                <dd class="info ellipsis">
                                    <g:if test="${user.birthday}">
                                        <span class="pull-right"><i class="fas fa-check text-success"></i></span>
                                        <span id="userBirthday"><g:formatDate format="${message(code:'date.format.dateOnly')}" date="${user.birthday}"/></span>
                                    </g:if>
                                    <g:else>
                                        <span class="pull-right" rel="tooltip" data-original-title="<g:message code="user.info.incomplete"/>"><i class="fas fa-exclamation-triangle text-warning"></i></span>
                                        <g:link action="edit" rel="tooltip" data-original-title="${message(code: 'button.edit.profile')}"><g:message code="user.edit.birthday"/></g:link>
                                    </g:else>
                                </dd>

                                <!-- SEX -->
                                <dt class="icon"><i class="fas fa-venus-mars"></i></dt>
                                <dd class="info ellipsis">
                                    <g:if test="${user.gender}">
                                        <span class="pull-right"><i class="fas fa-check text-success"></i></span>
                                        <span id="userGender"><g:message code="gender.${user.gender}"/></span>
                                    </g:if>
                                    <g:else>
                                        <span class="pull-right" rel="tooltip" data-original-title="<g:message code="user.info.incomplete"/>"><i class="fas fa-exclamation-triangle text-warning"></i></span>
                                        <g:link action="edit" rel="tooltip" data-original-title="${message(code: 'button.edit.profile')}"><g:message code="user.edit.gender"/></g:link>
                                    </g:else>
                                </dd>

                                <!-- EMAIL -->
                                <dt class="icon"><i class="fas fa-envelope"></i></dt>
                                <dd class="info ellipsis">
                                    <g:if test="${user.email}">
                                        <span class="pull-right"><i class="fas fa-check text-success"></i></span>
                                        <span id="userEmailAddress">${user.email}</span>
                                    </g:if>
                                    <g:else>
                                        <span class="pull-right" rel="tooltip" data-original-title="<g:message code="user.info.incomplete"/>"><i class="fas fa-exclamation-triangle text-warning"></i></span>
                                        <g:link action="edit" rel="tooltip" data-original-title="${message(code: 'button.edit.profile')}"><g:message code="user.edit.email"/></g:link>
                                    </g:else>
                                </dd>

                                <!-- PHONE -->
                                <dt class="icon"><i class="fas fa-phone"></i></dt>
                                <dd class="info ellipsis">
                                    <g:if test="${user.telephone}">
                                        <span class="pull-right"><i class="fas fa-check text-success"></i></span>
                                        <span id="userPhoneNumber">${user.telephone}</span>
                                    </g:if>
                                    <g:else>
                                        <span class="pull-right" rel="tooltip" data-original-title="<g:message code="user.info.incomplete"/>"><i class="fas fa-exclamation-triangle text-warning"></i></span>
                                        <g:link action="edit" rel="tooltip" data-original-title="${message(code: 'button.edit.profile')}"><g:message code="user.edit.phonenumber"/></g:link>
                                    </g:else>
                                </dd>

                                <!-- ADDRESS -->
                                <dt class="icon"><i class="fas fa-map-marker"></i></dt>
                                <dd class="info">
                                    <g:if test="${user.address || user.zipcode || user.city || user.municipality}">
                                        <span id="userAddress">
                                            ${user.address}
                                            <g:if test="user.zipcode">
                                                <g:if test="user.address">
                                                    <br/>
                                                    ${user.zipcode}
                                                </g:if>
                                                <g:else>
                                                    ${user.zipcode}
                                                </g:else>
                                            </g:if>
                                            ${user.municipality ? (user.zipcode ? ', ' + user.municipality : user.municipality) : ''}
                                            ${user.city ? (user.municipality ?' (' + user.city + ')': ', ' + user.city) : ''}
                                        </span>
                                    </g:if>
                                    <g:if test="${user.address && user.zipcode && user.city && user.municipality}">
                                        <span class="pull-right"><i class="fas fa-check text-success"></i></span>
                                    </g:if>
                                    <g:else>
                                        <span class="pull-right" rel="tooltip" data-original-title="<g:message code="user.info.incomplete"/>"><i class="fas fa-exclamation-triangle text-warning"></i></span>
                                        <g:link action="edit" rel="tooltip" data-original-title="${message(code: 'button.edit.profile')}"><g:message code="user.edit.address"/></g:link>
                                    </g:else>
                                </dd>
                            </dl>
                        </div><!-- /.panel-body -->

                        <div class="panel-footer bg-white">
                            <span class="text-sm"><i class="fas fa-info-circle text-info"></i> <g:message code="user.visible.only.for.user"/></span>
                        </div>
                    </div>
                </g:if>

            <!-- PUT AD OR SOME SHIT LIKE THAT HERE -->
            </div>
        </div><!-- /.row -->
    </div><!-- /.container -->
</section><!-- /.USER PROFILE CONTENT -->

<g:if test="${isCurrentUser}">
    <!-- Profile Image Modal -->
    <div class="modal fade" id="imageModal" tabindex="-1" role="dialog" aria-labelledby="imageModal" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title" id="imageModalLabel"> <g:message code="label.upload.image"/></h4>
                </div>
                <g:uploadForm name="upload-form" controller="userProfile" action="upload" class="no-margin" enctype="multipart/form-data">
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-sm-4">
                                <div id="profileImage" class="fileinput fileinput-new text-center">
                                    <div class="fileinput-new user-avatar avatar-circle-lg">
                                        <g:if test="${user.profileImage}">
                                            <g:fileArchiveUserImage size="large" id="${user.id}"/>
                                        </g:if>
                                        <g:else>
                                            <img class="img-responsive" src="resource(dir: 'images', file: 'avatar_default.png')"/>
                                        </g:else>
                                    </div>
                                    <div class="fileinput-preview fileinput-exists avatar-circle-lg img-responsive"></div>
                                    <div class="top-margin20">
                                        <span class="btn btn-default btn-file btn-xs"><span class="fileinput-new"><g:message code="button.choose.image"/></span><span class="fileinput-exists"><g:message code="button.change.image"/></span><input type="file" name="profileImage"></span>
                                        <!-- <g:if test="${user.profileImage}">
                                            <button class="btn btn-xs btn-danger"><g:message code="button.remove.image"/></button>
                                        </g:if> -->
                                    </div>

                                </div>
                            </div>
                            <div class="col-sm-8 vertical-padding20">
                                <h4>
                                    <i class="fas fa-info-circle"></i> <g:message code="userProfile.index.message3"/>
                                </h4>
                                <p><g:message code="userProfile.index.message4"/>:</p>
                                <ul>
                                    <li><g:message code="userProfile.index.message5"/></li>
                                    <li><g:message code="userProfile.index.message6"/></li>
                                    <li><g:message code="userProfile.index.message7"/></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-md btn-default" data-dismiss="modal"><g:message code="button.cancel.label"/></button>
                        <button type="submit" class="btn btn-md btn-success"><g:message code="button.save.label"/></button>
                    </div>
                </g:uploadForm>
            </div>
        </div>
    </div>

    <!-- Welcome Image Modal -->
    <div class="modal fade" id="welcomeImageModal" tabindex="-1" role="dialog" aria-labelledby="welcomeImageModal" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title" id="welcomeImageModalLabel"><g:message code="label.upload.image"/></h4>
                </div>
                <g:uploadForm name="upload-form" controller="userProfile" action="uploadWelcome" class="no-margin" enctype="multipart/form-data">
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-sm-4">
                                <div id="welcomeImage" class="fileinput fileinput-new text-center">
                                    <div class="fileinput-new user-avatar avatar-square-lg">
                                        <img class="img-responsive" alt=""/>
                                    </div>
                                    <div class="fileinput-preview fileinput-exists avatar-square-lg"></div>
                                    <div class="top-margin20">
                                        <span class="btn btn-default btn-file btn-xs"><span class="fileinput-new"><g:message code="button.choose.image"/></span><span class="fileinput-exists"><g:message code="button.change.image"/></span><input type="file" name="welcomeImage"></span>
                                    </div>
                                </div>
                            </div>
                            <div class="col-sm-8 vertical-padding20">
                                <h4>
                                    <i class="fas fa-info-circle"></i> <g:message code="userProfile.index.message3"/>
                                </h4>
                                <p><g:message code="userProfile.index.message4"/>:</p>
                                <ul>
                                    <li><g:message code="userProfile.index.message5"/></li>
                                    <li><g:message code="userProfile.index.message11"/></li>
                                    <li><g:message code="userProfile.index.message7"/></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-md btn-default" data-dismiss="modal"><g:message code="button.cancel.label"/></button>
                        <button type="submit" class="btn btn-md btn-success"><g:message code="button.save.label"/></button>
                    </div>
                </g:uploadForm>
            </div>
        </div>
    </div>

    <!-- Availability Modal -->
    <div class="modal fade" id="availabilityModal" tabindex="-1" role="dialog" aria-labelledby="availabilityModal" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title" id="availabilityModalLabel"><g:message code="userProfile.index.message12"/></h4>
                </div>
                <g:form action="updateAvailability" name="updateAvailability" class="form-horizontal" role="form">
                    <div class="modal-body">
                        <g:each in="${(1..7)}">
                            <div class="row select-availability disabled" day="${it}">
                                <div class="col-sm-3 ">
                                    <input type="hidden" class="fromHour" name="fromHour_${it}" />
                                    <input type="hidden" class="toHour" name="toHour_${it}" />
                                    <input type="hidden" class="active" name="active_${it}" />

                                    <div class="checkbox">
                                        <g:checkBox class="pull-none activeControl" name="active_${it}" day="${it}" data-title="${message(code: 'userProfile.index.message13')}" rel="tooltip"/>
                                        <label for="active_${it}"><g:message code="time.weekDay.plural.${it}"/></label>
                                    </div>
                                </div>
                                <div class="col-sm-4 ">
                                    <span class="small text-muted right-margin5"><g:message code="userProfile.index.message14"/> </span>
                                    <label class="timeLabel" day="${it}" style="margin-top: 9px;">00:00 - 23:00</label>
                                </div>

                                <div class="col-sm-5 ">
                                    <div class="timeFilter slider-input" day="${it}" style="margin-top: 12px;"></div>
                                </div>
                            </div>
                        </g:each>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-md btn-default" data-dismiss="modal"><g:message code="button.cancel.label"/></button>
                        <g:submitButton name="submit" class="btn btn-md btn-success" value="${message(code: "button.save.label")}"/>
                    </div>
                </g:form>
            </div>
        </div>
    </div>

    <!-- Sport Profile Modal -->
    <div class="modal fade" id="sportProfileModal" tabindex="-1" role="dialog" aria-labelledby="sportProfileModal" aria-hidden="true"></div>
</g:if>
<g:else>
    <!-- Message Modal -->
    <div class="modal fade" id="messageModal" tabindex="-1" role="dialog" aria-labelledby="messageModal" aria-hidden="true"></div>
</g:else>

<r:script>
    $(document).ready(function() {
        var $activeControl = $(".activeControl");
        $("[rel=tooltip]").tooltip({ delay: { show: 1000, hide: 100 } });

    <g:if test="${isCurrentUser}">
    var $maximumPoints  = 100;

    var $hasName = 0;
    var $hasBirthday = 0;
    var $hasGender = 0;
    var $hasEmail = 0;
    var $hasPhone = 0;
    var $hasAddress = 0;
    var $hasDescription = 0;
    var $hasSportProfiles = 0;
    var $hasAvailability = 0;
    var $hasFavorites = 0;

    $hasName = 10;
    $hasEmail = 10;

    <g:if test="${user.birthday}">
        $hasBirthday = 10;
    </g:if>

    <g:if test="${user.gender}">
        $hasGender = 10;
    </g:if>

    <g:if test="${user.telephone}">
        $hasPhone = 10;
    </g:if>

    <g:if test="${user.address && user.zipcode && user.city && user.municipality}">
        $hasAddress = 10;
    </g:if>

    <g:if test="${user.description}">
        $hasDescription = 10;
    </g:if>

    <g:if test="${userAvailableSportProfiles?.size() > 0}">
        $hasSportProfiles = 10;
    </g:if>

    <g:if test="${user.availabilities?.size() > 0}">
        $hasAvailability = 10;
    </g:if>

    <g:if test="${user.favourites?.size() > 0}">
        $hasFavorites = 10;
    </g:if>

    var $percentage = ($hasName + $hasBirthday + $hasGender + $hasEmail + $hasPhone + $hasAddress + $hasDescription + $hasSportProfiles + $hasAvailability + $hasFavorites)*$maximumPoints/100;

    console.log ("user profile completeness is " + $percentage + "%");

    var $pb = $('.profile-completeness.progress .progress-bar');

    $pb.attr('data-transitiongoal', $percentage).progressbar({transition_delay: 1000});
    $("#precentage").text($percentage);

</g:if>

    <g:if test="${isCurrentUser}">
        $("#profileImage").fileinput({'showUpload':false, 'previewFileType':'any'});
        $("#welcomeImage").fileinput({'showUpload':false, 'previewFileType':'any'});
    </g:if>

    $("#sportSelect").selectpicker();

    $activeControl.tooltip({ delay: { show: 1000, hide: 100 } });

    $activeControl.on("change", function() {
        var $this = $(this);
        var $selectAvailability = $this.closest(".select-availability");
        var $timeFilter = $selectAvailability.find(".timeFilter");

        if($this.is(":checked")) {
            $selectAvailability.removeClass("disabled");
            $timeFilter.slider( "option", "disabled", false );
        } else {
            $selectAvailability.addClass("disabled");
            $timeFilter.slider( "option", "disabled", true );
        }
    });

    $( ".timeFilter" ).slider({
        range: true,
        min: 0,
        max: 23,
        values: [ 0, 23 ],
        slide: function( event, ui ) {
            var weekDay   = $(this).attr("day");
            var $this      = $(".select-availability[day=" + weekDay + "]");
            var $timeLabel = $this.find(".timeLabel");
            var $fromHour  = $this.find(".fromHour");
            var $toHour    = $this.find(".toHour");

            var value1 = formatHour(ui.values[ 0 ]);
            var value2 = formatHour(ui.values[ 1 ]);

            $fromHour.val(ui.values[ 0 ]);
            $toHour.val(ui.values[ 1 ]);

            $timeLabel.html(value1 + ":00 - " + value2 + ":00");
        },
        create: function(event, ui) {
            var weekDay   = $(this).attr("day");
            var $this      = $(".select-availability[day=" + weekDay + "]");
            var $fromHour  = $this.find(".fromHour");
            var $toHour    = $this.find(".toHour");

            $fromHour.val("0");
            $toHour.val("23");
        }
    });

    <g:each in="${user.availabilities}">
        var $this = $(".select-availability[day=${g.forJavaScript(data: it.weekday)}]");

                var $timeFilter = $this.find(".timeFilter");
                var $timeLabel  = $this.find(".timeLabel");
                var $active     = $this.find(".activeControl");
                var $fromHour  = $this.find(".fromHour");
                var $toHour    = $this.find(".toHour");

                $timeFilter.slider("values", 0, "${g.forJavaScript(data: it.begin.toString("HH"))}");
                $timeFilter.slider("values", 1, "${g.forJavaScript(data: it.end.toString("HH"))}");
                $fromHour.val("${g.forJavaScript(data: it.begin)}");
                $toHour.val("${g.forJavaScript(data: it.end)}");

                var value1 = formatHour($timeFilter.slider( "values", 0 ));
                var value2 = formatHour($timeFilter.slider( "values", 1 ));
                $timeLabel.html(value1 + ":00 - " + value2 + ":00");

        <g:if test="${it.active}">
            $active.attr("checked", true);
            $active.attr("value", true);
            $this.removeClass("disabled");
        </g:if>
    </g:each>

    function updateLevel () {
        $(this).parent('form').find('input#level').value($(this.val()));
    }
    function formatHour(hour) {
        return (hour > 9 ?hour:"0"+hour)
    }
});
</r:script>
</body>
</html>
