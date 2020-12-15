<div class="control-group">
    <g:hiddenField name="addMemberId" value="${customer.getMembership()?.id}"/>
    <div class="controls well no-bottom-margin" style="padding: 15px">
        <div class="row">
            <span class="span1 inline" style="width: 45px">
                <g:fileArchiveUserImage size="small" id="${customer?.user?.id}" />
            </span>
            <span class="span5" style="width: 430px"><b>${customer.fullName()}</b> ( ${customer.number} )
                <g:memberBadge customer="${customer}"/>
                <br>
                <span id="show-email">${customer.email}</span>
                <g:if test="${customer.telephone}">
                    <span id="show-phone"> / ${customer.telephone}</span>
                </g:if>
            </span>
        </div>
        <div class="clearfix"></div>
    </div>
</div>