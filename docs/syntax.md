# Syntax

This chapter covers the syntax of the Zlang programming language.

## Comments

A comment starts with `/*` and can be found at any position in the line.
The characters following `/*` will be considered part of the comment, including new line characters,
up to the first `*/` closing the comment.

## Identifiers

An identifier starts with a letter or an underscore. However, it cannot start with a digit.

Thus, `_fun`, `fun` and `fun1` are legal identifiers, but `1fun` is not.
Zlang is a flexible dynamically-typed programming language which runs on the JVM, and supports access
to Java objects and interaction with Java at runtime.

### Numbers

Zlang supports all kinds of numbers which Java supports.
However, you can only write `int` and `double` in Zlang.
If you want to write a `long`, a `float`, a `BigInteger`, etc., you should do the following:

```
aLong = _new_instance("java.lang.Long", "18L");
aFloat = _new_instance("java.lang.Float", "0.5f");
aBigInteger = _new_instance("java.math.BigInteger", "890");
```

### Characters

A Zlang character is a single character surrounded by single quotes, such as `'a'`, `'1'`, `'@'`, etc.

You can escape special characters, such as `\`, `'` and `"` with the the backslash character `\`.

At runtime, a Zlang character is regarded as a Java character,
and thus can be passed to a Java function as its `char` or `java.lang.Character` parameter.

### Strings

A Zlang string a series of characters surrounded by single quotes.

At runtime, a Zlang string is regarded as a Java string,
and thus can be passed to a Java function as its `java.lang.String` parameter.

