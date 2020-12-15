
<r:require modules="bootstrap-select"/>

<select name="aFacility" id="aFacility" data-style="form-control" data-live-search="true" data-size="15" >
    <g:each in="${facilities}">
        <option id="${it.id}" value="${it.id}" ${it.id == currentFacility?.id ? "selected":""} >[${it.id}] ${it.name} (${it.getFortnoxCustomerId()})</option>
    </g:each>
</select>

<script>
    function switchFacility ( id, to ) {
        window.location.href = to+'?aFacility='+id;
    }

    $(document).ready(function() {
      if (!is_touch_enabled()) {
        $("#aFacility").selectpicker({
          virtualScroll: 30,
        })
      }

      $("#aFacility").change(function () {
        switchFacility(this.value, '<g:createLink controller="${targetController ?: 'adminHome'}" action="switchFacility" />');
      });
    });
</script>
