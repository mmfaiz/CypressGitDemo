<div class="row">
    <g:each in="${ads}" var="it" status="i">
        <div class="col-xs-3 text-center">
            <div class="partner">
                <g:if test="${it.link}">
                    <a href="${it.link}" target="_blank" class="center-text">
                        <img src="${resource(dir:'images/partners',file: it.img  )}" class="img-responsive"/>
                    </a>
                </g:if>
                <g:else>
                    <img src="${resource(dir:'images/partners',file: it.img  )}"/>
                </g:else>
            </div>
        </div>
    </g:each>
</div>
