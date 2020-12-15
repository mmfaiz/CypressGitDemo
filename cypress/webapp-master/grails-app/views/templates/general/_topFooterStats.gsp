<r:require modules="animateNumber"/>
<!-- FEATURETTES -->
<div class="featurette content-section row">
    <g:each in="${stats}" var="stat" status="i">
        <div class="col-md-4 col-sm-6 col-xs-12">
            <div class="featurette-item text-center">
                <!-- <div class="icon fa fa-${stat.icon}"></div> -->
                <div class="info">
                    <h4 class="number no-bottom-margin" id="stat_${i}">0</h4>
                    <span class="title">${stat.text}</span>
                </div>
            </div>
        </div>
    </g:each>
</div>
<!-- FEATURETTES END -->

<r:script>
$(function() {
    <g:each in="${stats}" var="stat" status="i">
        $('#stat_' + '${g.forJavaScript(data: i)}').animateNumber({
            number: '${g.forJavaScript(data: stat.number)}',
            easing: 'easeInQuad',
        }, 2000);
    </g:each>
});
</r:script>
