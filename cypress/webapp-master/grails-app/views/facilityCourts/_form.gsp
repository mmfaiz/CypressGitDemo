<%@ page import="com.matchi.Sport; com.matchi.Court; com.matchi.CourtTypeEnum" %>

<div class="control-group">
    <label class="control-label" for="name"><g:message code="court.name.label" default="Namn"/></label>

    <div class="controls">
        <g:textField name="name" value="${courtInstance?.name}" class="span8" required="required"/>
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="description"><g:message code="court.description.label"
                                                              default="Beskrivning"/></label>

    <div class="controls">
        <g:textArea name="description" rows="3" cols="30" value="${courtInstance?.description}"
                    class="span8" maxlength="255"/>
        <div class="checkbox">
            <g:checkBox name="showDescriptionOnline" value="${courtInstance?.showDescriptionOnline}"/>
            <g:message code="court.showDescriptionOnline.label"/>
        </div>

        <div class="checkbox">
            <g:checkBox name="showDescriptionForAdmin" value="${courtInstance?.showDescriptionForAdmin}"/>
            <g:message code="court.showDescriptionForAdmin.label"/>
        </div>
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="indoor"><g:message code="court.indoor.label" default="Inomhus"/></label>

    <div class="controls">
        <g:checkBox name="indoor" value="${courtInstance?.indoor}" class="styled"/>
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="surface"><g:message code="court.surface.label" default="Underlag"/></label>

    <div class="controls">
        <g:select from="${Court.Surface}" optionValue="${{ name -> g.message(code: 'court.surface.' + name) }}"
                  name="surface"
                  value="${courtInstance?.surface}"/>
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="sport"><g:message code="court.sport.label" default="Sport"/></label>

    <div class="controls">
        <g:select from="${Sport.list()}" optionKey="id" optionValue="${{ g.message(code: 'sport.name.' + it.id) }}"
                  name="sport"
                  value="${courtInstance?.sport?.id}"/>
    </div>
</div>
<g:each in="${CourtTypeEnum.findAll() as Collection<CourtTypeEnum>}" var="courtType">
    <div class="control-group court-type-selector"
         data-type="${courtType.name()}"
         data-sports="${courtType.sportsIds.collect { "_" + it + "_" }.join("")}"
         style="display: none;">
        <label class="control-label" for="sport"><g:message code="court.type.${courtType.name()}"/></label>

        <div class="controls">
            <g:hiddenField name="courtTypeAttributeNames" value="${courtType.name()}" isabled="disabled"/>
            <g:select from="${courtType.options}"
                      optionValue="${{ g.message(code: 'court.type.' + courtType.name() + '.' + it) }}"
                      name="courtTypeAttribute"
                      value="${courtInstance.courtTypeAttributes.find { it.courtTypeEnum == courtType }?.value}"
                      disabled="disabled" />
        </div>
    </div>
</g:each>

<r:script>
    $(document).ready(function () {
        $('select[name="sport"').on("change", function () {
            var sportId = $(this).val();
            $(".court-type-selector").hide().find("input, select").prop("disabled", true).end().filter(function () {
                return $(this).data("sports").includes("_" + sportId + "_");
            }).show().find("input, select").prop("disabled", false)
        }).trigger("change");
    });
</r:script>