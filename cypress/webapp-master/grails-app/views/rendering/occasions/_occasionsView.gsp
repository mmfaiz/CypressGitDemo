<%@ page defaultCodec="html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <title></title>
    <style type="text/css">

    body {
        font-family: "Arial", Arial, sans-serif;
        font-size: 0.9em;
    }

    @page {
        size: 297mm 210mm;
        padding: 0;
        margin: 10mm 10mm 7mm 10mm;

        @bottom-center {
            content: element(footer);
        }
        @top-center {
            content: element(header);
        }

        @bottom-right {
            padding-right: 20px;
        }
    }

    .break {
        page-break-after: always;
    }

    a {
        text-decoration: none;
    }

    ul, ol {
        margin-top: 0px;
        margin-bottom: 10px;
    }

    h4 {
        font-size: 1.25em;
    }

    h6 {
        font-size: 1em;
    }

    h4, h6, strong {
        font-family: "Source Sans Pro", "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
        font-weight: 400;
        line-height: 1.1;
        color: inherit;
        margin-top: 10px;
        margin-bottom: 10px;
    }

    input.pick-a-color {
        width: 15px;
        height: 15px;
    }

    .panel {
        margin-bottom: 20px;
        background-color: #ffffff;
        border: 1px solid transparent;
        border-radius: 1px;
        -webkit-box-shadow: 0 1px 1px rgba(0, 0, 0, 0.05);
        box-shadow: 0px 1px 1px rgba(0, 0, 0, 0.05);
    }

    .panel-default {
        border-color: #ebebeb;
    }

    .panel-heading {
        padding: 20px 15px;
        /*border-bottom: 1px solid transparent;*/
        border-top-right-radius: 0px;
        border-top-left-radius: 0px;
    }

    .panel-heading small {
        border-top: 1px solid transparent;
    }

    .panel-title {
        margin-top: 0px;
        margin-bottom: 0px;
        font-size: 16px;
        color: inherit;
    }

    .panel-title > a, .panel-title > small, .panel-title > .small, .panel-title > small > a, .panel-title > .small > a {
        color: inherit;
    }

    .panel-day .panel-day-heading {
        background-color: #414141;
        padding: 10px;
        font-size: 0.9em;
    }

    .panel-day .panel-day-heading .panel-title a {
        color: #ffffff;
    }

    .panel-body {
        padding: 15px;
    }

    .panel-default > .panel-heading {
        color: #333333;
        background-color: #ffffff;
        border-color: #ebebeb;
    }

    .panel-court {
        box-shadow: none;
        padding: 0;
        margin-right: 0px;
        min-height: 218px;
        transition: margin 0.1s ease-in 0s;
    }

    .panel-court .court-heading {
        background-color: #e2e2e2;
        border: none;
        padding: 1px;
        font-size: 0.8em;
    }

    .panel-court .court-heading h4 {
        margin-left: 8px;
    }

    .panel-court .occasion-grid {
        -webkit-flex-flow: row nowrap;
        justify-content: flex-start;
        overflow-x: auto;
        overflow-y: hidden;
        margin: 0;
        padding: 5px;
        column: 4;
        background-color: #f9f9f9;
    }

    .panel-court .occasion-grid .panel-occasion {
        display: inline-block;
        vertical-align: top;
        list-style: none;
        margin: 5px 0 5px 5px;
        width: auto;
        min-width: 200px;
    }

    .panel-court .occasion-grid .panel-occasion .panel-heading {
        padding: 5px;
    }

    .panel-court .occasion-grid .panel-occasion .panel-heading .list-table li {
        padding: 0 5px 0 0;
    }

    .panel-court .occasion-grid .panel-occasion .list-matches {
        min-height: 160px;
    }

    .panel-court .occasion-grid .panel-occasion .list-matches li {
        padding: 4px 5px;
    }

    div[class^="panel-description"] {
        padding: 0 5px;
        border-top: 1px solid #ebebeb;
        color: #ffffff;
    }

    .panel-description-YELLOW {
        background-color: #eaf725;
    }

    .panel-description-ORANGE {
        background-color: #f39c12;
    }

    .panel-description-RED {
        background-color: #ff3333;
    }

    .panel-description-PURPLE {
        background-color: #aa0077;
    }

    .panel-description-BLUE {
        background-color: #19b5fe;
    }

    .panel-description-GREEN {
        background-color: #85b20b;
    }

    .panel-description-BLACK {
        background-color: #000000;
    }

    .panel-description-BROWN {
        background-color: #a52a2a;
    }

    .panel-description-PINK {
        background-color: #ffc0cb;
    }

    .panel-description-GREY {
        background-color: #bfbfbf;
    }

    .panel-occasion-YELLOW {
        border-color: #ecf83e;
    }

    input[value="YELLOW"] {
        background-color: #eaf725;
    }

    .panel-occasion-YELLOW > .panel-heading {
        color: #ffffff;
        background-color: #eaf725;
        border-color: #ecf83e;
    }

    .panel-occasion-YELLOW > .panel-heading a {
        color: #ffffff;
    }

    .panel-occasion-ORANGE {
        border-color: #f4a62a;
    }

    input[value="ORANGE"] {
        background-color: #f39c12;
    }

    .panel-occasion-ORANGE > .panel-heading {
        color: #ffffff;
        background-color: #f39c12;
        border-color: #f4a62a;
    }

    .panel-occasion-ORANGE > .panel-heading a {
        color: #ffffff;
    }

    .panel-occasion-RED {
        border-color: #ff4d4d;
    }

    input[value="RED"] {
        background-color: #ff3333;
    }

    .panel-occasion-RED > .panel-heading {
        color: #ffffff;
        background-color: #ff3333;
        border-color: #ff4d4d;
    }

    .panel-occasion-RED > .panel-heading a {
        color: #ffffff;
    }

    .panel-occasion-PURPLE {
        border-color: #c30089;
    }

    input[value="PURPLE"] {
        background-color: #aa0077;
    }

    .panel-occasion-PURPLE > .panel-heading {
        color: #ffffff;
        background-color: #aa0077;
        border-color: #c30089;
    }

    .panel-occasion-PURPLE > .panel-heading a {
        color: #ffffff;
    }

    .panel-occasion-BLUE {
        border-color: #32bdfe;
    }

    input[value="BLUE"] {
        background-color: #19b5fe;
    }

    .panel-occasion-BLUE > .panel-heading {
        color: #ffffff;
        background-color: #19b5fe;
        border-color: #32bdfe;
    }

    .panel-occasion-BLUE > .panel-heading a {
        color: #ffffff;
    }

    .panel-occasion-GREEN {
        border-color: #97ca0c;
    }

    input[value="GREEN"] {
        background-color: #85b20b;
    }

    .panel-occasion-GREEN > .panel-heading {
        color: #ffffff;
        background-color: #85b20b;
        border-color: #97ca0c;
    }

    .panel-occasion-GREEN > .panel-heading a {
        color: #ffffff;
    }

    .panel-occasion-BLACK {
        border-color: #1e1e1e;
    }

    input[value="BLACK"] {
        background-color: #000000;
    }

    .panel-occasion-BLACK > .panel-heading {
        color: #ffffff;
        background-color: #000000;
        border-color: #1e1e1e;
    }

    .panel-occasion-BLACK > .panel-heading a {
        color: #ffffff;
    }

    .panel-occasion-BROWN {
        border-color: #b92f2f;
    }

    input[value="BROWN"] {
        background-color: #a52a2a;
    }

    .panel-occasion-BROWN > .panel-heading {
        color: #ffffff;
        background-color: #a52a2a;
        border-color: #b92f2f;
    }

    .panel-occasion-BROWN > .panel-heading a {
        color: #ffffff;
    }

    .panel-occasion-PINK {
        border-color: #ffdae0;
    }

    input[value="PINK"] {
        background-color: #ffc0cb;
    }

    .panel-occasion-PINK > .panel-heading {
        color: #ffffff;
        background-color: #ffc0cb;
        border-color: #ffdae0;
    }

    .panel-occasion-PINK > .panel-heading a {
        color: #ffffff;
    }

    .panel-occasion-GREY {
        border-color: #cccccc;
    }

    input[value="GREY"] {
        background-color: #bfbfbf;
    }

    .panel-occasion-GREY > .panel-heading {
        color: #ffffff;
        background-color: #ffc0cb;
        border-color: #cccccc;
    }

    .panel-occasion-GREY > .panel-heading a {
        color: #ffffff;
    }

    .list-courses-div {
        margin-bottom: 15px;
    }

    .list-activity, .list-favorites, .list-matches, .list-participants, .list-courses {
        padding-left: 0px;
        list-style: none;
    }

    .list-activity li, .list-favorites li, .list-matches li, .list-participants li, .list-courses li {
        border-bottom: 1px solid #ebebeb;
        padding: 10px 15px;
    }

    .list-activity li:last-child, .list-favorites li:last-child, .list-matches li:last-child, .list-participants li:last-child, .list-courses li:last-child {
        border-bottom: none;
    }

    .list-table {
        padding-left: 0px;
        list-style: none;
        margin-left: -5px;
        display: table-row;
    }

    .list-table > li {
        display: inline-block;
        padding-left: 5px;
        padding-right: 5px;
    }

    .list-table li {
        display: table-cell;
        vertical-align: middle;
    }

    .media, .media-body {
        overflow: hidden;
        *overflow: visible;
        zoom: 1;
    }

    .media {
        margin-top: 15px;
    }

    .media:first-child {
        margin-top: 0;
    }

    .media > .pull-left {
        margin-right: 10px;
    }

    .media > .pull-right {
        margin-left: 10px;
    }

    .media-left {
        padding-right: 20px;
    }

    .media, .media-body {
        zoom: 1;
        overflow: hidden;
    }

    .media-left, .media-right, .media-body {
        display: table-cell;
        vertical-align: top;
    }

    .media-body a, .media-body div {
        display: table-cell;
    }

    .panel-court .occasion-grid .draggable-item .media .media-body {
        font-size: 0.90em;
        font-weight: normal;
    }

    .ellipsis {
        -ms-text-overflow: ellipsis;
        -o-text-overflow: ellipsis;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .avatar-circle-xxs {
        width: 25px;
        height: 25px;
        -webkit-border-radius: 50%;
        -moz-border-radius: 50%;
        -ms-border-radius: 50%;
        -o-border-radius: 50%;
        border-radius: 50%;
        overflow: hidden;
    }

    .avatar-circle-xxs > img {
        width: 25px;
    }

    .text-right {
        text-align: right;
    }

    .weight400 {
        font-weight: 400 !important;
    }

    .right-padding10 {
        padding-right: 10px !important;
    }

    .no-bottom-margin {
        margin-bottom: 0px !important;
    }

    .no-padding {
        padding: 0 !important;
    }

    .text-sm {
        font-size: 0.9em;
        font-weight: normal;
    }

    .text-xs {
        font-size: 0.75em;
    }

    .footer {
        border-top: thin solid black;
        position: running(footer);
        width: 267mm;
        height: 200mm;
    }

    div.footer {
        font-size: 0.9em;
    }

    </style>
    <r:layoutResources/>
</head>

<body>
<div class="list-courses-div">
    <ul class="list-table">
        <g:each in="${activeCourses}">
            <li class="${it.id} list-group-item no-horizontal-padding">
                <input type="text" value="${it.hintColor}" name="hint-color" class="pick-a-color form-control inline"/>
                <span class="text-sm">${it.name}</span>
            </li>
        </g:each>
    </ul>
</div>
<g:each in="${1..7}" var="day">
    <g:set var="expandedByDefault" value="${occasions.find { it.value.find { o -> o.day() == day } }}"/>
    <g:if test="${expandedByDefault}">
        <div class="panel panel-day">
            <div class="panel-heading panel-day-heading">
                <h6 class="panel-title">
                    <a href="#weekday_${day}">
                        <g:message code="time.weekDay.plural.${day}"/>
                    </a>
                </h6>
            </div>

            <div class="weekday_${day} panel-court">
                <div class="panel-body no-padding">
                    <g:if test="${occasions.size() > 0}">
                        <g:set var="groupIdx" value="${-1}"/>
                        <g:set var="allOccasions" value="${(0..24).collectEntries { [(it): occasions[it].findAll { it.day() == day }] }.findAll { it.value }}"/>
                        <g:each in="${0..24}" var="hour">
                            <g:render template="/templates/printPlanning/occasions"
                                      model="[allOccasions: allOccasions, occasions: occasions[hour]?.findAll { it.day() == day }, hour: hour]"/>
                        </g:each>
                    </g:if>
                </div>
            </div>
        </div>
        <g:if test="${day != 7}">
            <div class="break"></div>
        </g:if>
    </g:if>
</g:each>
<div class="footer" style="margin-bottom: 10pt;height:80mm">
    <table width="100%">
        <tr>
            <td width="50%"><g:formatDate date="${new Date()}"
                                          format="${message(code: 'date.format.readable.year')}"/></td>
            <td width="50%"></td>
        </tr>
    </table>
</div>
</body>
</html>