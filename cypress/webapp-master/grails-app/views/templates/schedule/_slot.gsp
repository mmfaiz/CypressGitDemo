<td  onclick="showForm('${slot.id}');return false;" slotid="${slot.id}"
     class="slot free ${slot?.restricted ? "restricted":""}" rel="tooltip" data-delay="250"
     style="width: ${width}"
     title="${g.toRichHTML(text: slot.court.name)}<br> ${g.toRichHTML(text: slot.interval.start.toString("HH:mm"))} - ${g.toRichHTML(text: slot.interval.end.toString("HH:mm"))}"></td>