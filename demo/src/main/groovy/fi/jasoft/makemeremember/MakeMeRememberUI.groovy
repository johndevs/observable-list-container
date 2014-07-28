package fi.jasoft.makemeremember

import com.vaadin.annotations.Theme
import com.vaadin.server.FontAwesome
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.*
import fi.jasoft.ObservableListContainer
import fi.jasoft.makemeremember.model.Task
import fi.jasoft.makemeremember.service.TaskService

@Theme("MakeMeRemember")
class MakeMeRememberUI extends UI{

    /*
    Table for displaying all the tasks
     */
    def Table table = {
        def tbl = new Table()
        tbl.setSizeFull()
        tbl.selectable = true

        // Set tasks as datasource
        def tasks = TaskService.instance.tasks as ObservableList
        tbl.containerDataSource = new ObservableListContainer(tasks)

        // Add a dummy task if no tasks are available
        if(tasks.empty){
            tasks << [message: 'Example task'] as Task
        }

        // Set the visible columns
        tbl.visibleColumns = ['message','date','done']

        tbl
    }()

    /*
    Button for adding tasks to table
     */
    def Button addTaskButton = {
        def btn = new Button()
        btn.description = "Add new task"
        btn.icon = FontAwesome.PLUS
        btn.addClickListener({
            TaskService.instance.tasks << ([message: 'New Task'] as Task)
        })
        btn
    }()

    /*
    Button for removing selected tasks
     */
    def Button removeTaskButton = {
        def btn = new Button()
        btn.description = "Remove selected task"
        btn.enabled = table.value != null
        btn.icon = FontAwesome.MINUS
        btn.addClickListener({
            TaskService.instance.tasks.remove(table.value as Task)
        } as Button.ClickListener)

        // Only enable removal when task is selected
        table.addValueChangeListener({
            btn.enabled = it.property.value != null
        })

        btn
    }()

    /*{
    Button for editing task
     */
    def Button editTaskButton = {
        def btn = new Button()
        btn.description = "Edit selected task"
        btn.enabled = table.value != null
        btn.icon = FontAwesome.PENCIL
        btn.addClickListener({
            getUI().addWindow(new MakeMeRememberTaskEditor(table.value as Task))
        })

        // Only enable editing when task is selected
        table.addValueChangeListener({
            btn.enabled = it.property.value != null
        })
        btn
    }()

	@Override
	def void init(VaadinRequest request) {

        // Create layout
        def buttons = new VerticalLayout(addTaskButton, editTaskButton, removeTaskButton)
        buttons.width = "43px"

        content = new HorizontalLayout(buttons, table)
        content.setSizeFull()
        content.addComponents()
        content.setExpandRatio(table, 1)
    }
}
