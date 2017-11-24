# Internal Java Functions

This chapter introduces the internal Java functions provided by the Zlang programming language.
Please note that, you must pass the parameter of the correct type to the function.
Otherwise, an error will occur at runtime.

Some Zlang functions allows a variable number of parameters, which is represented by an ellipsis (...). 

## Array functions

### _length(arr)

Returns the length of the array.

**Parameters:**

arr - the array.

**Returns:**

The length of the array.

### _array_of(...)

Returns an new array containing the objects passed to it.

`_array_of(1, 4, 3)` returns {1, 4, 3}. `_array_of("abc", 45, 8.9)` returns {"abc", 45, 8.9}.

**Parameters:**

... - the objects to put into the array.

**Returns:**

The new array.

### _new_array(type, ...)

Returns an new array with the specified component type and dimensions.

`_new_array("int", 4)` returns `new int[4]`. `_new_array("java.lang.Integer", 4, 5)` returns `new Integer[4][5]`.

**Parameters:**

type - the Class object representing the component type of the new array.

... - an array of `int` representing the dimensions of the new array. 

**Returns:**

The new array.

## List functions

### _new_list()

Returns a new list.

**Returns:**

A new list.

### _size(list)

Returns the number of elements in the list.

**Parameters:**

list - the list.

**Returns:**

The number of elements in the list.

### _is_empty(list)

Returns true if the list contains no elements.

**Parameters:**

list - the list.

**Returns:**

true if the list contains no elements, false otherwise.

### _add(list, element)

Appends the specified element to the end of the list.

**Parameters:**

list - the list.

element - element to be appended to the list.

### _remove(list, element)

Removes the first occurrence of the specified element from the list, if it is present.

**Parameters:**

list - the list.

element -  element to be removed from the list, if present.

**Returns:**

true if the list contained the specified element.

### _contains(list, element)

Returns true if the list contains the specified element.

**Parameters:**

list - the list.

element -  whose presence in the list is to be tested.

**Returns:**

true if the list contains the specified element.

### _list_get(list, index)

Returns the element at the specified position in the list.

**Parameters:**

list - the list.

index - index of the element to return. 

**Returns:**

The element at the specified position in the list.

### _list_set(list, index, element)

Replaces the element at the specified position in the list with the specified element (optional operation).

**Parameters:**

list - the list.

index - index of the element to replace.

element - element to be stored at the specified position.

**Returns:**

The element previously at the specified position.

## Set

### _new_set()

Returns a new set.

**Returns:**

A new set.

### _size(set)

Returns the number of elements in the set.

**Parameters:**

set - the set.

**Returns:**

The number of elements in the set.

### _is_empty(set)

Returns true if the set contains no elements.

**Parameters:**

set - the set.

**Returns:**

true if the set contains no elements, false otherwise.

### _add(set, element)

Adds the specified element to the set if it is not already present.

**Parameters:**

set - the set.

element - element to be added to the set. 

**Returns:**

true if the set did not already contain the specified element, false otherwise.

### _remove(set, element)

Removes the specified element from the set if it is present.

**Parameters:**

set - the set.

element - element to be removed from the set, if present.

**Returns:**

true if the list contained the specified element.

### _contains(set, element)

Returns true if the set contains the specified element.

**Parameters:**

set - the set.

element - whose presence in the set is to be tested.

**Returns:**

true if the set contains the specified element.

## Map

### _new_map()

Returns a new map.

**Returns:**

A new map.

### _size(map)

Returns the number of key-value mappings in the map.

**Parameters:**

map - the map.

**Returns:**

The  number of key-value mappings in the map.

### _is_empty(map)

Returns true if the map contains no key-value mappings.

**Parameters:**

map - the map.

**Returns:**

true if the map contains no key-value mappings, false otherwise.

### _map_put(map, key, value)

Associates the specified value with the specified key in the map.
If the map previously contained a mapping for the key, the old value is replaced by the specified value.

**Parameters:**

map - the map.

key - key with which the specified value is to be associated.

value - value to be associated with the specified key.

**Returns:**

The previous value associated with key, or null if there was no mapping for key.

### _map_get(map, key)

Returns the value to which the specified key is mapped, or null if the map contains no mapping for the key.

**Parameters:**

map - the map.

key - the key whose associated value is to be returned.

**Returns:**

The value to which the specified key is mapped, or null if the map contains no mapping for the key.

### _map_contains_key(map, key)

Returns true if the map contains a mapping for the specified key.

**Parameters:**

map - the map.

key -  key whose presence in the map is to be tested.

**Returns:**

true if the map contains a mapping for the specified key.

### _map_contains_value(map, value)

Returns true if the map maps one or more keys to the specified value.

**Parameters:**

map - the map.

value - value whose presence in the map is to be tested.

**Returns:**

true if the map maps one or more keys to the specified value.