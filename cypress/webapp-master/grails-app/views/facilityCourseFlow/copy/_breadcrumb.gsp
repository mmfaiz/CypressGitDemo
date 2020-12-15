<%@ page import="org.joda.time.LocalTime" %>
<ul class="breadcrumb">
    <li>
        <g:link controller="facilityCourse" action="index">
            <g:message code="course.label.plural"/>
        </g:link>
        <span class="divider">/</span>
    </li>
    <li class="active"><g:message code="facilityCourse.copy.title"/></li>
</ul>

<g:errorMessage bean="${cmd}"/>

<g:render template="/templates/wizard"
        model="[steps: [message(code: 'facilityCourse.copy.step1'),
                message(code: 'facilityCourse.copy.step2')], current: wizardStep]"/>