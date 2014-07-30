/*
 * Copyright 2014 John Ahlroos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.jasoft

import com.vaadin.data.Container
import com.vaadin.data.Item
import com.vaadin.data.Property

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

/**
 * Container with ObservableList backend
 */
@Newify(UnsupportedOperationException)
class ObservableListContainer implements
        Container.Indexed,
        Container.ItemSetChangeNotifier,
        Container.PropertySetChangeNotifier,
        Property.ValueChangeNotifier {

    protected class ObservedItem implements Item {

        def bean

        @Override
        def Property getItemProperty(Object id) {
            flyweightProperty.propertyId = id
            flyweightProperty.bean = bean
            return flyweightProperty
        }

        @Override
        def Collection getItemPropertyIds() {
            assert bean != null
            bean.properties.asImmutable()
        }

        @Override
        def boolean addItemProperty(id, Property property) {
            throw UnsupportedOperationException
        }

        @Override
        def boolean removeItemProperty(id) {
            throw UnsupportedOperationException
        }
    }

    protected class ObservedProperty implements Property {

        def propertyId

        def bean

        @Override
        def getValue() {
            bean != null ? bean.properties[propertyId] : null
        }

        @Override
        void setValue(Object newValue) {
            throw Property.ReadOnlyException()
        }

        @Override
        def Class getType() {
            bean != null ? bean.metaClass.properties.find{ it.name == propertyId }.type : null
        }

        @Override
        def boolean isReadOnly() {
            true
        }

        @Override
        def void setReadOnly(boolean newStatus) {

        }
    }

    private def flyweightItem = new ObservedItem()

    private def flyweightProperty = new ObservedProperty()

    final def ObservableList list

    def listChangeListener = {

        if(it instanceof ObservableList.ElementEvent) {
            def e = it as ObservableList.ElementEvent

            switch (e.changeType) {

                case ObservableList.ChangeType.ADDED:
                    def bean = (e as ObservableList.ElementAddedEvent).newValue
                    if(supportsBinding(bean)){
                        bean.addPropertyChangeListener(beanChangeListener)
                    }
                    fireItemSetChangeEvent()
                    break

                case ObservableList.ChangeType.MULTI_ADD:
                    (e as ObservableList.MultiElementAddedEvent).values.each { bean ->
                        if(supportsBinding(bean)){
                            bean.addPropertyChangeListener(beanChangeListener)
                        }
                    }
                    fireItemSetChangeEvent()
                    break

                case ObservableList.ChangeType.REMOVED:
                    def bean = (e as ObservableList.ElementRemovedEvent).oldValue
                    if(supportsBinding(bean)){
                        bean.removePropertyChangeListener(beanChangeListener)
                    }
                    fireItemSetChangeEvent()
                    break

                case ObservableList.ChangeType.MULTI_REMOVE:
                    (e as ObservableList.MultiElementRemovedEvent).values.each { bean ->
                        if(supportsBinding(bean)){
                            bean.removePropertyChangeListener(beanChangeListener)
                        }
                    }
                    fireItemSetChangeEvent()
                    break

                case ObservableList.ChangeType.CLEARED:
                    (e as ObservableList.ElementClearedEvent).values.each { bean ->
                        if(supportsBinding(bean)){
                            bean.removePropertyChangeListener(beanChangeListener)
                        }
                    }
                    fireItemSetChangeEvent()
                    break
            }
        }
    } as PropertyChangeListener

    def beanChangeListener = { PropertyChangeEvent e ->

        // Fire value change listeners for property
        valueChangeListeners.each { Property.ValueChangeListener listener ->
            listener.valueChange(new Property.ValueChangeEvent() {
                @Override
                Property getProperty() {
                    flyweightProperty.propertyId = e.propertyName
                    flyweightProperty.bean = e.source
                    return flyweightProperty
                }
            })
        }

        // TODO This is wrong but we need to do it since Table has no other means of updating its current content. The container API sucks in this way
        firePropertySetChangeEvent()

    } as PropertyChangeListener

    def itemSetChangeListeners = []

    def propertySetChangeListeners = []

    def valueChangeListeners = []

    def static boolean supportsBinding(bean){
        bean != null && bean.metaClass != null &&
        bean.metaClass.respondsTo(bean,'addPropertyChangeListener', PropertyChangeListener) &&
        bean.metaClass.respondsTo(bean,'removePropertyChangeListener', PropertyChangeListener)
    }

    def ObservableListContainer(ObservableList list){
        this.list = list

        // Listen to list changes
        this.list.addPropertyChangeListener(listChangeListener)

        // Attach listeners to already added list items
        list.each { bean ->
            if(supportsBinding(bean)){
                bean.addPropertyChangeListener(beanChangeListener)
            }
        }
    }

    def fireItemSetChangeEvent() {
        itemSetChangeListeners.each { Container.ItemSetChangeListener listener ->
            listener.containerItemSetChange(new Container.ItemSetChangeEvent() {
                @Override
                Container getContainer() {
                    ObservableListContainer.this
                }
            })
        }
    }

    def firePropertySetChangeEvent() {
        propertySetChangeListeners.each { Container.PropertySetChangeListener listener ->
            listener.containerPropertySetChange(new Container.PropertySetChangeEvent() {
                @Override
                Container getContainer() {
                    ObservableListContainer.this
                }
            })
        }
    }

    @Override
    def int indexOfId(itemId) {
        list.indexOf(itemId)
    }

    @Override
    def getIdByIndex(int index) {
        list[index]
    }

    @Override
    def List<?> getItemIds(int startIndex, int numberOfItems) {
        def endIndex = startIndex + numberOfItems - 1
        list[startIndex..endIndex]
    }

    @Override
    def addItemAt(int index) {
        throw UnsupportedOperationException()
    }

    @Override
    def Item addItemAt(int index, newItemId) {
        throw UnsupportedOperationException()
    }

    @Override
    def nextItemId(itemId) {
        list[indexOfId(itemId)+1]
    }

    @Override
    def prevItemId(itemId) {
        list[indexOfId(itemId)-1]
    }

    @Override
    def firstItemId() {
        list.isEmpty() ? null : list.first()
    }

    @Override
    def lastItemId() {
        list.isEmpty() ? null : list.last()
    }

    @Override
    def boolean isFirstId(itemId) {
        firstItemId() == itemId
    }

    @Override
    def boolean isLastId(itemId) {
        lastItemId() == itemId
    }

    @Override
    def addItemAfter(previousItemId) {
        throw UnsupportedOperationException()
    }

    @Override
    def Item addItemAfter(previousItemId, newItemId) {
        throw UnsupportedOperationException()
    }

    @Override
    def Item getItem(itemId) {
        flyweightItem.bean = list[indexOfId(itemId)]
        return flyweightItem
    }

    @Override
    def Collection getContainerPropertyIds() {
       list.isEmpty() ? Collections.emptySet() : firstItemId().properties.keySet()
    }

    @Override
    def Collection getItemIds() {
        list.asImmutable()
    }

    @Override
    def Property getContainerProperty(itemId, propertyId) {
        getItem(itemId).getItemProperty(propertyId)
    }

    @Override
    def Class getType(propertyId) {
        getContainerProperty(firstItemId(), propertyId).type
    }

    @Override
    def int size() {
        list.size()
    }

    @Override
    def boolean containsId(itemId) {
        list.contains(itemId)
    }

    @Override
    def Item addItem(itemId) {
        list << itemId
    }

    @Override
    def addItem() {
        throw UnsupportedOperationException()
    }

    @Override
    def boolean removeItem(itemId) {
        list.remove(itemId)
    }

    @Override
    def boolean addContainerProperty(propertyId, Class type, defaultValue) {
        throw UnsupportedOperationException()
    }

    @Override
    def boolean removeContainerProperty(propertyId) {
        throw UnsupportedOperationException()
    }

    @Override
    def boolean removeAllItems()  {
        list.clear()
    }

    @Override
    def void addItemSetChangeListener(Container.ItemSetChangeListener listener) {
        itemSetChangeListeners << listener
    }

    @Override
    def void addListener(Container.ItemSetChangeListener listener) {
        itemSetChangeListeners << listener
    }

    @Override
    def void removeItemSetChangeListener(Container.ItemSetChangeListener listener) {
        itemSetChangeListeners -= listener
    }

    @Override
    def void removeListener(Container.ItemSetChangeListener listener) {
        itemSetChangeListeners -= listener
    }

    @Override
    def void addPropertySetChangeListener(Container.PropertySetChangeListener listener) {
        propertySetChangeListeners << listener
    }

    @Override
    def void addListener(Container.PropertySetChangeListener listener) {
        propertySetChangeListeners << listener
    }

    @Override
    def void removePropertySetChangeListener(Container.PropertySetChangeListener listener) {
        propertySetChangeListeners -= listener
    }

    @Override
    def void removeListener(Container.PropertySetChangeListener listener) {
        propertySetChangeListeners -= listener
    }

    @Override
    def void addValueChangeListener(Property.ValueChangeListener listener) {
        valueChangeListeners << listener
    }

    @Override
    def void addListener(Property.ValueChangeListener listener) {
        valueChangeListeners << listener
    }

    @Override
    def void removeValueChangeListener(Property.ValueChangeListener listener) {
        valueChangeListeners -= listener
    }

    @Override
    def void removeListener(Property.ValueChangeListener listener) {
        valueChangeListeners -= listener
    }
}
