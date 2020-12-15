<%@ page import="com.matchi.sportprofile.SportProfileMindset; com.matchi.sportprofile.SportProfileAttribute; com.matchi.sportprofile.SportProfile;" %>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4 class="modal-title" id="addSportModalLabel">
                <g:message code="templates.profile.sportAdd.message1"/> <g:message code="sport.name.${sport.id}"/> <i class="icon-sport ${sport.id}"></i>
            </h4>
        </div>

        <g:form action="updateSport" name="updateSport" class="form-horizontal" role="form">
            <div class="modal-body">
                <g:hiddenField name="sport" value="${sport?.id}"/>

                <!-- SPELSÄTT -->
                <h5><legend class="text-muted"><g:message code="templates.profile.sportAdd.message2"/></legend></h5>

                <div class="form-group">
                    <g:each in="${SportProfileMindset.list()}" var="mindSet" status="i">
                        <div class="col-sm-4">
                            <div class="checkbox checkbox-info checkbox-inline">
                                <g:checkBox id="${mindSet}" name="mindset" value="${mindSet}" checked="${mindSet in profile?.mindSets}"/>
                                <label for="${mindSet}">
                                    <g:message code="sportprofile.mindset.${mindSet}"/>
                                </label>
                            </div>
                        </div>
                    </g:each>
                </div>

                <div class="vertical-margin10 clearfix"></div>

                <!-- FREKVENS -->
                <h5><legend class="text-muted"><g:message code="templates.profile.sportAdd.message3"/></legend></h5>

                <select id="frequency" name="frequency" class="form-control">
                    <g:each in="${SportProfile.Frequency.list()}">
                        <option value="${it}" ${profile?.frequency == it ? "selected":""}>
                            <g:message code="sportprofile.frequency.${it}"/>
                        </option>
                    </g:each>
                </select>

                <div class="vertical-margin10 clearfix"></div>

                <!-- SKICKLIGHET -->
                <h5><legend class="text-muted"><g:message code="templates.profile.sportAdd.message4"/></legend></h5>

                <div class="form-group">
                    <label for="level" class="col-sm-3">
                        <g:message code="sportprofile.skilllevel.name" />
                    </label>
                    <div class="col-sm-8 slider-black">
                        <input type="text" name="level" data-slider-min="1" data-slider-max="10" data-slider-step="1" data-slider-value="${profile?.skillLevel ?: 5}"/>
                    </div>
                    <div class="col-sm-1 text-right">
                        <g:inputHelp title="${message(code: "sportprofile.skilllevel.description", args: ["${sport?.name}"])}"/>
                    </div>
                </div>
                <div class="text-muted skill-level-text">
                    <span class="skill-level-description"><g:message code="sport.skillLevel.${sport.id}.${profile?.skillLevel ?: 5}" default="" /></span>
                </div>

                <div class="vertical-margin10 clearfix"></div>

                <!-- ATTRIBUT -->
                <h5><legend class="text-muted"><g:message code="templates.profile.sportAdd.message5"/></legend></h5>

                <g:each in="${sport?.sportAttributes}" var="attr">
                    <div class="form-group">
                        <label for="level_${attr.id}" class="col-sm-3">
                            ${message(code: "sportattribute.name.${attr}")}
                        </label>
                        <div class="col-sm-8 slider-yellow">
                            <%
                                SportProfileAttribute profileAttr = profile?.getSportProfileAttribute(attr.id)
                            %>
                            <input name="level_${attr.id}" type="text" data-slider-min="0" data-slider-max="10" data-slider-step="1" data-slider-value="${profileAttr?.skillLevel ?: 0}"/>
                        </div>
                        <div class="col-sm-1 text-right">
                            <g:inputHelp title="${message(code: "sportattribute.description.${attr}")}"/>
                        </div>
                    </div>
                </g:each>

                <div class="vertical-margin10 clearfix"></div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-md btn-default" data-dismiss="modal">${message(code: 'button.cancel.label')}</button>
                <g:submitButton name="submit" class="btn btn-md btn-success" value="${message(code: 'button.save.label')}"/>
            </div>
        </g:form>
    </div>
</div>
<r:script>
    $("[rel=tooltip]").tooltip();

    <g:each in="${sport?.sportAttributes}">
        $('input[name=level_${g.forJavaScript(data: it.id)}]').bootstrapSlider({});
    </g:each>

    $('input[name=level]').bootstrapSlider({
        tooltip: 'always'
    });

    var sportDescriptions = []
    <g:each in="${(1..10)}" var="i">
        sportDescriptions[${i}] = "<g:message code="sport.skillLevel.${sport.id}.${i}" default=""/>";
    </g:each>

    $('input[name=level]').on("change", function(slideEvt) {

        var value = $('input[name=level]').bootstrapSlider('getValue');

        $('.skill-level-description').text(sportDescriptions[value]);
    });

    $("#frequency").selectpicker({
        title: "${message(code: 'templates.profile.sportAdd.message6')}"
    });
</r:script>
</body>
</html>
