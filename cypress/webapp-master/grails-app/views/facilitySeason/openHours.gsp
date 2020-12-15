<%@ page import="org.joda.time.LocalTime; com.matchi.Season; org.joda.time.DateTimeConstants; com.matchi.Court; com.matchi.Sport; org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilitySeason.openHours.title"/></title>
    <r:require modules="jquery-validate,jquery-timepicker"/>
</head>
<body>

<h2>Öppetider ${season.name}</h2>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="season.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:link action="edit" id="${season.id}"><g:message code="facilitySeason.edit.heading"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilitySeason.openHours.edit"/></li>
</ul>
<g:errorMessage bean="${cmd}"/>

<div id="errorMessage" class="alert alert-error" style="display: none;">
    <a class="close" data-dismiss="alert" href="#">×</a>

    <h4 class="alert-heading">${ new LocalTime().toString("HH:mm:ss") }: <g:message code="default.error.heading"/></h4>

    <g:renderErrors bean="${bean}" as="list" />
</div>


<g:form id="${season.id}"  name="seasonForm" action="confirmOpenHours" method="post" class="form-horizontal form-well">
    <g:hiddenField name="form" value="true"/>

    <div class="form-header">
        <g:message code="facilitySeason.openHours.edit"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group">
            <label style="font-size: 20px;"><g:message code="facilitySeason.openHours.message6"/></label>
            <table class="weekdays" width="900">
                <tr>
                    <th width="180"></th>
                    <g:each in="${form.weekDays}">
                        <th style="text-align: left;"><g:message code="time.shortWeekDay.${it}"/></th>
                    </g:each>
                </tr>
                <g:each in="${Sport.list()}" var="sport">
                    <%
                        def courtCriteria = Court.createCriteria()
                        def courts = courtCriteria {
                            eq("facility", form.facility)
                            eq("sport", sport)
                            eq("archived", false)
                        }

                        def startHour = new DateTime().withHourOfDay(form.openingHour).withMinuteOfHour(0).toString("HH:mm")
                        def endHour = new DateTime().withHourOfDay(form.closingHour).withMinuteOfHour(0).toString("HH:mm")
                    %>
                    <g:if test="${courts}">
                        <tr>
                            <td colspan="8">
                                <span>
                                    <big><strong>
                                        <a id="${sport.id}" href="javascript:void(0)"
                                           onclick="toggleCourts(${sport.id})"><g:message code="sport.name.${sport.id}"/> (${courts.size()} ${courts.size()==1 ? message(code: 'facilitySeason.openHours.message13') : message(code: 'facilitySeason.openHours.message14')})</a> <i id="toggleMarker_${sport.id}" class="icon-chevron-right"></i></strong></big>
                                </span>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div class="padding10">
                                    <label for="_courtinfo"><g:message code="facilitySeason.openHours.message7"/></label>
                                    <g:textField readonly="true" name="_courtinfo" class="timepicker _courtinfo courtlength span1" id="${'1/' + sport.id}" value="01:00"/><br><br>
                                    <label for="_courtinfo"><g:message code="facilitySeason.openHours.message8"/></label>
                                    <g:textField readonly="true" name="_courtinfo" class="timepicker _courtinfo courtinterval span1" id="${'2/' + sport.id}" value="00:00"/>
                                </div>
                            </td>
                            <g:each in="${form.weekDays}">
                                <td>
                                    <div>
                                        <label for="_sports"><g:message code="default.from.label"/></label>
                                        <g:textField readonly="true" name="_sports" class="timepicker _sports sport span1" id="${sport.id + '/' + it +'/1'}" value=""/><br><br>
                                        <label for="_sports"><g:message code="facilitySeason.openHours.message10"/></label>
                                        <g:textField readonly="true" name="_sports" class="timepicker _sports sport span1" id="${sport.id + '/' + it +'/2'}" value=""/>
                                    </div>
                                </td>
                            </g:each>
                        </tr>
                        <g:each in="${courts}" var="court" status="i">
                            <tr class="${sport.id}_courts" style="display: none;" bgcolor="${i%2 == 0 ? '#EEEEEE' : ''}">
                                <td colspan="8" style="padding: 10px 0 0 10px;">
                                    <strong>${court.name}</strong>
                                </td>
                            </tr>
                            <tr class="${sport.id}_courts" style="display: none;" bgcolor="${i%2 == 0 ? '#EEEEEE' : ''}">
                                <td>
                                    <!-- ${openingHours.get("${it}_${court.id}")?.bookingLength?.toString()?:"01:00"}-->
                                    <div class="courtrow" style="padding: 10px 0 0 10px;">
                                        <g:textField readonly="true" name="_bookingLength/${court.id}" class="timepicker _courtinfo span1"
                                                     id="${court.id + '/1/' + sport.id + '/length'}" value="${period(period: openingHours.get("${it}_${court.id}")?.bookingLength, default: "01:00")}"/><br><br>
                                        <g:textField readonly="true" name="_timeBetween/${court.id}" class="timepicker _courtinfo span1"
                                                     id="${court.id + '/2/' + sport.id + '/interval'}" value="${period(period: openingHours.get("${it}_${court.id}")?.timeBetween, default: "00:00")}"/>
                                    </div>
                                </td>
                                <g:each in="${form.weekDays}">
                                    <td>
                                        <div class="courtrow vertical-padding10">
                                            <g:textField readonly="true" name="_courts/${court.id + '/' + it + '/1'}" class="timepicker _courts span1"
                                                         id="${'court_' + sport.id + '/' + it + '/1'+ '/' + court.id}" value="${openingHours.get("${it}_${court.id}")?.opens?.toString("HH:mm")?:startHour}"/><br><br>
                                            <g:textField readonly="true" name="_courts/${court.id + '/' + it + '/2'}" class="timepicker _courts span1"
                                                         id="${'court_' + sport.id  + '/' + it + '/2'+ '/' + court.id}" value="${openingHours.get("${it}_${court.id}")?.closes?.toString("HH:mm")?:endHour}"/>
                                        </div>
                                    </td>
                                </g:each>
                            </tr>
                        </g:each>
                    </g:if>
                </g:each>
            </table>
        </div>
        <div class="pull-left">

        </div>
        <div class="form-actions">
            <p class="help-block" id="status-message"></p>
            <g:submitButton name="save" value="${message(code: 'facilitySeason.openHours.message11')}" class="btn btn-success"/>
            <g:link action="edit" id="${season.id}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
<!--
<jqvalui:renderValidationScript for="com.matchi.season.CreateSeasonCommand"
                                form="seasonForm" errorClass="invalid" validClass="success" onsubmit="true"
                                renderErrorsOnTop="true" />
-->


<r:script>
    $(document).ready(function() {
        $('#seasonForm').preventDoubleSubmission({});

        $('.timepicker').addTimePicker({
            hourText: '${message(code: 'default.timepicker.hour')}',
            minuteText: '${message(code: 'default.timepicker.minute')}'
        });

        var startDate = new Date($("#startTime").val());

        $("#endTime").datepicker({
            autoSize: true,
            dateFormat: 'yy-mm-dd',
            minDate: new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate() + 1)
        });

        $(".courtlength").on('change', function() {
            var id  = $(this).attr("id");
            var val = $(this).attr("value");

            $("input[id*='" + id + "/length']").val(val);
        });
        $(".courtinterval").on('change', function() {
            var id  = $(this).attr("id");
            var val = $(this).attr("value");

            $("input[id*='" + id + "/interval']").val(val);
        });
        $("._sports").on('change', function() {
            var id  = $(this).attr("id");
            var val = $(this).attr("value");

            $("input[id*='court_" + id + "']").val(val);
        });




        $('#seasonForm').validate({
            errorLabelContainer: "#errorMessage",
            onkeyup: true,
            errorClass: 'invalid',
            validClass: 'success',
            onsubmit: true,

            success: function(label) {
                //$('[id="' + label.attr('for') + '"]').qtip('destroy');
            },
            //To reset preventDoubleSubmission()
            invalidHandler: function() {
                $('#seasonForm').enableSubmission();
            }/*,
             errorPlacement: function(error, element) {
             if ($(error).text())
             error.appendTo( element.parent().next() );

             $(element).filter(':not(.success)').qtip({
             overwrite: true,
             content: error,
             position: { my: 'left center', at: 'right center' },
             show: {
             event: false,
             ready: true
             },
             hide: false,
             style: {
             widget: false,
             classes: 'ui-tooltip-red ui-tooltip-shadow ui-tooltip-rounded',
             tip: true
             }
             });
             }*/
        });

        $("input.timepicker").each(function() {
            if(!$(this).hasClass("_sports")) {
                $(this).rules("add", {firstTimeLargerThenSecond:true});
            }
        });
    });

    $.validator.addMethod(
            "firstTimeLargerThenSecond",
            function(value,element) {
                var td = $(element).closest("td");

                var first = new Date(new Date().toDateString() + ' ' + td.children().find("input").eq(0).val())
                var second = new Date(new Date().toDateString() + ' ' + td.children().find("input").eq(1).val());

                if($(element).hasClass('_courtinfo')) {
                    return true;
                }

                if (second >= first) {
                    return true
                }

                return false;
            }, "First time can't be later/greater then second one");

    function toggleCourts(id) {

        var toggleMarker = $('#toggleMarker_'+ id)
        if(toggleMarker.hasClass('icon-chevron-right')) {
            toggleMarker.removeClass('icon-chevron-right')
            toggleMarker.addClass('icon-chevron-down')
        } else {
            toggleMarker.removeClass('icon-chevron-down')
            toggleMarker.addClass('icon-chevron-right')
        }

        if($('.' + id + '_courts').is(":visible")) {
            $('.' + id + '_courts').hide()
        } else {
            $('.' + id + '_courts').show();
        }
    }


    (function($){
        $.fn.extend({
            //plugin name - animatemenu
            seasonChangeStatus: function(options) {

                var defaults = {
                };

                var options = $.extend(defaults, options);

                return this.each(function() {
                    var form = $(this);


                    $("._courts", form).on('change', function() {
                        $("#status-message").html("<img src='<g:resource dir="images" file="spinner.gif"/>'/>  Laddar... ");

                        $.ajax({type:'POST', url: '<g:createLink absolute="false" id="${g.forJavaScript(data: season.id)}" action="openHoursChangeStatus"/> ', data:form.serialize(),

                            success: function(response) {
                                $("#status-message").html(response)
                            },

                            error: function(response) {
                                $("#status-message").html("<h3>${message(code: 'facilitySeason.openHours.message12')}")
                            }

                        });


                    });
                });
            }
        });
    })(jQuery);

    $("#seasonForm").seasonChangeStatus();

</r:script>
</body>
</html>
