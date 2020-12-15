<%@ page import="com.matchi.enums.BookingGroupType; com.matchi.BookingGroup; com.matchi.DateUtil" %>
<%
    def headline = message(code: 'facilityBooking.facilityNewCustomerBookingForm.message5', args: [slots?.size()])
%>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3>${headline}</h3>
</div>
<g:form name="bookingForm" class="no-margin" controller="facilityBooking" action="book">
    <g:hiddenField name="facilityId" value="${facility.id}"/>
    <g:hiddenField name="slotId" value="${slots.collect { it.id }.join(',')}"/>
    <g:hiddenField name="date" value="${date}"/>
    <g:hiddenField name="type" value="${BookingGroupType.DEFAULT}"/>
    <g:hiddenField name="newCustomer" value="${true}"/>
    <g:hiddenField name="userId" value=""/>
    <div class="modal-body">
        <div class="alert alert-error" style="display: none;">
            <a class="close" data-dismiss="alert" href="#">×</a>
        </div>

        <fieldset>
        <div class="control-group" style="padding: 0">
            <h3><g:slotCourtAndTime slot="${slots[0]}"/>
                <g:if test="${slots.size() > 1}">
                    <small> <g:message code="facilityBooking.facilityNewCustomerBookingForm.message6" args="[slots.size()]"/></small>
                </g:if>
            </h3>
        </div>
        <div class="control-group">
            <div class="controls controls-row">
                <div class="btn-group">
                    <a class="btn btn-small" href="#" style="width: 105px"><i class="icon-list icon"></i>&nbsp;<span id="typeSelect"><g:message code="default.booking"/></span></a>
                    <a class="btn btn-small dropdown-toggle" data-toggle="dropdown" href="#"><span class="caret"></span></a>
                    <ul class="dropdown-menu">
                        <g:each in="${bookingTypes}" var="type">
                            <li><a href="javascript:void(0)" onclick="updateType('${type}', '<g:message code="bookingGroup.name.${type}"/>');"> <g:message code="bookingGroup.name.${type}"/></a></li>
                        </g:each>
                    </ul>
                    <g:if test="${params.bookingType}">
                        <script type="text/javascript">
                            $(function() {$("a[data-value=${g.forJavaScript(data: params.bookingType)}]").click();});
                        </script>
                    </g:if>
                </div>
            </div>
        </div>

        <div class="clearfix"></div>
        <div class="well well-small">
            <div class="row">
                <span class="span3">
                    <h6><g:message code="facilityBooking.facilityNewCustomerBookingForm.message2"/></h6>
                </span>
            </div>

            <div class="row">
                <div class="span2">
                    <div class="control-group">
                        <div class="controls">
                            <g:textField id="email" name="email" class="email span2"
                                    tabindex="1" placeholder="${message(code: 'customer.email.label')}"/>
                        </div>
                    </div>
                </div>
                <div class="span2">
                    <div class="control-group">
                        <div class="controls">
                            <g:textField id="telephone" name="telephone" class="span2"
                                    tabindex="2" placeholder="${message(code: 'customer.telephone.label')}"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="span2">
                    <div class="control-group">
                        <div class="controls">
                            <g:textField id="firstname" class="required span2" name="firstname"
                                    tabindex="3" placeholder="${message(code: 'customer.firstname.label')}*"/>
                        </div>
                    </div>
                </div>
                <div class="span2">
                    <div class="control-group">
                        <div class="controls">
                            <g:textField id="lastname" class="required span2" name="lastname"
                                    tabindex="4" placeholder="${message(code: 'customer.lastname.label')}*"/>
                        </div>
                    </div>
                </div>
            </div>

            <g:if test="${memberTypes}">
                <div class="control-group controls-row">
                    <label class="checkbox no-padding" for="newMember">
                        <label class="checkbox inline">
                            <g:checkBox id="newMember" name="newMember" tabindex="5"/>
                            <g:message code="facilityCustomer.create.message44"/>
                        </label>
                    </label>
                </div>

                <div id="membershipWrapper" style="display: none;">
                    <div class="row">
                        <div class="span2">
                            <div class="control-group">
                                <div class="controls">
                                    <select id="selectMemberType" name="memberType" class="span2">
                                        <option value=""><g:message code="membershipType.multiselect.noneSelectedText"/></option>
                                        <g:each in="${memberTypes}">
                                            <option value="${it.id}" data-start-date="${it.startDate}" data-end-date="${it.endDate}"
                                                    data-grace-period-end-date="${it.gracePeriodEndDate}">${it.name}</option>
                                        </g:each>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="span1">
                            <div class="control-group">
                                <div class="controls">
                                    <g:textField name="startDate" readonly="true" class="span1"
                                            placeholder="${message(code: 'membership.startDate.label.long')}"/>
                                </div>
                            </div>
                        </div>
                        <div class="span1 left-padding15">
                            <div class="control-group">
                                <div class="controls">
                                    <g:textField name="endDate" readonly="true" class="span1"
                                            placeholder="${message(code: 'membership.endDate.label.long')}"/>
                                </div>
                            </div>
                        </div>
                        <div class="span2 left-padding15">
                            <div class="control-group">
                                <div class="controls">
                                    <g:textField name="gracePeriodEndDate" readonly="true" class="span1"
                                            placeholder="${message(code: 'membership.gracePeriodEndDate.label.long')}"/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="span4">
                            <div class="control-group">
                                <div class="controls">
                                    <g:render template="/templates/customer/membershipStartingGracePeriodDays"/>

                                    <label class="checkbox" for="membershipPaid">
                                        <input type="checkbox" id="membershipPaid" name="membershipPaid"
                                                value="true" class="membership-paid-checkbox"/>
                                        <g:message code="membershipCommand.paid.label"/>
                                    </label>

                                    <label class="checkbox" for="membershipCancel">
                                        <input type="checkbox" id="membershipCancel" name="membershipCancel" value="true"/>
                                        <g:message code="membershipCommand.cancel.label"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>
        </div>

        <div id="recurrence" style="display: none">
            <g:bookingFormRecurrence slot="${slots[0]}"/>
        </div>


        <div class="control-group">
            <g:textArea class="span6" rows="5" cols="50" name="comments" id="comments" value="${booking?.comments}" tabindex="6" placeholder="${message(code: 'facilityBooking.facilityNewCustomerBookingForm.message13')}" maxlength="${com.matchi.BookingService.COMMENTS_MAX_INPUT_SIZE}"/>
            <div id="inputCounter">
                <label><span id="inputCounterCurrent"></span> / <span id="inputCounterMax"></span></label>
            </div>
            <label class="checkbox" style="display:none;}">
                <g:checkBox name="showComment"/><g:message code="facilityBooking.facilityNewCustomerBookingForm.message14"/>
            </label>
        </div>
        <g:if test="${!facility.hasApplicationCashRegister()}">
            <div class="control-group">
                <label class="checkbox"  for="paid">
                    <g:checkBox id="paid" name="paid" value="${booking?.paid}" tabindex="8"/> <g:message code="facilityBooking.facilityNewCustomerBookingForm.message15"/>
                </label>
            </div>
        </g:if>
        <label class="checkbox" for="sendNotification">
            <g:checkBox id="sendNotification" name="sendNotification" tabindex="7" value="${true}"/><g:message code="facilityBooking.facilityNewCustomerBookingForm.message16"/>
            <g:inputHelp title="${message(code: 'facilityBooking.facilityNewCustomerBookingForm.message17')}"/>
        </label>
        </fieldset>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton id="formSubmit" name="formSubmit" value="${message(code: "button.addbook.label")}" class="btn btn-md btn-success" tabindex="7"/>

            <g:submitToRemote id="recurrenceSubmit" class="btn btn-md btn-success hidden"
                              controller="facilityBooking" action="confirmRecurrence" update="bookingModal" value="${message(code: "button.addconfirm.label")}"/>

            <g:remoteLink class="new btn btn-md btn-danger" controller="facilityBooking" action="bookingForm" update="bookingModal"
                  params="['date': date, 'slotId': slots.collect { it.id }.join(',') ]" tabindex="7">
                <g:message code="button.back.label"/>
            </g:remoteLink>

        </div>
    </div>
</g:form>
<script type="text/javascript">
    var $comments = $("#comments");
    var $showComment = $("#showComment");

    $(document).ready(function() {
        var $userSearch = $("#email");

        $userSearch.autocomplete({
            source: function( request, response ) {
                $.ajax({
                    url: "<g:createLink controller="autoCompleteSupport" action="userOnEmail"/>",
                    dataType: "json",
                    data: {
                        featureClass: "P",
                        style: "full",
                        maxRows: 12,
                        query: request.term
                    },
                    success: function( data ) {
                        response( $.map( data, function( item ) {
                            return {
                                value: item.email,
                                label: item.number,
                                id: item.id,
                                fullname: item.fullname,
                                firstname: item.firstname,
                                lastname: item.lastname
                            }
                        }));
                    }
                });
            },
            minLength: 1,
            select: function( event, ui ) {
                selectUser(ui.item)
            },
            open: function() {
            },
            close: function() {
            }

        }).data("ui-autocomplete")._renderItem = function(ul, item) {
            return $("<li></li>").data("ui-autocomplete-item", item).append(hintUser(item)).appendTo(ul)};

        $('#bookingForm').preventDoubleSubmission({});
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

        $("[rel='tooltip']").tooltip();

        <g:if test="${slots.size() > 1}">
        $("#additionalBookings").popover({title: "${message(code: 'default.booking.plural')}", content: "" +
                "<table class=table style='font-size: 12px; color: #2c2c2c;'>" +
                <g:each in="${slots}" var="slot">
                "<tr><td><g:slotCourtAndTime slot="${slot}"/></td></tr>" +
                </g:each>
                "</table>",
            trigger: "hover"
        })
        </g:if>

        $("#email").focus();

        $("#newMember").on("change", function() {
            $("#membershipWrapper").toggle();
        });

        $("#bookingForm").validate({
            errorLabelContainer: ".alert-error",
            errorPlacement: function(error, element) {},
            highlight: function (element, errorClass) {
                $("#bookingForm").enableSubmission();
                $(element).addClass("invalid-input");
            },
            unhighlight: function (element, errorClass) {
                $(element).removeClass("invalid-input");
            },
            messages: {
                firstname: "${message(code: 'facilityBooking.facilityNewCustomerBookingForm.message19')}",
                lastname: "${message(code: 'facilityBooking.facilityNewCustomerBookingForm.message20')}",
                email: {
                    required: "${message(code: 'facilityBooking.facilityNewCustomerBookingForm.message21')}",
                    email: "${message(code: 'facilityBooking.facilityNewCustomerBookingForm.message22')}"
                }
           }
        });

        $("#startDate,#endDate,#gracePeriodEndDate").datepicker({
            autoSize: true,
            dateFormat: 'yy-mm-dd'
        });
        $("#endDate,#gracePeriodEndDate").datepicker("option", "minDate", new Date());

        $("#endDate").on("change", function() {
            $("#gracePeriodEndDate").datepicker("option", "minDate", new Date($(this).val()));
        });
        $("#startDate").on("change", function() {
            var date = new Date($(this).val());
            var today = new Date();
            $("#endDate").datepicker("option", "minDate",
                    date.getTime() < today.getTime() ? today : date).trigger("change");
        }).trigger("change");

        $("#selectMemberType").on("change", function() {
            if ($(this).val()) {
                var option = $(this).find("option:selected");
                $("#gracePeriodEndDate").val(option.attr("data-grace-period-end-date"));
                $("#endDate").val(option.attr("data-end-date"));
                $("#startDate").val(option.attr("data-start-date")).trigger("change");
            }
        });
    });

    function updateType(type, name) {
        $("#type").attr("value", type);
        $("#typeSelect").html(name);
        $('#typeSelect').dropdown();

        if(type != "${g.forJavaScript(data: BookingGroupType.DEFAULT)}") {
            if(type == "${g.forJavaScript(data: BookingGroupType.SUBSCRIPTION)}") {
                if(!$("#recurrenceForms").is(":visible")) {
                    toggleRecurrence();
                }
                $("#recurrence").slideDown();
                $("#recurrenceForms").attr("locked", "true");
            } else {
                $("#recurrence").slideDown();
                $("#recurrenceForms").attr("locked", "false");
            }
        } else {
            $("#recurrenceForms").attr("locked", "false");
            $("#recurrence").slideUp();
            if($("#recurrenceForms").is(":visible")) {
                toggleRecurrence();
            }
        }
    }

    function hintUser(item) {
        var html = "<a>";
        html += "<strong>" + item.value + "</strong>";
        html += "<br><span style='font-size:100%'>";
        html += item.fullname + "</span>";
        html += "</a>";

        return html;
    }

    function selectUser(user) {
        if(user && user.id != "") {
            $("#email").val(user.value);
            $("#firstname").val(user.firstname);
            $("#lastname").val(user.lastname);
            $("#telephone").val(user.phone);
            $("#userId").val(user.id);
        } else {
            $("#customerId").val("");
            $("#booking-customer").slideUp("fast");
        }
    }
</script>