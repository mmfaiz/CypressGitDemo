<div class="control-group form-group col-sm-12">
  <label class="control-label" for="name">
    <g:message code="organization.name.label"/>
  </label>

  <div class="controls">
    <g:textField name="name" value="${organization.name}" class="form-control" required="required"
                 autofocus="autofocus"/>
  </div>
</div>

<div class="control-group form-group col-sm-12">
  <label class="control-label" for="name">
    <g:message code="organization.number.label"/>
  </label>

  <div class="controls">
    <g:textField name="number" value="${organization.number}" class="form-control"/>
  </div>
</div>

  <div class="control-group form-group col-sm-12">
    <label class="control-label" for="fortnoxCustomerId">
      <g:message code="organization.fortnoxCustomerId.label"/>
    </label>

    <div class="controls">
      <g:textField name="fortnoxCustomerId" value="${organization.fortnoxCustomerId}" class="form-control"/>
    </div>
  </div>

<div class="col-sm-12 no-bottom-margin">
    <label for="fortnox3Token"><g:message code="facility.fortnoxV3.label"/></label>
    <ul class="list-inline">
      <li class="col-sm-4"><input type="text" placeholder="${message(code: 'facility.fortnox3AuthCode.placeholder')}"
                                  id="fortnox3AuthCode" value="${organization.fortnoxAuthCode}"
                                  class="form-control fortnox-value" ${organization.fortnoxAuthCode ? 'disabled = ""' : ''}/></li>
      <li class="col-sm-4"><g:textField name="fortnox3Token" disabled="" value="${organization.fortnoxAccessToken}"
                                        class="form-control fortnox-value"/></li>
      <li class="col-sm-2">
        <a href="javascript:void(0)" id="confirmRenewAccessToken" ${organization.fortnoxAuthCode ? '' : 'style = display:none'}
           class="btn btn-success">
          <g:message code="facility.fortnoxV3.confirmRenewAccessToken"/>
        </a>
        <a href="javascript:void(0)" id="renewAccessToken" ${organization.fortnoxAuthCode ? 'style = display:none' : ''}
           class="btn btn-success">
          <g:message code="facility.fortnoxV3.renewToken"/>
        </a>

        <div class="panel-body renewAccessTokenSpinner" style="display: none; margin-left: 20px;">
          <i class="fas fa-spinner fa-spin"></i>
        </div>
      </li>
      <li class="col-sm-2">
        <g:remoteLink controller="adminFacilityOrganizations" action="testFortnox3Values" params="[id: organization.id]" update="fortnoxV3Result"
                      class="btn btn-success"
                      onLoading="\$('#fortnoxV3Result').html('${message(code: 'default.loader.label')}')">(<g:message
            code="facility.fortnoxV3.testConnection"/> )</g:remoteLink>
      </li>
    </ul>
    <span id="fortnoxV3Result" class="help-block"></span>
</div>

<div class="control-group form-group col-sm-12">
  <label class="control-label" for="name">
    <g:message code="organization.fortnoxCostCenter.label"/>
  </label>

  <div class="controls">
    <g:textField name="fortnoxCostCenter" value="${organization.fortnoxCostCenter}"
        class="form-control" maxlength="6"/>
  </div>
</div>

<r:script>
$(function(){
  $('#renewAccessToken').click(function(){
    $('.renewAccessTokenSpinner').show();
    var authCode = $('#fortnox3AuthCode').val();
    if(authCode === "${g.forJavaScript(data: organization.fortnoxAuthCode)}"){
            alert("${message(code: 'facility.fortnoxV3.specifyOtherAuthCode')}");
            $('.renewAccessTokenSpinner').hide();
            return false;
          }
          $.ajax({
            type:'POST',
            data:{'id': '${g.forJavaScript(data: organization.id)}','authCode': authCode },
            url:'${g.forJavaScript(data: createLink(controller: 'adminFacilityOrganizations', action: 'renewAccessToken'))}',
            success:function(data,textStatus){
              if(data){
                $('#fortnox3Token').val(data);
                if(data == 'ERR') {
                  showRenewTokenPossibility();
                } else {
                  hideRenewTokenPossibility();
                }
              }
              $('.renewAccessTokenSpinner').hide();
            }
          })
        });

        $('#confirmRenewAccessToken').click(function(){
          if(confirm("${g.message(code: 'facility.fortnoxV3.confirmRenewAccessToken.description')}")){
            showRenewTokenPossibility();
          }
        })
    });

    function showRenewTokenPossibility(){
        $('#fortnox3AuthCode').removeAttr('disabled');
        $('#renewAccessToken').show();
        $('#confirmRenewAccessToken').hide();
    }

    function hideRenewTokenPossibility(){
        $('#fortnox3AuthCode').attr('disabled', 'disabled');
        $('#renewAccessToken').hide();
        $('#confirmRenewAccessToken').show();
    }
</r:script>
