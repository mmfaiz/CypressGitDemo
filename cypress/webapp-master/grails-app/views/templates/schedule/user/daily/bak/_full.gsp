<td slotid="${slot.id}"
     class="slot red" rel="tooltip" data-delay="500"
     style="width: ${width}"
     title="Bokad<br>${g.toRichHTML(text: slot.court.name)}<br> ${g.toRichHTML(text: slot.interval.start.toString("HH:mm"))} - ${g.toRichHTML(text: slot.interval.end.toString("HH:mm"))}"></td>