package fi.jasoft.makemeremember.model

import groovy.beans.Bindable
import groovy.transform.ToString

import javax.persistence.*
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport


@NamedQueries(value = [
        @NamedQuery(name = 'Task.all', query = 'SELECT t FROM Task t')
])
@Entity
@ToString
class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id

    @Temporal(TemporalType.TIMESTAMP)
    @Bindable
    Date date

    @Lob
    @Bindable
    String message

    @Bindable
    boolean done = false

    /**
     * Need to define the property change support manually since JPA is trying to persist the property change support field...
     */
    @Transient
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    def addPropertyChangeListener(PropertyChangeListener listener) { pcs.addPropertyChangeListener(listener)}
    def removePropertyChangeListener(PropertyChangeListener listener) { pcs.removePropertyChangeListener(listener)}
    def firePropertyChange(String name, oldValue, newValue) { pcs.firePropertyChange(name, oldValue, newValue)}
}