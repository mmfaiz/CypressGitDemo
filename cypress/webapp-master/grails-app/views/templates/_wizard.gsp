<div class="wizard">
    <g:each in="${steps}" var="step" status="index">
        <g:set var="selected" value="${index == current}"/>
        <a ${selected ? " class='current'":""}><span class="badge ${selected ? "badge-inverse":""}">${index+1}</span> ${step}</a>
    </g:each>
</div>