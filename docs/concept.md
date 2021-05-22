
# Component

A Component is an instance which is attached to a specific Target.
Components always has a fixed Component Type.


# Target

A Target is an instance that can have a collection of Components attached to it.


# Component Type

A Component Type represents a class whose instances can be attached to a specific Target.
Instances of Components must always exactly match their Component Type.
The Component class must inherit from Component<T>, where T is the Target class they can be attached to.
They may also implement any interfaces and may inherit from intermediary abstract classes.


# Target Type

A Target Type represents a class whose instances can have Components attached to them.
