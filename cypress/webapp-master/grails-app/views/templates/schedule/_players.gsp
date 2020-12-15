<g:each in="${players}">
    <div class='no-margin text-left'>
        <span class='fas fa-user'></span>
        ${it.customer ? it.customer.fullName() : message(code: 'player.unknown.label')}
    </div>
</g:each>