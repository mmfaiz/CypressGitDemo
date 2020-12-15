<%@ page import="com.matchi.HtmlUtil" defaultCodec="html" %>
<li class="${occasion.id}_occasion panel panel-occasion panel-occasion-${occasion.activity.hintColor} no-bottom-margin">
    <g:set var="startTime" value="${occasion?.startTime?.toDateTime()?.toString("HH:mm")}"/>
    <g:set var="endTime" value="${occasion?.endTime?.toDateTime()?.toString("HH:mm")}"/>
    <div class="panel-heading">
        <ul class="list-table">
            <li class="text">
                <span class="text-sm">
                    ${occasion?.court?.name}
                </span>
            </li>
        </ul>
    </div>
    <g:if test="${occasion?.message}">
        <div class="panel-description-${occasion.activity.hintColor}">
            <small>${raw(HtmlUtil.escapeAmpersands(HtmlUtil.closeInlineElements(occasion.message)))}</small>
        </div>
    </g:if>
    <ol class="${occasion?.id} list-matches horizontal-padding15">
        <g:each in="${occasion?.trainers}"><li class="${it.id}_trainer text-xs"><strong>${it.firstName + " " + it.lastName}</strong></li></g:each>
        <g:each in="${occasion?.participants}" var="participant">
            <li class="${participant?.id} matching-item">
                <div class="media">
                    <div class="media-left right-padding10">
                        <div class="avatar-circle-xxs">
                            <g:fileArchiveUserImage class="${participant?.customer?.user?.id}" size="small"/>
                        </div>
                    </div>
                    <div class="media-body weight400">
                        <a class="text-sm ellipsis pull-left" style="width: 85%" target="_blank">
                            ${participant}
                        </a>
                        <g:if test="${participant.customer.birthyear}">
                            <div class="text-sm text-right pull-right" style="width: 15%">
                                -${participant.customer.birthyear.toString()[-2..-1]}
                            </div>
                        </g:if>
                    </div>
                </div>
            </li>
        </g:each>
    </ol>
</li>
