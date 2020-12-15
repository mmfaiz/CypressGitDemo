<%@ page import="org.joda.time.LocalDateTime;" %>
<html>
<head>
    <meta name="layout" content="b3main">
    <title><g:message code="userMessage.label.plural"/></title>
    <r:require modules="jquery-slimscroll,matchi-truncate"/>
    <r:script>
        function loadUnreadMessages() {
            $.ajax({
                cache: false,
                url: "${g.forJavaScript(data: createLink(action: 'listUnreadMessages', id: targetUser.id))}",
                success: function (data, textStatus, jqXHR) {
                    for (var i = 0; i < data.length; i++) {
                        $("#message-list").append($("<div>").addClass("media block vertical-padding20")
                            .append($("<div>").addClass("media-left")
                                .append($("<div>").addClass("avatar-circle-sm")
                                    .html('${g.forJavaScript(data: fileArchiveUserImage(id: targetUser.id, class:""))}')
                                )
                            )
                            .append($("<div>").addClass("media-body full-width user-message from")
                                .append($("<div>").addClass("text-xs text-left bottom-margin5")
                                    .append($("<span>").addClass("bold username")
                                        .text("${g.forJavaScript(data: targetUser.fullName())}")
                                        .append($("<span>").addClass("text-muted")
                                            .text(" ${message(code: 'userMessage.conversation.on')} " + data[i].date)
                                        )
                                    )
                                )
                                .append($("<div>").addClass("text-left")
                                    .append($("<span>").addClass("user-msg").text(data[i].message))
                                )
                            )
                        );
                    }
                    setTimeout(loadUnreadMessages, 10000);
                }
            });
        }

        function scrollToBottomOfConversation() {
            var $conversationBox = $('#conversation-box');
            $conversationBox.scrollTop($conversationBox[0].scrollHeight);
        }

        $(function() {
            loadUnreadMessages();

            $("#message-form").submit(function() {
                var btnEl = $(this).find(".btn");
                var msgEl = $(this).find("#message");
                if (msgEl.val()) {
                    btnEl.prop("disabled", true);
                    $.ajax({
                        url: "${g.forJavaScript(data: createLink(action: 'sendMessage'))}",
                        data: $(this).serialize(),
                        success: function(data, textStatus, jqXHR) {
                            $("#message-list").append($("<div>").addClass("media block vertical-padding20")
                                .append($("<div>").addClass("media-body full-width user-message to")
                                    .append($("<div>").addClass("text-xs text-right bottom-margin5")
                                        .append($("<span>").addClass("bold username")
                                            .text("${message(code: 'userMessage.conversation.me')}")
                                            .append($("<span>").addClass("text-muted")
                                                .text(" ${message(code: 'userMessage.conversation.on')} " + data.date)
                                            )
                                        )
                                    )
                                    .append($("<div style='white-space:pre-line;'>").addClass("text-right")
                                        .append($("<span>").addClass("user-msg").text(data.message))
                                    )
                                )
                                .append($("<div>").addClass("media-right")
                                    .append($("<div>").addClass("avatar-circle-sm")
                                        .html('${g.forJavaScript(data: fileArchiveUserImage(id: currentUser.id, class:"avatar-circle-sm"))}')
                                    )
                                )
                            );
                            msgEl.val("");
                        },
                        complete: function() {
                            loadUnreadMessages();
                            btnEl.prop("disabled", false);
                            scrollToBottomOfConversation();
                        }
                    });
                }
                return false;
            });

            $("#message").keydown(function(e) {
                if ((e.keyCode == 10 || e.keyCode == 13) && e.ctrlKey) {
                    $("#message-form").submit();
                }
            });

            $(document).ready(function() {
                var $sidebarInbox    = $('#sidebar-inbox');
                var $conversationBox = $('#conversation-box');

                var sidebarInboxMinHeight    = 300;
                var conversationBoxMinHeight = sidebarInboxMinHeight - 100;

                var windowHeight = $(window).innerHeight();
                var panelsOffset = $sidebarInbox.offset().top;
                var marginBottom = 401;

                var calculatedHeight = windowHeight - panelsOffset - marginBottom;

                var sidebarInboxHeight    = calculatedHeight > sidebarInboxMinHeight    ? calculatedHeight : sidebarInboxMinHeight;
                var conversationBoxHeight = calculatedHeight > conversationBoxMinHeight ? calculatedHeight : conversationBoxMinHeight;

                $sidebarInbox.css('height', sidebarInboxHeight);
                $conversationBox.css('height', conversationBoxHeight);


                scrollToBottomOfConversation();
            });
        });
    </r:script>
</head>

<body>

<section class="vertical-padding40">
    <div class="container">
        <h2 class="h4 page-header no-top-margin text-muted"><i class="fas fa-envelope"></i> <g:message code="userMessage.label.plural"/></h2>

        <div class="row">
            <!-- LEFT -->
            <div class="col-md-4">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4><i class="fas fa-inbox"></i> <g:message code="userMessage.conversation.inbox"/></h4>
                    </div>
                    <div id="sidebar-inbox" class="panel-body no-padding">
                        <ul class="nav nav-tabs nav-stacked inbox-nav">
                            <g:each in="${conversations}" var="msg">
                                <g:set var="user" value="${msg.from == currentUser ? msg.to : msg.from}"/>
                                <li class="${targetUser == user ? 'active' : ' '}">
                                    <g:link action="conversation" id="${user.id}" class="media clearfix no-border no-margin">
                                        <div class="media-left">
                                            <div class="avatar-circle-sm">
                                                <g:fileArchiveUserImage id="${user.id}"/>
                                            </div>
                                        </div>
                                        <div class="media-body full-width">
                                            <span class="pull-right text-muted text-xs vertical-margin5">
                                                <g:humanDateFormat date="${new LocalDateTime(msg.dateCreated)}"/>
                                            </span>
                                            <h5 class="bold vertical-margin5">
                                                <g:if test="${msg.from == currentUser}">
                                                    <g:message code="userMessage.conversation.me"/>,
                                                </g:if>
                                                ${user.firstname}
                                            </h5>
                                            <div class="text-muted user-msg-preview">${msg.message}</div>
                                        </div>
                                    </g:link>
                                </li>
                            </g:each>
                        </ul>
                    </div>
                </div>
            </div>

            <!-- CONVERSATION -->
            <div class="col-md-8">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4><i class="fas fa-comments"></i> <g:message code="userMessage.label.plural"/></h4>
                    </div>
                    <div id="conversation-box" class="panel-body relative">
                        <div id="message-list" class="text">
                            <g:each in="${messages}" var="msg">
                                <div class="media block vertical-padding20">

                                    <!-- YOUR IMAGE -->
                                    <g:if test="${msg.from != currentUser}">
                                        <div class="media-left">
                                            <div class="avatar-circle-sm">
                                                <g:link controller="userProfile" action="index" id="${msg.from.id}">
                                                    <g:fileArchiveUserImage id="${msg.from.id}"/>
                                                </g:link>
                                            </div>
                                        </div>
                                    </g:if>

                                    <div class="media-body full-width user-message ${msg.from == currentUser ? 'to' : 'from'}">
                                        <div class="text-xs bottom-margin5 ${msg.from == currentUser ? 'text-right' : 'text-left'}">
                                            <span class="bold username">
                                                <g:if test="${msg.from == currentUser}">
                                                    <g:message code="userMessage.conversation.me"/>
                                                </g:if>
                                                <g:else>
                                                    <g:link controller="userProfile" action="index" id="${msg.from.id}" class="text-black">
                                                        ${msg.from.fullName()}
                                                    </g:link>
                                                </g:else>
                                            </span>

                                            <span class="text-muted">
                                                <g:message code="userMessage.conversation.on"/>
                                                <g:formatDate date="${msg.dateCreated}" formatName="date.format.timeShort"/>
                                            </span>

                                        </div>
                                        <div class="${msg.from == currentUser ? 'text-right' : 'text-left'}">
                                            <span class="user-msg">${g.toRichHTML(text: msg.message.replaceAll('\r\n', '<br/>'))}</span>
                                        </div>
                                    </div>

                                    <!-- MY IMAGE -->
                                    <g:if test="${msg.from == currentUser}">
                                        <div class="media-right">
                                            <div class="avatar-circle-sm">
                                                <g:fileArchiveUserImage id="${currentUser.id}"/>
                                            </div>
                                        </div>
                                    </g:if>

                                </div>
                            </g:each>
                        </div>
                    </div>
                    <g:canSendDirectMessage to="${targetUser}">
                        <div class="message-field-wrap">
                            <form id="message-form" action="#" class="form top-margin40">
                                <g:hiddenField name="id" value="${targetUser.id}"/>
                                    <g:textArea class="form-control" name="message" rows="5" cols="30" autofocus="autofocus" maxlength="2000"
                                                    placeholder="${message(code: 'userMessage.message.placeholder')}"/>
                                <div class="clearfix vertical-margin5">
                                    <g:submitButton name="sumbit" class="btn btn-success pull-right"
                                                    value="${message(code: 'button.submit.label')}"/>
                                </div>
                            </form>
                        </div>
                    </g:canSendDirectMessage>
                </div>
            </div>
        </div>
    </div>
</section>
<r:script>
    $(function(){
        $('.user-msg-preview').truncateText({ max: 95 });
    });
</r:script>
</body>
</html>
