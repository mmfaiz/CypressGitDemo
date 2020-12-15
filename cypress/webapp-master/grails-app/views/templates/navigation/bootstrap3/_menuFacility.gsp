<%@ page import="com.matchi.FacilityProperty;com.matchi.FacilityUserRole" %>
<g:set var="activePage" value="${params.controller}-${params.action}"/>
<nav class="navbar navbar-default navbar-admin navbar-fixed-top" role="navigation">
    <div class="container">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#navbar-collapse">
                <span class="sr-only"><g:message code="default.navigation.toggle"/></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <g:link controller="${defaultFacilityController()}" class="navbar-brand navbar-brand-facility">
                ${facilityName}
            </g:link>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="navbar-collapse">
            <g:ifFacilityAccessGranted accessRights="${[FacilityUserRole.AccessRight.CUSTOMER, FacilityUserRole.AccessRight.TRAINING_PLANNER]}">
                <ul class="nav navbar-nav navbar-left">
                    <li class="dropdown no-border">
                        <a id="cSearch" class="dropdown-toggle" href="#" data-toggle="dropdown">
                            <i class="fas fa-search fa-white"></i><strong class="caret"></strong>
                        </a>
                        <div class="dropdown-menu" style="padding: 15px;">
                            <g:form class="navbar-search pull-left" controller="facilityCustomer" action="index">
                                <g:textField id="general-search-input" class="search-query" name="q" value="" placeholder="${message(code: 'customer.search.placeholder')}"/>
                            </g:form>
                        </div>
                    </li>
                </ul>
            </g:ifFacilityAccessGranted>
            <ul class="nav navbar-nav navbar-right">
                <g:ifFacilityAccessGranted accessRights="${FacilityUserRole.AccessRight.SCHEDULE}">
                    <li class="${activePage.contains('facilityBooking') ? "active":""}">
                        <g:link controller="facilityBooking">
                            <g:message code="templates.navigation.menuFacility.message16"/>
                        </g:link>
                    </li>
                </g:ifFacilityAccessGranted>
                <g:ifFacilityAccessGranted accessRights="${[FacilityUserRole.AccessRight.CUSTOMER, FacilityUserRole.AccessRight.TRAINING_PLANNER]}">
                    <li class="${activePage.contains('facilityCustomer') ? "active":""}">
                        <g:link controller="facilityCustomer">
                            <g:message code="customer.label.plural"/>
                        </g:link>
                    </li>
                </g:ifFacilityAccessGranted>
                <g:ifFacilityAccessGranted accessRights="${FacilityUserRole.AccessRight.TRAINING_PLANNER}">
                    <g:if test="${FacilityProperty.findByFacilityAndKey(activeFacility, FacilityProperty.FacilityPropertyKey.FEATURE_TRAINERS.name())?.value ||
                            FacilityProperty.findByFacilityAndKey(activeFacility, FacilityProperty.FacilityPropertyKey.FEATURE_TRAINING_PLANNER.name())?.value}">
                        <li class="dropdown ${activePage.contains('facilityForm') ||
                                activePage.contains('trainer') || activePage.contains('facilityCourse') ? "active":"" ||
                                activePage.contains('facilityCourseParticipant') ? "active":""}">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                <g:message code="templates.navigation.menuFacility.message18"/>
                                <b class="caret"></b>
                            </a>
                            <ul class="dropdown-menu">
                                <g:if test="${FacilityProperty.findByFacilityAndKey(activeFacility, FacilityProperty.FacilityPropertyKey.FEATURE_TRAINING_PLANNER.name())?.value}">
                                    <li><g:link controller="facilityCourse" action="settings"><g:message code="course.settings.label"/></g:link></li>
                                    <li><g:link controller="facilityCourse" action="index"><g:message code="course.label.plural"/></g:link></li>
                                    <li><g:link controller="facilityCourseSubmission" action="index"><g:message code="courseSubmission.label.plural"/></g:link></li>
                                    <li><g:link controller="facilityCourseParticipant" action="index"><g:message code="courseParticipant.label.plural"/></g:link></li>
                                    <li><g:link controller="facilityCourse" action="planning"><g:message code="course.planning.label"/></g:link></li>
                                </g:if>
                                <g:if test="${FacilityProperty.findByFacilityAndKey(activeFacility, FacilityProperty.FacilityPropertyKey.FEATURE_TRAINERS.name())?.value}">
                                    <li><g:link controller="trainer" action="index"><g:message code="trainer.label"/></g:link></li>
                                </g:if>
                            </ul>
                        </li>
                    </g:if>
                </g:ifFacilityAccessGranted>
                <g:ifFacilityAccessGranted accessRights="${[FacilityUserRole.AccessRight.INVOICE, FacilityUserRole.AccessRight.SCHEDULE]}">
                    <li class="dropdown ${activePage.contains('facilityBooking') ||
                            activePage.contains('facilityActivity') ||
                            activePage.contains('facilityAdministration') ||
                            activePage.contains('facilityNotification') ||
                            activePage.contains('facilitySubscription') ||
                            activePage.contains('facilityCourt') ||
                            activePage.contains('facilityCoupon') ||
                            activePage.contains('facilityPriceList') ||
                            activePage.contains('facilityGroup') ||
                            activePage.contains('facilityInvoice') ||
                            activePage.contains('facilityAccessCode') ||
                            activePage.contains('facilityInvoiceRow') ||
                            activePage.contains('facilityCustomerCategorie') ||
                            activePage.contains('facilityMembershipType') ||
                            activePage.contains('facilitySeason') ||
                            activePage.contains('facilityAction') ||
                            activePage.contains('facilityUser') ||
                            activePage.contains('facilityTransaktions') ||
                            activePage.contains('facilityEventActivity') ||
                            activePage.contains('organization') ? "active":""}">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <g:message code="adminHome.index.title"/>
                            <b class="caret"></b>
                        </a>
                        <ul class="dropdown-menu">
                            <g:if test="${!activeFacility?.isMasterFacility()}">
                                <g:ifFacilityAccessGranted accessRights="${FacilityUserRole.AccessRight.SCHEDULE}">
                                    <li><g:link controller="facilityActivity"><g:message code="default.activity.plural"/></g:link></li>
                                </g:ifFacilityAccessGranted>
                            </g:if>
                            <g:ifFacilityFullRightsGranted>
                                <g:if test="${!activeFacility?.isMasterFacility()}">
                                    <li><g:link controller="facilitySubscription"><g:message code="subscription.label"/></g:link></li>
                                </g:if>
                                <li><g:link controller="facilityAdministration"><g:message code="facility.label2"/></g:link></li>
                                <g:if test="${!activeFacility?.isMasterFacility()}">
                                    <g:if test="${FacilityProperty.findByFacilityAndKey(activeFacility, FacilityProperty.FacilityPropertyKey.FEATURE_FACILITY_DYNAMIC_FORMS.name())?.value}">
                                        <li><g:link controller="facilityEventActivity"><g:message code="eventActivity.label.plural"/></g:link></li>
                                    </g:if>
                                </g:if>
                                <li><g:link controller="facilityUser"><g:message code="user.facility.label"/></g:link></li>
                                <g:if test="${!activeFacility?.isMasterFacility()}">
                                    <li class="dropdown-submenu">
                                        <a tabindex="-1" href="#"><g:message code="court.management.label"/></a>
                                        <ul class="dropdown-menu">
                                            <li><g:link controller="facilityCourts"><g:message code="court.label.plural"/></g:link></li>
                                            <li><g:link controller="facilityCourtsGroups"><g:message code="courtGroup.label.plural"/></g:link></li>
                                        </ul>
                                    </li>
                                </g:if>
                                <li class="dropdown-submenu">
                                    <a tabindex="-1" href="#"><g:message code="offers.label"/></a>
                                    <ul class="dropdown-menu">
                                        <li><g:link mapping="Coupon" action="index"><g:message code="offers.coupon.label"/></g:link></li>
                                        <g:if test="${FacilityProperty.findByFacilityAndKey(activeFacility, FacilityProperty.FacilityPropertyKey.FEATURE_GIFT_CARDS.name())?.value}">
                                            <li><g:link mapping="GiftCard" action="index"><g:message
                                                    code="offers.giftCard.label"/></g:link></li>
                                        </g:if>
                                        <g:if test="${FacilityProperty.findByFacilityAndKey(activeFacility, FacilityProperty.FacilityPropertyKey.FEATURE_PROMO_CODES.name())?.value}">
                                            <li><a href="javascript:void(0)"><g:message code="offers.promo.label"/></a></li>
                                        </g:if>
                                    </ul>

                                </li>
                            </g:ifFacilityFullRightsGranted>
                            <g:if test="${!activeFacility?.isMasterFacility()}">
                                <g:ifFacilityAccessGranted accessRights="${FacilityUserRole.AccessRight.INVOICE}">
                                    <g:if test="${activeFacility.hasApplicationInvoice()}">
                                        <li><g:link action="index" controller="facilityInvoice"><g:message code="facility.invoicing.label"/></g:link></li>
                                    </g:if>
                                </g:ifFacilityAccessGranted>
                            </g:if>
                            <g:ifFacilityFullRightsGranted>
                                <g:if test="${!activeFacility?.isMasterFacility()}">
                                    <li><g:link controller="facilityGroup"><g:message code="group.label.plural"/></g:link></li>
                                    <g:if test="${FacilityProperty.findByFacilityAndKey(activeFacility,
                                            FacilityProperty.FacilityPropertyKey.FEATURE_REQUIREMENT_PROFILES.name())?.value}">
                                        <li><g:link controller="facilityRequirements"><g:message code="requirementProfile.label.plural"/></g:link></li>
                                    </g:if>
                                    <li><g:link controller="facilityNotification"><g:message code="facilityNotification.label.plural"/></g:link></li>
                                </g:if>
                                <li><g:link controller="facilityMembershipType"><g:message code="membershipType.label.plural"/></g:link></li>
                                <g:if test="${FacilityProperty.findByFacilityAndKey(activeFacility,
                                        FacilityProperty.FacilityPropertyKey.FEATURE_ORGANIZATIONS.name())?.value}">
                                    <li><g:link controller="organization"><g:message code="organization.label.plural"/></g:link></li>
                                </g:if>
                                <g:if test="${!activeFacility?.isMasterFacility()}">
                                    <li><g:link controller="facilityPriceList" action="index"><g:message code="priceList.label.plural"/></g:link></li>
                                </g:if>
                                <li><g:link controller="facilityCustomerCategory" action="index"><g:message code="priceListCustomerCategory.label.plural"/></g:link></li>
                                <g:if test="${!activeFacility?.isMasterFacility()}">
                                    <li><g:link controller="facilitySeason"><g:message code="season.label.plural"/></g:link></li>
                                </g:if>
                            </g:ifFacilityFullRightsGranted>
                        </ul>
                    </li>
                </g:ifFacilityAccessGranted>

                <g:if test="${!activeFacility?.isMasterFacility()}">
                    <g:ifFacilityFullRightsGranted>
                        <li><g:link controller="facilityStatistic"><g:message code="templates.navigation.menuFacility.message12"/></g:link></li>
                    </g:ifFacilityFullRightsGranted>
                </g:if>
                <li class="dropdown">
                    <g:facilityMenuButton />
                </li>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div>
</nav>
<r:script>
    var $cSearch = $('#general-search-input');

    $(document).ready(function() {

        if($cSearch.length > 0) {
            $cSearch.autocomplete({
                source: function( request, response ) {
                    $.ajax({
                        url: "<g:createLink controller="autoCompleteSupport" action="customers"/>",
                        dataType: "json",
                        data: {
                            featureClass: "P",
                            style: "full",
                            maxRows: 12,
                            query: request.term
                        },
                        success: function( data ) {
                            var regex = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
                            var sorted = $.grep(data, function(value) {
                                return value.fullname && regex.test(value.fullname);
                            });
                            sorted = sorted.concat($.grep(data, function(value) {
                                return value.email && regex.test(value.email) && $.inArray(value, sorted) == -1;
                            }));
                            sorted = sorted.concat($.grep(data, function(value) {
                                return value.number && regex.test(value.number) && $.inArray(value, sorted) == -1;
                            }));
                            sorted = sorted.concat($.grep(data, function(value) {
                                return value.telephone && regex.test(value.telephone) && $.inArray(value, sorted) == -1;
                            }));
                            sorted = sorted.concat($.grep(data, function(value) {
                                return $.inArray(value, sorted) == -1;
                            }));

                            response( $.map( sorted, function( item ) {
                                return {
                                    value: item.fullname,
                                    label: item.number,
                                    id: item.id,
                                    email: item.email
                                }
                            }));
                        }
                    });
                },
                minLength: 1,
                select: function( event, ui ) {
                    customerSelected(ui.item)
                },
                open: function() {
                },
                close: function() {
                }

            }).data("ui-autocomplete")._renderItem = function(ul, item) {
                return $("<li></li>").data("ui-autocomplete-item", item).append(hintHTML(item)).appendTo(ul)};

            $("#custumer-search-input").focus();
            $cSearch.on('blur', function(){
                $(this).val('');
            });
        }

        Mousetrap.bindGlobal(['ctrl+k','command+k', 'ctrl+shift+k', 'command+shift+k'], function() {
            $("#cSearch").click();
            $cSearch.focus();
        });
        Mousetrap.bindGlobal('esc', function() {
            if (!$("#general-search-input").is(":hidden")) {
                $("#cSearch").click();
            }
        });
    });

    function hintHTML(item) {
        var html = "<a>";
html += "<strong>" + item.label + " - " + item.value + "</strong>";
html += "<br><span style='font-size:100%'>";
html += item.email + "</span>";
html += "</a>";

        return html;
    }

    function customerSelected(customer) {
        window.location = "<g:createLink controller="facilityCustomer" action="show"/>?id="+customer.id
    }
</r:script>
