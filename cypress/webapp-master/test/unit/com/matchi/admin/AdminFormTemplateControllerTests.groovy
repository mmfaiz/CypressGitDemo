package com.matchi.admin

import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormTemplate
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

/**
 * @author Sergei Shushkevich
 */
@TestFor(AdminFormTemplateController)
@Mock([Form, FormTemplate])
class AdminFormTemplateControllerTests {

    void testIndex() {
        def tmpl = new FormTemplate(name: "test", description: "desc").save(failOnError: true)

        def model = controller.index()

        assert 1 == model.templates.size()
        assert tmpl == model.templates[0]
        assert 1 == model.templatesCount
    }

    void testSave() {
        params.name = "test"
        params.description = "desc"

        controller.save()

        assert "/admin/formTemplates/index" == response.redirectedUrl
        assert 1 == FormTemplate.count()
        assert FormTemplate.findByNameAndDescription("test", "desc")
    }

    void testEdit() {
        def tmpl = new FormTemplate(name: "test", description: "desc").save(failOnError: true)

        def model = controller.edit(tmpl.id)

        assert tmpl == model.formTemplate
    }

    void testUpdate() {
        def tmpl = new FormTemplate(name: "test", description: "desc").save(failOnError: true)
        params.name = "new name"

        controller.update(tmpl.id, tmpl.version)

        assert "/admin/formTemplates/index" == response.redirectedUrl
        assert 1 == FormTemplate.count()
        assert FormTemplate.findByNameAndDescription("new name", "desc")
    }

    void testDelete() {
        def tmpl = new FormTemplate(name: "test", description: "desc").save(failOnError: true)

        controller.delete(tmpl.id)

        assert "/admin/formTemplates/index" == response.redirectedUrl
        assert !FormTemplate.count()
    }
}
