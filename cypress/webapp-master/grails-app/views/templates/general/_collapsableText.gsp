
<g:set var="id" value="${UUID.randomUUID().toString()}" />
<div class="collapsable-text" id="${id}">
${raw(text)}
</div>
<script>
$(document).ready(function() {
  var showMoreButton = $("<span class='show-more-button'>${message([code: "default.readmore.label"])}</span>");
  var collapsable = $('#${id}');
  $(collapsable ).prepend(showMoreButton);
  $(showMoreButton).on("click", function() {
    $(collapsable).toggleClass("open")
  })
});
</script>

