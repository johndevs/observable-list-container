package fi.jasoft

import com.sun.xml.internal.messaging.saaj.util.TeeInputStream
import com.vaadin.data.Property
import groovy.beans.Bindable
import org.junit.Before
import org.junit.Test

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertFalse
import static junit.framework.Assert.assertTrue
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class ObservableListContainerTest {

    class Person {

        @Bindable String name

        @Bindable int age

        String notBoundField
    }

    class Dog {

        String name

        String age
    }

    def ObservableList items

    def ObservableListContainer container

    @Before
    def void setup(){
        items  = [
                [name: 'John Doe', age: 30] as Person,
                [name: 'Jane Doe', age: 18] as Person,
                [name: 'Mr Tweets', age: 64, notBoundField:'Some irrellevant tweet'] as Person
        ] as ObservableList

        container = new ObservableListContainer(items)
    }

    @Test
    def void getPropertiesFromContainer(){
        assertEquals(5, container.containerPropertyIds.size())
        assertEquals('notBoundField', container.containerPropertyIds[0])
        assertEquals('class', container.containerPropertyIds[1])
        assertEquals('age', container.containerPropertyIds[2])
        assertEquals('propertyChangeListeners', container.containerPropertyIds[3])
        assertEquals('name', container.containerPropertyIds[4])
    }

    @Test
    def void itemIdIsBeanItself() {
        assertEquals(Person.class, container.getIdByIndex(0).getClass())
    }

    @Test
    def void updateItemOutsideOfContainer() {

        def johnDoe = items[0] as Person;

        assertEquals('John Doe', container.getItem(johnDoe).getItemProperty('name').value)

        johnDoe.name = 'Jane Doe'

        assertEquals('Jane Doe', container.getItem(johnDoe).getItemProperty('name').value)
    }

    @Test(expected = Property.ReadOnlyException)
    def void updateItemWithContainerAPI() {
        def johnDoe = items[0] as Person;

        container.getItem(johnDoe).getItemProperty('name').value = 'Jane Doe'
    }

    @Test
    def void triggerValueChangeEvent() {

        def valueChangeEventTriggered = false

        container.addValueChangeListener({
            valueChangeEventTriggered = true
        })

        def johnDoe = items[0] as Person;
        johnDoe.age = 39

        assertTrue valueChangeEventTriggered
    }

    @Test
    def void triggerItemSetChangeEventOnAddition() {

        def itemSetChangeEventTriggered = false

        container.addItemSetChangeListener({
            itemSetChangeEventTriggered = true
        })

        items.add([name: 'Mr Bean', age: 30] as Person)
    }

    @Test
    def void triggerItemSetChangeEventOnRemoval() {

        def itemSetChangeEventTriggered = false

        container.addItemSetChangeListener({
            itemSetChangeEventTriggered = true
        })

        items.remove items[0]
    }

    @Test
    def void changeUnboundFieldNoValueChangeEvent(){
        def valueChangeEventTriggered = false

        container.addValueChangeListener({
            valueChangeEventTriggered = true
        })

        def johnDoe = items[0] as Person;
        johnDoe.notBoundField = 'no event'

        assertFalse valueChangeEventTriggered
    }

    @Test
    def void containerWithUnboundBeans() {
        items  = [
                [name: 'John Doe', age: 30] as Dog,
                [name: 'Jane Doe', age: 18] as Dog,
                [name: 'Mr Tweets', age: 64] as Dog
        ] as ObservableList

        container = new ObservableListContainer(items)

        def valueChangeEventTriggered = false
        container.addValueChangeListener({
            valueChangeEventTriggered = true
        })

        def itemSetChangeEventTriggered = false
        container.addItemSetChangeListener({
            itemSetChangeEventTriggered = true
        })

        def johnDoe = items[0] as Dog
        johnDoe.name = "Scruffy"
        assertFalse valueChangeEventTriggered

        items.add([name: 'Mr Scruffy', age:100] as Dog)
        assertTrue itemSetChangeEventTriggered
    }
}