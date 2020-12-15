<div class="navbar">
    <div class="navbar-inner">
        <div class="container">
            <g:link class="brand" controller="home" action="index"><r:img uri="/images/logo_clean_negative.png"/></g:link>
            <ul class="nav pull-right">
                <li>
                    <g:link controller="userProfile" action="home">
                        <div class="menu_icons menu_home"></div><g:message code="default.home.label"/>
                    </g:link>
                </li>
                <li>
                    <g:link controller="facilityBooking" action="index">
                        <div class="menu_icons menu_home"></div><g:message code="templates.navigation.myClub"/>
                    </g:link>
                </li>
                <li>
                    <g:link controller="facilities" action="index">
                        <div class="menu_icons menu_globe"></div><g:message code="facility.label.plural"/>
                    </g:link>
                </li>
                <li>
                    <g:link controller="matching" action="index">
                        <div class="menu_icons menu_checkbox"></div><g:message code="templates.navigation.menuUser.message3"/>
                    </g:link>
                </li>
                <li>
                    <g:link controller="userMessage" action="index">
                        <div class="menu_icons menu_checkbox"></div><g:message code="userMessage.label.plural"/>
                        <span id="unread-messages-count" class="badge badge-success"></span>
                        <r:script>
                            function loadUnreadMessagesCount() {
                                $.ajax({
                                    cache: false,
                                    url: "${g.forJavaScript(data: createLink(controller: 'userMessage', action: 'countUnreadMessages'))}",
                                    success: function (data, textStatus, jqXHR) {
                                        $("#unread-messages-count").text(data.count || "");
                                        setTimeout(loadUnreadMessagesCount, 5000);
                                    }
                                });
                            }
                            $(function() {loadUnreadMessagesCount();});
                        </r:script>
                    </g:link>
                </li>
                <g:ifFacilityFullRightsGranted>
                    <li>
                        <g:link controller="facilityBooking">
                            <div class="menu_icons menu_settings"></div><g:message code="templates.navigation.myClub"/>
                        </g:link>
                    </li>
                </g:ifFacilityFullRightsGranted>
                <li class="dropdown">
                    <g:profileMenuButton />
                </li>
            </ul>
        </div>
    </div>
</div>