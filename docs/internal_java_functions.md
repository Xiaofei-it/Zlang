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

### _list_get(list, index)

Returns an new array with the specified component type and dimensions.

`_new_array("int", 4)` returns `new int[4]`. `_new_array("java.lang.Integer", 4, 5)` returns `new Integer[4][5]`.

**Parameters:**

type - the Class object representing the component type of the new array.

... - an array of `int` representing the dimensions of the new array. 

**Returns:**

The new array.