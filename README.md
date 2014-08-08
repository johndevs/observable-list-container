ObservableListContainer for Vaadin

Groovy provides conviently a List implementation (http://groovy.codehaus.org/api/groovy/util/ObservableList.html) that triggers property change events when items are added and removed from the List. If you combine that with using the @Bindable annotation on your beans fields you can convienienty listen to all modifications on the beans and list.

Vaadin components in general does however not use lists as data sources, they use Containers. So what this addon allows you to do is take any ObservableList and convert it into a container you can pass into any Vaadin component.

But doesn't the Vaadin BeanItemContainer already do this? Yes, and no. The way the BeanItemContainer listens to property changes is to wrap every bean into a BeanItem and listen to changes to the BeanItem instead. This has the downside that if you want the BeanItemContainer to work properly you cannot go and edit the beans themselves, you will always need to edit the BeanItem if you want the changes to be reflected in the BeanItemContainer. This usually clutters up API's where you are passing BeanItems instead of the real beans. 

The ObservableListContainer works differently. It attaches listeners to the ObservableList to listen to additions and removals as well as attaches listeners to the beans themselves using the @Bindable provided listener mechanism. This allows the container to monitor the bean itself whether it is being modified inside the container or somewhere far away in another CRUD edit.

To see how the ObservableListContainer can be used check out the examples and the example application over at https://github.com/johndevs/observable-list-container/tree/master/demo. 
