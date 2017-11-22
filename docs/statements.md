# Statements

This chapter covers the statements of the Zlang programming language.

## Variables

### Scope of a variable

In Zlang, you do not need to declare or define a variable before accessing it.

A Zlang program consists of functions, i.e., it is a series of functions.

A variable exists and only exists in a particular function,
and can be accessed within the function. It cannot be accessed outside the function.

The scope of a variable starts from where it is assigned for the first time
and ends at the end of the function.

See the following:

```
function f1() {
  a = 1;
  b = 2;
  if (a == 1) {
    b = 3;
  }
  println(b);
}
```

The above will print `3` instead of `2`.

See an incorrect example:

```
function f2() {
  a = 1;
}

function f3() {
  b = a;
}
```

Calling `f3()` will cause an exception at runtime, because `a` is not assigned.

See another incorrect example:

```
function f4() {
  return 1;
}

c = f4();
```

This will cause an exception at compile time, because a statement exists outside a function.

### Type of a variable

The type of a variable is dynamical, depending on the value you assign to it.

That