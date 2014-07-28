package fi.jasoft.makemeremember.service

import fi.jasoft.makemeremember.model.Task
import groovy.util.logging.Log

import javax.persistence.Persistence
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

/**
 * Service for handling tasks
 */
@Singleton(lazy = true)
@Log
class TaskService {

    def tasks = {

        def factory = Persistence.createEntityManagerFactory('makemerememberdb')
        def manager = factory.createEntityManager()
        def query = manager.createNamedQuery('Task.all')

        def list = query.getResultList() as ObservableList

        //Monitor beans for changes
        list.each { Task task ->
            task.addPropertyChangeListener({ PropertyChangeEvent pce ->
                manager.transaction.begin()
                manager.persist(pce.source)
                manager.transaction.commit()
            })
        }

        // Monitor list for changes
        list.addPropertyChangeListener({

            /*
            We need to do an instanceof check since there will always be two events triggered,
            one PropertyChangeEvent and one ElementEvent. We are only interested in the latter.
             */
            if(it instanceof ObservableList.ElementEvent) {
                def e = it as ObservableList.ElementEvent
                switch (e.changeType) {

                    case ObservableList.ChangeType.ADDED:
                        def bean = (e as ObservableList.ElementAddedEvent).newValue as Task

                        // Save to db
                        manager.transaction.begin()
                        manager.persist(bean)
                        manager.transaction.commit()

                        //Monitor new bean for changes
                        bean.addPropertyChangeListener({ PropertyChangeEvent pce ->
                            manager.transaction.begin()
                            manager.persist(pce.source)
                            manager.transaction.commit()
                        })

                        println 'Saved new task'

                        break

                    case ObservableList.ChangeType.MULTI_ADD:
                        manager.transaction.begin()
                        (e as ObservableList.MultiElementAddedEvent).values.each { Task bean ->

                            // Save to db
                            manager.persist(bean)

                            //Monitor new bean for changes
                            bean.addPropertyChangeListener({ PropertyChangeEvent pce ->
                                manager.transaction.begin()
                                manager.persist(pce.source)
                                manager.transaction.commit()
                            })

                            println 'Saved new task'

                        }
                        manager.transaction.commit()
                        break

                    case ObservableList.ChangeType.REMOVED:
                        def bean = (e as ObservableList.ElementRemovedEvent).oldValue
                        manager.transaction.begin()
                        manager.remove(manager.merge(bean))
                        manager.transaction.commit()
                        log.info('Removed task')
                        break

                    case ObservableList.ChangeType.MULTI_REMOVE:
                        manager.transaction.begin()
                        (e as ObservableList.MultiElementRemovedEvent).values.each { bean ->
                            manager.remove(bean)
                            log.info('Removed task')
                        }
                        manager.transaction.commit()
                        break

                    case ObservableList.ChangeType.CLEARED:
                        manager.transaction.begin()
                        (e as ObservableList.ElementClearedEvent).values.each { bean ->
                            manager.remove(bean)
                            log.info('Removed task')
                        }
                        manager.transaction.commit()
                        break
                }
            }
        })

        list
    }()
}
