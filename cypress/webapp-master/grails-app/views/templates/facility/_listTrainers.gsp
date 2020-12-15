<%@ page import="org.joda.time.DateTime" %>
<div class="row">
    <div class="col-sm-6">
        <ul class="nav nav-pills nav-stacked list-coupons" id="trainerTab">
            <g:each in="${trainers}" var="trainer">

                <li>
                    <a data-toggle="tab" href="#trainer_${trainer.id}" class="padding10">
                        <div class="media">

                            <span class="pull-left icon-lg">
                                <div class="avatar-circle-sm">
                                    <img class="img-responsive" src="${trainer?.profileImage?.getAbsoluteFileURL() ?: resource(dir: 'images', file: 'avatar_default.png')}"/>
                                </div>
                            </span>

                            <div class="media-body">
                                <div class="top-margin5">
                                    <h5 class="block weight400">
                                        ${trainer}
                                    </h5>
                                </div>

                                <div class="text-muted text-xs right-margin5">
                                    <span class="badge badge-default">
                                        ${g.message(code:'sport.name.'+trainer?.sport?.id)}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </a>
                </li>
            </g:each>
        </ul>
    </div>

    <div class="col-sm-6 horizontal-padding30 bottom-padding20">
        <div class="tab-content">
            <g:each in="${trainers}" var="trainer">
                <div class="tab-pane fade in active" id="trainer_${trainer.id}">
                    <h4 class="top-margin20 weight400"><g:message code="templates.facility.listTrainers.message1"/> ${trainer.firstName}</h4>
                    <div class="text-sm">
                        <p>
                            ${g.toRichHTML(text: trainer.description)}
                        </p>
                        <g:if test="${trainer.email || trainer.phone}">
                            <h5><g:message code="default.contact.label"/> ${trainer.firstName}</h5>
                            <hr class="vertical-margin5"/>
                        </g:if>
                        <ul class="list-inline">
                            <g:if test="${trainer.email}">
                                <li>
                                    <i class="fas fa-envelope text-muted"></i> <a href="mailto:${trainer.email}">${trainer.email}</a>
                                </li>
                            </g:if>
                            <g:if test="${trainer.phone}">
                                <li>
                                    <i class="fas fa-phone text-muted"></i> <a href="tel:${trainer.phone}">${trainer.phone}</a>
                                </li>
                            </g:if>
                        </ul>
                    </div>
                    <g:if test="${trainer.hasAvailability() && trainer.availabilities?.any()}">
                        <div class="text-sm">
                            <h5 class="top-margin20 weight400"><g:message code="facility.show.trainer.booking.label"/></h5>
                            <hr class="vertical-margin5"/>

                            <g:each in="${1..7}" var="day" status="i">
                                <%
                                    def av = trainer?.availabilities?.findAll { it.weekday == day }
                                %>
                                <g:each in="${av}" var="a">
                                    <ul class="list-inline">
                                        <li class="col-sm-4"><strong><g:message code="time.weekDay.plural.${a.weekday}"/></strong></li>
                                        <li>${a.begin.toString("HH:mm")} - ${a.end.toString("HH:mm")}</li>
                                    </ul>
                                </g:each>
                            </g:each>

                        </div>
                    </g:if>
                </div>
            </g:each>
        </div>
    </div>
</div>

<r:script>
    $(document).ready(function () {
        $('[rel=tooltip]').tooltip();

        $('#trainerTab a:first').tab('show');
    });
</r:script>
