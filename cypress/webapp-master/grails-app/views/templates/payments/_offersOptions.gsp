<g:each in="${customerOfferGroups}">
    <option value="${it.id}">${it.name} (${it.remainingNrOfTickets ? it.remainingNrOfTickets + ' ' + (units ?: message(code: 'unit.st')) : "\u221E"})</option>
</g:each>