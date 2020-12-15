<%@ page import="com.matchi.CouponService; com.matchi.enums.BookingGroupType; com.matchi.PriceListService; com.matchi.Season; org.joda.time.DateTime; com.matchi.BookingGroup; com.matchi.DateUtil; com.matchi.play.Recording" %>
<%
    def subscription = slots[0].subscription
    def booking = slots[0]?.booking?:null
    def headline = (booking ? message(code: 'button.update.label') :
            message(code: 'facilityBooking.facilityBookingForm.message4', args: [slots?.size()]))
    def isShowPlayers = (!booking?.group?.type || booking?.group?.type == BookingGroupType.SUBSCRIPTION || booking?.group?.type == BookingGroupType.DEFAULT)
%>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3>${headline}</h3>
    <div class="clearfix"></div>
</div>
<g:form name="bookingForm" class="no-margin" controller="facilityBooking" action="book">
    <g:hiddenField name="returnUrl" value="${params.returnUrl ?: ""}"/>
    <g:hiddenField name="slotId" value="${slots.collect { it.id }.join(',')}"/>
    <g:hiddenField name="date" value="${params.date}"/>
    <g:hiddenField name="id" value="${booking?.id}"/>
    <g:hiddenField name="type" value="${booking?.group?.type ?: BookingGroupType.DEFAULT}"/>
    <g:hiddenField name="version" value="${booking?.version}"/>
    <div class="modal-body">
        <div class="alert alert-error" style="display: none;">
            <a class="close" data-dismiss="alert" href="#">×</a>
        </div>
        <fieldset>
            <div class="control-group">
                <g:if test="${isRestricted}">
                    <span class="text-warning">
                        <%
                            Date validFrom = new DateTime(slots?.first().startTime).minusMinutes(slots?.first().bookingRestriction?.validUntilMinBeforeStart).toDate()
                        %>
                        <g:if test="${slots?.size() > 1}">
                            <g:if test ="${sameRestrictions}">
                                <g:message code="facilityBooking.facilityBookingForm.isRestrictedSame.plural" args="[slots.first().getRequirementProfiles().collect { it.name }.join(', ')]"/>
                            </g:if>
                            <g:else>
                                <g:message code="facilityBooking.facilityBookingForm.isRestrictedDifferent.plural"/>
                            </g:else>
                        </g:if>
                        <g:else>
                            <g:if test="${validFrom.after(new Date())}">
                                <g:message code="facilityBooking.facilityBookingForm.isRestricted" args="[slots.first().getRequirementProfiles().collect { it.name }.join(', ')]"/>
                                <g:message code="facilityBooking.facilityBookingForm.validFrom" args="[validFrom.format('d MMMM HH:mm')]" />
                                <g:message code="facilityBooking.facilityBookingForm.removeRestriction"/>
                            </g:if>
                        </g:else>
                    </span>
                </g:if>
                <h3 style="line-height: 1.2em;"><g:slotCourtAndTime slot="${slots[0]}"/>
                    <g:if test="${slots.size() > 1}">
                        <small > <g:message code="facilityBooking.facilityBookingForm.message6" args="[slots.size()]"/></small>
                    </g:if>
                    <g:if test="${accessCode}">
                        <small>(<g:message code="facilityBooking.facilityBookingForm.message7"/>: ${accessCode})</small>
                    </g:if>
                    <g:if test="${subscription && (!booking || !booking?.customer?.id?.equals(subscription?.customer?.id))}">
                        <br><small>${message(code: 'facilityBooking.facilityBookingForm.message8')} # ${subscription?.customer?.number} - ${subscription?.customer?.fullName()}</small>
                    </g:if>
                </h3>
                <g:if test="${slots[0].court.description && slots[0].court.showDescriptionForAdmin}">
                    <div>${slots[0].court.description.encodeAsHTML()}</div>
                </g:if>
            </div>
            <g:if test="${!booking}">
                <div class="control-group">
                    <div class="controls controls-row">
                        <input type="hidden" id="customerSearch" name="customerSearch" class="required" value="${facility?.defaultBookingCustomer?.id}"/>
                        <a href="javascript: void(0)" id="new-customer-btn" class="new btn btn-small" style="vertical-align: top;">
                            <i class="icon-user icon"></i>
                            &nbsp;<g:message code="facilityBooking.facilityBookingForm.message9"/>
                        </a>
                        <script type="text/javascript">
                            $(function() {
                                $("#new-customer-btn").click(function() {
                                    var typeText = $.trim($("#typeSelect").text());
                                    var bookingType = $("#booking-type-list").find("a").filter(function() {
                                        return $(this).text() == typeText;
                                    }).attr("data-value");

                                    $.ajax({
                                        url: "${g.forJavaScript(data: createLink(controller: 'facilityBooking', action: 'newCustomerBookingForm'))}",
                                        type: "POST",
                                        data: {
                                            date: "${g.forJavaScript(data: params.date)}",
                                            slotId: "${g.forJavaScript(data: slots.collect { it.id }.join(','))}",
                                            bookingType: bookingType
                                        },
                                        success: function (data, textStatus) {
                                            $("#bookingModal").html(data);
                                        }
                                    });
                                    return false;
                                });
                            });
                        </script>

                        <div class="btn-group">
                            <a class="btn btn-small dropdown-toggle" data-toggle="dropdown" href="#" style="width: 101px"><i class="icon-list icon"></i>&nbsp;<span id="typeSelect"><g:message code="default.booking"/></span></a>
                            <a class="btn  btn-small dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                            <ul id="booking-type-list" class="dropdown-menu">
                                <g:each in="${bookingTypes}" var="type">
                                    <li><a href="javascript:void(0)" onclick="updateType('${type}', '${message(code: "bookingGroup.name.${type.toString()}")}');" data-value="${type}"><g:message code="bookingGroup.name.${type.toString()}"/></a></li>
                                </g:each>
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="clearfix"></div>
            </g:if>

            <div id="bookingFormInfo">
                <g:if test="${booking}">
                    <g:bookingFormInfo booking="${booking}" slots="${slots}" warnAboutCodeRequest="${warnAboutCodeRequest}"/>
                </g:if>
            </div>
            <div id="customerPayment" style="display: none"></div>

            <div id="trainers-wrapper" style="display: none">
                <g:message code="facilityBooking.facilityBookingForm.selectTrainer"/>
                <g:select class="form-control" optionKey="id" id="trainerId" name="trainerId" from="${trainers}"/>
            </div>

            <div id="recurrence" style="display: none">
                <g:bookingFormRecurrence slot="${slots[0]}"/>
            </div>

            <g:if test="${!booking || (booking?.players && isShowPlayers)}">
                <div id="players-wrapper" class="controls well" style="padding: 15px">
                    <a href="javascript:void(0)" onclick="togglePlayers();">
                        <h6>
                            <i class="icon-user"></i>
                            <g:message code="player.label.plural"/>
                            <i id="players-toggle-marker" class="icon-chevron-right"></i>
                        </h6>
                    </a>
                    <div id="players-wrapper-forms" style="display: none;">
                        <div class="space10"></div>
                        <a href="javascript: void(0)" class="btn add-player-btn">
                            + <g:message code="player.button.add"/>
                        </a>
                        <div class="players-list"></div>
                        <div id="player-template" class="row top-margin10" style="display: none">
                            <div class="span3 left">
                                <input type="hidden" name="playerCustomerId"/>
                            </div>
                            <div class="span1 right">
                                <label class="checkbox">
                                    <input type="checkbox" name="unknownPlayer" value="true"/>
                                    <g:message code="player.unknown.label"/>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>

            <div class="well well-small">
                <g:textArea class="span6 no-bottom-margin" rows="5" cols="50" name="comments" id="comments" value="${booking?.comments}" tabindex="3" placeholder="${message(code: 'facilityBooking.facilityBookingForm.message10')}" style="width: 500px;" maxlength="${com.matchi.BookingService.COMMENTS_MAX_INPUT_SIZE}"/>
                <div id="inputCounter">
                    <label><span id="inputCounterCurrent"></span> / <span id="inputCounterMax"></span></label>
                </div>
                <label class="checkbox" style="margin-top: 5px; display: ${!booking?.comments ? "none":""}">
                    <g:checkBox name="showComment" checked="${booking?.showComment}"/><g:message code="facilityBooking.facilityBookingForm.message11"/> ${facility.showBookingHolder ? message(code: 'facilityBooking.facilityBookingForm.message18') : ''}
                </label>
                <g:if test="${optionUpdateGroup}">
                    <label class="checkbox" for="updateGroup">
                        <g:checkBox id="updateGroup" name="updateGroup" tabindex="8"
                                    value="${false}"/><g:message code="facilityBooking.facilityBookingForm.message12"/>
                        <g:inputHelp
                                title="${message(code: 'facilityBooking.facilityBookingForm.message13')}"/>
                    </label>
                </g:if>
            </div>
            <g:if test="${!booking}">
                <div id="sendNotificationCheck" class="well well-small">
                    <label class="checkbox" for="sendNotification">
                        <g:checkBox id="sendNotification" name="sendNotification" tabindex="9" value="${facility.whetherToSendEmailConfirmationByDefault}"/><g:message code="facilityBooking.facilityBookingForm.message14"/>
                    </label>
                </div>
                <g:if test="${facility.hasMPC()}">
                    <div id="activateMpcCheck" class="well well-small no-bottom-margin" style="display: none;">
                        <label class="checkbox" for="activateMpc">
                            <g:checkBox name="activateMpc" tabindex="9" value="${false}"/><g:message code="facilityBooking.facilityBookingForm.mpcControl"/>
                        </label>
                    </div>
                </g:if>
            </g:if>
            <g:if test="${recording && recording.hasStarted()}">
                <div id="recordingLink" class="well well-small">
                    <g:if test="${recording.hasLive()}">
                        <i class="fa fa-video-camera" aria-hidden="true"></i> <a href="${recording.internalPlayerUrl}" target="_blank"><g:message code="facilityBooking.facilityBookingForm.viewLiveStream"/></a>

                    </g:if>
                    <g:elseif test="${recording.hasRecording() && recording.isPossiblyAccessed()}">
                        <i class="fa fa-video-camera" aria-hidden="true"></i> <a href="${recording.internalPlayerUrl}" target="_blank"><g:message code="facilityBooking.facilityBookingForm.viewRecording"/></a>
                        <g:if test="${!recording.isPurchased()}">
                            <g:message code="facilityBooking.facilityBookingForm.RecordingExpiresOn" /> <g:formatDate formatName="date.format.timeShort" date="${recording.lastViewableDate}" />
                        </g:if>
                    </g:elseif>
                </div>
            </g:if>
            <g:if test="${facility.hasMPC() && booking && booking.isNotAvailable()}">
                <div id="activateMpcCheck" class="well well-small no-bottom-margin">
                    <label class="checkbox" for="activateMpc">
                        <g:if test="${booking.getCodeRequest() != null}">
                            <g:checkBox name="activateMpc" tabindex="9" value="${true}" disabled="true"/><g:message code="facilityBooking.facilityBookingForm.mpcControl.activated"/>
                        </g:if>
                        <g:else>
                            <g:checkBox name="activateMpc" tabindex="9" value="${false}" disabled="true"/><g:message code="facilityBooking.facilityBookingForm.mpcControl.notActive"/>
                        </g:else>
                    </label>
                </div>
            </g:if>
        </fieldset>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton name="formSubmit" id="formSubmit" value="${!booking ? message(code: "button.book.label") : message(code: "button.save.label")}"
                            class="btn btn-md btn-success" onclick="return submitBookingForm()"/>

            <g:submitToRemote id="recurrenceSubmit" class="btn btn-md btn-success hidden"
                              controller="facilityBooking" action="confirmRecurrence" before="markAsPaidIfFreeBooking()" update="bookingModal" value="${message(code: "button.confirm.label")}"/>
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.closewindow.label" default="Avbryt"/></a>
            <g:if test="${booking && !booking?.group?.isType(BookingGroupType.ACTIVITY)}">
                <g:remoteLink id="cancelBtn" class="btn btn-md btn-inverse" controller="facilityBooking" action="cancelForm" update="bookingModal"
                              params="[ cancelSlotsData: booking.slot.id, returnUrl: params.returnUrl ?: '' ]"><span class="underline"><g:message code="button.unbook.label2"/></g:remoteLink>
            </g:if>
            <g:elseif test="${subscription}">
                <a id="rebookSubscription" href="javascript: void(0)" class="btn btn-md btn-info">
                    <g:message code="facilityBooking.facilityBookingForm.rebook"/>
                </a>
            </g:elseif>
            <g:if test="${booking && !booking.isActivity()}">
                <g:remoteLink id="extraInfo" class="btn btn-md btn-primary" controller="facilityBooking" action="extraInfo" update="bookingModal"
                              params="[ bookingId: booking.id ]"><g:message code="facilityBooking.facilityBookingForm.extraInfo"/></g:remoteLink>
            </g:if>
        </div>
        <g:if test="${booking && (!booking.isActivity() || !(booking.group?.bookings?.size() > 1))}">
            <div class="pull-right">
                <button id="moveBtn" onclick="moveBooking.startMove('${booking?.slot?.id}')" class="btn btn-md btn-info" data-dismiss="modal"><g:message code="facilityBooking.facilityBookingForm.message16"/></button>
            </div>
        </g:if>
    </div>
</g:form>

<script type="text/javascript">

    var $comments = $("#comments");
    var $showComment = $("#showComment");
    var $customerSearch;
    var isBooking = ${g.forJavaScript(data: booking ? true : false)};

    $(document).ready(function() {
        var $bookingForm = $('#bookingForm');

        $customerSearch = $("#customerSearch").matchiCustomerSelect({width:'250px', onchange: selectCustomer});

        $bookingForm.preventDoubleSubmission({});
        $('.dropdown-toggle').dropdown();

        var textLimit = ${com.matchi.BookingService.COMMENTS_MAX_INPUT_SIZE};

        function textOnChange() {
            var text = $comments.val();
            $("#inputCounterCurrent").html(text.length);
            $("#inputCounterMax").html(textLimit);
        }

        $comments.on("keyup input", function() {
            var val = $(this).val();
            var showCommentVisible = $showComment.is(":visible");

            if(!val && showCommentVisible) {
                $showComment.parent().slideUp();
                $showComment.attr("checked", false);
            } else if(!showCommentVisible) {
                $showComment.parent().slideDown();
            }

            textOnChange();
        });

        textOnChange();

        <g:if test="${slots.size() > 1}">
        $("#additionalBookings").popover({delay: 200,title: "${message(code: 'default.booking.plural')}", content: "" +
        "<table class=table style='font-size: 12px; color: #2c2c2c;'>" +
        <g:each in="${slots}" var="slot">
        "<tr><td nowrap><g:slotCourtAndTime slot="${slot}"/></td></tr>" +
        </g:each>
        "</table>",
            trigger: "hover",
            placement: "bottom"
        });
        </g:if>

        $bookingForm.validate({
            errorLabelContainer: ".alert-error",
            errorPlacement: function(error, element) { },
            highlight: function (element, errorClass) {
                $("#bookingForm").enableSubmission();
                $(".alert-error").show();
            },
            unhighlight: function (element, errorClass) {
                $(".alert-error").hide();
            },
            messages: {
                customerSearch: {
                    required: "${message(code: 'facilityBooking.facilityBookingForm.message5')}"
                }
            }
        });

        // Keyboard shortcuts for bookingform
        Mousetrap.bindGlobal('n', function(e) {
            $("#newCustomerBooking").trigger("click");
        });
        Mousetrap.bindGlobal('b', function(e) {
            $("#formSubmit").trigger("click");
        });
        Mousetrap.bindGlobal('a', function(e) {
            $("#cancelBtn").trigger("click");
        });
        Mousetrap.bindGlobal('y', function(e) {
            $("#moveBtn").trigger("click");
        });

        $(".add-player-btn").click(function() {
            addPlayerRow();
        });

        $("#rebookSubscription").click(function() {
            window.location.href = "${g.forJavaScript(data: createLink(controller: 'facilityBooking', action: 'rebookSubscriber'))}?" +
                    $(this).closest("form").serialize();
        });

        <g:if test="${booking}">
            <g:each in="${booking?.players}" var="player">
                addPlayerRow("${g.forJavaScript(data: player.customer?.id)}");
            </g:each>
        </g:if>
    });

    function updateType(type, name) {
        var $recurrenceForms = $("#recurrenceForms");
        var $typeSelect = $("#typeSelect");

        $("#type").attr("value", type);
        $typeSelect.html(name);
        $typeSelect.dropdown();

        if (type == "${g.forJavaScript(data: BookingGroupType.NOT_AVAILABLE)}") {
            $("#activateMpcCheck").show();
        } else {
            $("#activateMpcCheck").hide();
        }

        if (type == "${g.forJavaScript(data: BookingGroupType.PRIVATE_LESSON)}") {
            $("#players-wrapper").slideUp();
            if(!$recurrenceForms.is(":visible")) {
                toggleRecurrence();
            }
            $("#recurrence").slideDown();
            $recurrenceForms.attr("locked", "false");
            $("#trainers-wrapper").slideDown();
        } else if(type != "${g.forJavaScript(data: BookingGroupType.DEFAULT)}") {
            if(type == "${g.forJavaScript(data: BookingGroupType.SUBSCRIPTION)}") {
                $("#players-wrapper").slideDown();
                if(!$recurrenceForms.is(":visible")) {
                    toggleRecurrence();
                }
                $("#recurrence").slideDown();
                $recurrenceForms.attr("locked", "true");
            } else {
                $("#players-wrapper").slideUp();
                $("#recurrence").slideDown();
                $recurrenceForms.attr("locked", "false");
            }

            $("#trainers-wrapper").slideUp();
            $('#sendNotificationCheck').hide();
        } else {
            $("#players-wrapper").show();
            $recurrenceForms.attr("locked", "false");
            $("#recurrence").slideUp();
            if($recurrenceForms.is(":visible")) {
                toggleRecurrence();
            }
            $("#trainers-wrapper").slideUp();
        }
    }

    function selectCustomer(customer) {
        if(customer && customer.id != "") {
            $("#booking-customer").slideDown("fast");
            $customerPayment = $('#customerPayment');
            $(".players-list").html("");
            addPlayerRow(customer.id);

            $.ajax({
                url: '<g:createLink controller="facilityBooking" action="bookingPayment" params="[slotId: params.slotId]"/>&customerId='+ customer.id + '&playerCustomerId=' + customer.id,
                method: 'GET',
                success: function(data) {
                    $customerPayment.html(data);
                    $customerPayment.slideDown("fast");
                }
            });
        } else {
            $("#customerId").val("");
            $("#booking-customer").slideUp("fast");
        }
    }

    function addPlayerRow(customerId) {
        var tmpl = $("#player-template").clone();
        tmpl.removeAttr("id");
        $(".players-list").append(tmpl);
        tmpl.find("input[name=playerCustomerId]").matchiCustomerSelect({width:'250px'});
        if (customerId) {
            tmpl.find("input[name=playerCustomerId]").select2("val", customerId);
        } else if (customerId == "") {
            tmpl.find("input[name=unknownPlayer]").click();
            tmpl.find("input[name=playerCustomerId]").select2("disable", $(this).is(":checked"));
        }
        tmpl.find("input[name=playerCustomerId]").on("change", changePlayers);
        tmpl.find("input[name=unknownPlayer]").click(function() {
            tmpl.find("input[name=playerCustomerId]").select2("enable", !$(this).is(":checked"));
            changePlayers();
        });
        tmpl.show();
    }

    function changePlayers() {
        <g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name()}">
        var customerId = $("#customerId").val();

        if (customerId) {
            var playerParams = $("input[name=playerCustomerId]").filter(function() {return $(this).val()}).serialize();
            var unknownPlayerParams = $(".players-list").find("input[name=unknownPlayer]:checked").serialize();
            var facilityBookingActionUrl = isBooking ?
                "${g.forJavaScript(data: createLink(controller: 'facilityBooking', action: 'bookingFormInfo', params: [slotId: params.slotId, bookingId: booking?.id]))}" :
                "${g.forJavaScript(data: createLink(controller: 'facilityBooking', action: 'bookingPayment', params: [slotId: params.slotId]))}&customerId=" + customerId + "&" + playerParams + "&" + unknownPlayerParams;

            $.ajax({
                url: facilityBookingActionUrl,
                method: 'GET',
                success: function(data) {
                    if (isBooking) {
                        $('#bookingFormInfo').html(data);
                        $('#bookingFormInfo').slideDown("fast");
                    } else {
                        $('#customerPayment').html(data);
                        $("#customerPayment").slideDown("fast");
                    }
                }
            });
        }
        </g:ifFacilityPropertyEnabled>
    }

    function submitBookingForm() {
        var $bookingWarning = $("#bookingWarning");
        markAsPaidIfFreeBooking();
        return $("#type").val() == "${g.forJavaScript(data: BookingGroupType.DEFAULT.name())}" && $bookingWarning.val() ?
                confirm($bookingWarning.val()) : true;
    }

    function togglePlayers() {
        var $toggleMarker = $("#players-toggle-marker");
        var $playerForms = $("#players-wrapper-forms");

        if($toggleMarker.hasClass("icon-chevron-right")) {
            $toggleMarker.removeClass("icon-chevron-right");
            $toggleMarker.addClass("icon-chevron-down");

            $playerForms.show();
        } else {
            $toggleMarker.removeClass("icon-chevron-down");
            $toggleMarker.addClass("icon-chevron-right");

            $playerForms.hide();
        }
    }

    function markAsPaidIfFreeBooking() {
        var bookingPrice = $("#bookingPrice").val();
        if (bookingPrice && !parseInt(bookingPrice)) {
            var $paid = $("#paid");
            $paid.attr('checked', true);
            $paid.val(true);
        }
    }
</script>
