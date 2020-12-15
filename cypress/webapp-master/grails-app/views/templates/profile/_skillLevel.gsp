<%@ page import="com.matchi.sportprofile.SportProfile" %>
<g:if test="${user}">
    <span class="badge badge-black badge-ranking" rel="tooltip" data-original-title="${message(code: 'templates.profile.skillLevel.message1')}">
        ${user.getRoundedAverageSkillLevel()}
    </span>
    <!--<span class="text-info">
        <%
            def r = SportProfile.skillLevelRange.toInt / 2
            def l = user.getRoundedAverageSkillLevel() / 2
        %>
        <g:each in="${1..r}" var="level" status="i">
            <g:if test="${l == ((i+1) - 0.5)}">
                <i class="fa fa-star-half-o"></i>
            </g:if>
            <g:elseif test="${l > i}">
                <i class="fas fa-star"></i>
            </g:elseif>
            <g:else>
                <i class="fa fa-star-o"></i>
            </g:else>
        </g:each>
    </span>-->
</g:if>
