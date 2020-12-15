<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="com.matchi.FacilityProperty" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>
    <style type="text/css">

    body {
        font-family: "Arial", Arial, sans-serif;
        font-size: 0.9em;
    }

    @page {
        size: 210mm 297mm;
        padding: 0;
        margin: 10mm;

        @bottom-center {
            content: element(footer);
        }
        @top-center {
            content: element(header);

        }

        @bottom-right {
            padding-right:20px;
            /*  content: "Sida " counter(page) " av " counter(pages)  ; */
        }
    }

    .main {
        padding-top: 0;
        margin-top: 0;
        clear:left;
        float: left;
        width: 180mm;
    }

    .footer {
        border-top: thin solid black;
        position: running(footer);
        width: 180mm;
        height: 200mm;
    }

    div.footer {
        font-size: 0.9em;
    }

    table {
        padding: 0;
        margin: 0;
    }
    table tr.trainer td {
        text-transform: uppercase;
    }
    .break {
        page-break-after:always;
    }
    </style>
    <r:layoutResources/>
</head>
<body>
<table width="200mm" cellpadding="0" border="0">
    <tr>
        <div>
            <table border="0" width="100%">
                <thead>
                <tr>
                    <th width="12%"><g:message code="customer.number.label"/></th>
                    <th width="20%"><g:message code="default.name.label"/></th>
                    <th width="5%"><g:message code="default.birthyear.label"/></th>
                    <th width="20%"><g:message code="customer.telephone.label"/></th>
                    <th width="7%"><g:message code="default.day.label"/></th>
                    <th width="8%"><g:message code="default.date.time"/></th>
                    <th width="15%"><g:message code="court.label"/></th>
                </tr>
                </thead>
            </table>
        </div>
    </tr>
    <tr>
        <td>
            <div class="main">
                <table border="0" width="100%">
                    <g:each in="${1..7}" var="day">
                        <% def pageBreak = false %>
                        <g:each in="${0..24}" var="hour">
                            <g:set var="courtOccasions" value="${occasions[hour]?.findAll { it.day() == day }}"/>
                            <g:each in="${courtOccasions}" var="occasion">
                                <g:set var="weekDay" value="${occasion?.date?.getDayOfWeek()}"/>

                                <g:each in="${occasion.trainers}" var="trainer" status="i">
                                    <tr class="trainer">
                                        <td width="15%"><g:message code="trainer.label"/> ${i + 1}</td>
                                        <td width="30%">${trainer}</td>
                                        <td width="5%"></td>
                                        <td width="20%">${trainer.phone}</td>
                                        <td width="7%"></td>
                                        <td width="8%"></td>
                                        <td width="15%"></td>
                                    </tr>
                                </g:each>
                                <g:if test="${occasion.message}">
                                    <tr>
                                        <td colspan="6">
                                            <em>${occasion.message.replaceAll("\\<.*?>","")}</em>
                                        </td>
                                    </tr>
                                </g:if>
                                <g:each in="${occasion.participants}" var="participant">
                                    <tr>
                                        <td>${participant?.customer?.number}</td>
                                        <td>${participant?.customer?.fullName()}</td>
                                        <td><g:if test="${participant?.customer?.birthyear}">-${participant?.customer?.birthyear?.toString()[-2..-1]}</g:if></td>
                                        <td>${participant?.customer?.cellphone ?: participant?.customer?.telephone}</td>
                                        <td><g:message code="time.shortWeekDay.${weekDay}"/></td>
                                        <td>${occasion?.startTime?.toDateTime()?.toString("HH:mm")}</td>
                                        <td>${occasion?.court?.name}</td>
                                    </tr>
                                </g:each>
                                <g:if test="${occasion.trainers || occasion.participants}">
                                    <% pageBreak = true %>
                                    <tr><td colspan="6">&nbsp;</td></tr>
                                </g:if>
                            </g:each>
                        </g:each>
                        <g:if test="${pageBreak}">
                            <div class="break"></div>
                        </g:if>
                    </g:each>
                </table>
            </div>
        </td>
    </tr>
</table>
<div class="footer" style="margin-bottom: 10pt;height:80mm">
    <table width="100%">
        <tr>
            <td width="50%"><g:formatDate date="${new Date()}" format="${message(code: 'date.format.readable.year')}"/></td>
            <td width="50%"></td>
        </tr>
    </table>
</div>
</body>
</html>