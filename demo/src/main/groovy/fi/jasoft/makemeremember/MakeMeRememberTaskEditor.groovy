package fi.jasoft.makemeremember

import com.vaadin.data.fieldgroup.FieldGroup
import com.vaadin.data.fieldgroup.FieldGroupFieldFactory
import com.vaadin.ui.CheckBox
import com.vaadin.ui.Component
import com.vaadin.ui.DateField
import com.vaadin.ui.FormLayout
import com.vaadin.ui.Notification
import com.vaadin.ui.PopupView
import com.vaadin.ui.TextField
import com.vaadin.ui.Window
import fi.jasoft.makemeremember.model.Task

import java.beans.PropertyChangeListener

/**
 * Editor for editing tasks
 */
class MakeMeRememberTaskEditor extends Window {

    def Task task

    def dateField = {
        def df = new DateField("Task date")
        df.immediate = true
        df.addValueChangeListener({
            // Store field value into bean
            task.date = df.value
        })
        df
    }()

    def messageField = {
        def textField = new TextField("Task message")
        textField.immediate = true
        textField.addValueChangeListener({
            // Store field value into bean
            task.message = textField.value
        })
        textField
    }()

    def doneField = {
        def cb = new CheckBox("Done")
        cb.immediate = true
        cb.addValueChangeListener({
            task.done = cb.value
        })
        cb
    }()

    MakeMeRememberTaskEditor(Task task) {
        this.task = task

        content = new FormLayout(dateField, messageField, doneField)
        closable = true
        resizable = false
        modal = true
        caption = "Edit task"

        //Set initial field values
        dateField.value = task.date
        messageField.value = task.message

        center()
    }
}
