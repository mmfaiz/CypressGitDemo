<%@ page import="java.text.SimpleDateFormat; com.matchi.User" %>
<html>
<head>
    <meta name="layout" content="b3main" />
    <meta name="showFacebookNagger" content="${true}"/>
    <meta name="hideNotifications" content="${true}"/>
    <title><g:message code="button.edit.profile"/> - MATCHi</title>
</head>
<body>
<g:b3StaticErrorMessage bean="${cmd}"/>
<g:b3StaticErrorMessage bean="${user}"/>

<section class="block vertical-padding30">

    <div class="container">
        <h2 class="page-header no-top-margin"><i class="fas fa-user"></i> <g:message code="userProfile.edit.message3"/></h2>

        <div id="accountEditInfoMessage" class="alert alert-info vertical-margin20" style="display: none;">
            <button type="button" class="close" onclick="removeAccountEditInfoMessage()" data-dismiss="alert" rel="tooltip" title="<g:message code="default.do.not.show.again"/>"><span aria-hidden="true">&times;</span><span class="sr-only"><g:message code="button.close.label"/></span></button>
            <p>
                <span class="fa-stack fa-lg">
                    <i class="fas fa-circle fa-stack-2x"></i>
                    <i class="fa fa-lightbulb-o fa-stack-1x fa-inverse"></i>
                </span>
                <g:message code="userProfile.edit.message4"/></p>
        </div>
        <div class="space-20"></div>

        <g:form action="update" name="updateProfileForm" class="form">
            <strong><g:message code="userProfile.edit.message5"/></strong>
            <hr class="top-margin5">

            <div class="row">
                <div class="col-md-6">
                    <div class="form-group ${hasErrors(bean:cmd, field:'firstname', 'has-error')}">
                        <label for="firstname"><g:message code="user.firstname.label"/> <g:publicInfoLabel /></label>
                        <g:textField name="firstname" value="${cmd?.firstname}" placeholder="${message(code: 'userProfile.edit.message7')}" class="form-control"/>
                    </div>

                    <div class="form-group ${hasErrors(bean:cmd, field:'email', 'has-error')}">
                        <label for="email"><g:message code="user.email.label"/> <g:privateInfoLabel/></label>
                        <g:textField name="email" value="${cmd?.email}" placeholder="${message(code: 'userProfile.edit.message11')}" class="form-control"/>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="form-group ${hasErrors(bean:cmd, field:'lastname', 'has-error')}">
                        <label for="lastname"><g:message code="user.lastname.label"/> <g:publicInfoLabel /></label>
                        <g:textField name="lastname" value="${cmd?.lastname}" placeholder="${message(code: 'userProfile.edit.message9')}" class="form-control"/>
                    </div>

                    <g:if test="${user?.facebookUID != null}">
                        <div class="form-group">
                            <label></label>
                            <g:link controller="userProfile" action="passwordForFb" class="btn btn-primary form-control">
                                <g:message code="user.profile.facebook.setPassword"/>
                            </g:link>
                        </div>
                    </g:if>
                </div>
            </div>
            <g:link target="_new" controller="home" action="privacypolicy"><small><i class="fa fa-file-text fa-fw"></i> <g:message code="userProfile.integritypolicy"/></small></g:link>

            <%
                def months = new java.text.DateFormatSymbols().shortMonths
                def today = new Date()
                def userBirthYear = "1970"
                def userBirthMonth = "1"
                def userBirthDay = "1"

                if(user.birthday) {
                    userBirthYear = new SimpleDateFormat("yyyy").format(user.birthday)
                    userBirthDay = new SimpleDateFormat("dd").format(user.birthday)
                    userBirthMonth = months[user?.birthday?.month]
                }

            %>
            <div class="row top-margin20">
                <div class="col-md-12">
                    <strong><g:message code="userProfile.edit.message16"/></strong>
                    <hr class="top-margin5">
                </div>
                <div class="col-md-6">
                    <div class="form-group ${hasErrors(bean:cmd, field:'telephone', 'has-error')}">
                        <label for="telephone"><g:message code="default.cellphone.label"/> <g:privateInfoLabel/></label>
                        <g:textField name="telephone" value="${cmd?.telephone}" placeholder="${message(code: 'default.phoneNumber.label')}" class="form-control"/>
                    </div>
                    <div class="form-group">
                        <label for="birthYear"><g:message code="userProfile.edit.message15"/> <g:privateInfoLabel/></label><br>
                        <g:select id="birthYear" data-style="form-control" data-width="25%" from="${1930..(today[Calendar.YEAR])}" name="birthYear" value="${user ? userBirthYear : cmd?.birthYear}"/>
                        <g:select id="birthMonth" data-style="form-control" data-width="25%" from="${months}" name="birthMonth" value="${user ? userBirthMonth : cmd?.birthMonth}"/>
                        <g:select id="birthDay" data-style="form-control" data-width="25%" from="${1..31}" name="birthDay" value="${user ? userBirthDay : cmd?.birthDay}"/>
                    </div>
                    <div class="form-group">
                        <label for="address"><g:message code="default.address.label"/> <g:privateInfoLabel/></label>
                        <div class="controls">
                            <g:textField name="address" value="${cmd?.address}" placeholder="${message(code: 'userProfile.edit.message18')}" class="form-control"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="city"><g:message code="userProfile.edit.message21"/> <g:publicInfoLabel/></label>
                        <div class="controls">
                            <g:textField name="city" value="${cmd?.city}" placeholder="${message(code: 'userProfile.edit.message22')}" class="form-control"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="country"><g:message code="default.country.label"/> <g:privateInfoLabel/></label>
                        <div class="controls">
                            <g:select id="country" name="country" from="${grailsApplication.config.matchi.settings.available.countries}"
                                      valueMessagePrefix="country" value="${cmd?.country}" noSelection="['':message(code: 'userProfile.edit.message29')]"
                                      class="form-control"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="nationality"><g:message code="default.nationality.label"/> <g:privateInfoLabel/></label>
                        <div class="controls">
                            <g:select id="nationality" name="nationality" from="${grailsApplication.config.matchi.settings.available.countries}"
                                      valueMessagePrefix="country" value="${cmd?.nationality}" noSelection="['':message(code: 'userProfile.edit.message30')]"
                                      class="form-control"/>
                        </div>
                    </div>
                    <g:submitButton value="${message(code: 'button.save.label')}" name="save" class="btn btn-success col-sm-6 hidden-sm hidden-xs"/>
                </div>
                <div class="col-md-6">
                    <div class="form-group">
                        <label for="gender"><g:message code="default.gender.label"/> <g:privateInfoLabel/></label><br>
                        <g:select id="gender"
                                  data-style="form-control"
                                  from="${User.Gender}"
                                  optionValue="name"
                                  name="gender"
                                  value="${cmd?.gender}"
                                  noSelection="${['': message(code: 'facilityCourseParticipant.index.genders.noneSelectedText')]}"
                        />
                    </div>

                    <div class="form-group">
                        <label for="language"><g:message code="user.language.label"/> <g:privateInfoLabel/></label>
                        <div class="controls">
                            <g:select name="language" from="${grailsApplication.config.i18n.availableLanguages}"
                                      optionKey="key" optionValue="value" value="${cmd?.language}" data-style="form-control"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="zipcode"><g:message code="userProfile.edit.message19"/> <g:privateInfoLabel/></label>
                        <div class="controls">
                            <g:textField name="zipcode" value="${cmd?.zipcode}" placeholder="${message(code: 'userProfile.edit.message20')}" class="form-control"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="municipality"><g:message code="municipality.label"/> <g:publicInfoLabel /></label><br>
                        <select id="municipality" name="municipality" data-style="form-control" data-live-search="true" title="${message(code: 'municipality.multiselect.noneSelectedText')}">
                            <option value></option>
                            <g:each in="${regions}">
                                <optgroup label="${it.name}">
                                    <g:each in="${it.municipalities}" var="municipality">
                                        <option value="${municipality.id}" ${(cmd?.municipality == municipality.id || cmd?.municipality?.id == municipality.id) ? "selected":""}>${municipality.name}</option>
                                    </g:each>
                                </optgroup>
                            </g:each>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="description"><g:message code="userProfile.edit.description"/> <g:publicInfoLabel /></label>
                        <div class="controls">
                            <g:textArea class="form-control" rows="10" cols="10" name="description" value="${cmd?.description}" placeholder="${message(code: 'userProfile.edit.message26')}"/>
                        </div>
                    </div>
                    <Test></Test>
                    <g:submitButton value="${message(code: 'button.save.label')}" name="save" class="btn btn-success col-xs-12 visible-sm visible-xs"/>
                </div>
            </div>
        </g:form>
    </div>
    <div class="space-40"></div>
</section>


<r:script>
    $(document).ready(function() {
        $("#gender").selectpicker();
        $("#birthYear").selectpicker();
        $("#birthMonth").selectpicker();
        $("#birthDay").selectpicker();
        $("#language").selectpicker();
        $("#municipality").selectpicker({
            title: "${message(code: 'municipality.multiselect.noneSelectedText')}"
        });
        $("#country").selectpicker({
            title: "${message(code: 'userProfile.edit.message29')}"
        });
        $("#nationality").selectpicker({
            title: "${message(code: 'userProfile.edit.message30')}"
        });

        $("#modalToggle").on("click", function() {
            $("#imageModal").modal("toggle");
        });

        $("[rel=tooltip]").tooltip();

        if(!getCookie("hideFacebookNagger")) {
             $("#fbConnect").show();
        }
        if(!getCookie("hideAccountEditInfo")) {
             $("#accountEditInfoMessage").show();
        }
    });
</r:script>
</body>
</html>
