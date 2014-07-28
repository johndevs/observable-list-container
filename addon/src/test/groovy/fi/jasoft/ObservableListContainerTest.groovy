package fi.jasoft

import groovy.beans.Bindable
import org.junit.Before
import org.junit.Test

import static junit.framework.Assert.assertEquals

class ObservableListContainerTest {

    class Person {

        @Bindable String name

        @Bindable int age

        String notBoundField
    }

    def ObservableList items

    def ObservableListContainer container

    @Before
    def void setup(){
        items  = [
                [name: 'John Doe', age: 30] as Person,
                [name: 'Jane Doe', age: 18] as Person,
                [name: 'Mr Tweets', age: 64, notBoundField:'Some irrellevant tweet']
        ] as ObservableList

        container = new ObservableListContainer(items)
    }

    @Test
    def void getPropertiesFromContainer(){

        // Test property ids
        assertEquals(3, container.containerPropertyIds.size())
        assertEquals('name', container.containerPropertyIds[0])
        assertEquals('age', container.containerPropertyIds[1])
        assertEquals('notBoundField', container.containerPropertyIds[1])




    }


}