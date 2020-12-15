var Ajax;
if (Ajax && (Ajax != null)) {
	Ajax.Responders.register({
	  onCreate: function() {
        if($('spinner') && Ajax.activeRequestCount>0)
          Effect.Appear('spinner',{duration:0.3,queue:'end'});
	  },
	  onComplete: function() {
        if($('spinner') && Ajax.activeRequestCount==0)
        setTimeout(function() {
            Effect.Fade('spinner',{duration:0.3,queue:'end'})
        }, 2000);
	  }
	});
}

function is_touch_enabled() {
  return ( 'ontouchstart' in window ) ||
    ( navigator.maxTouchPoints > 0 ) ||
    ( navigator.msMaxTouchPoints > 0 );
}