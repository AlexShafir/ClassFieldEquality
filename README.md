# ClassFieldEquality
Kotlin library to validate equality of Kotlin classes between Clean Arch layers.
Suitable for Android and Kotlin.

## Motivation

In Clean Architecture one has to create Kotlin-only library for domain layer.

Other libraries, such as ORM, may require putting annotations onto your classes - but you are restricted to do so by Clean Arch.

So you need to create copies. For regular classes you can get away with extension. 

However, if you use data classes or need to extend from Realm objects, you can only copy class definition completely.

In this case, there are no compile-time guarantees that these classes are actually same.

Here comes my ClassFieldEquality library.

Relevant discussions/articles:
- [SO: Extend data class in Kotlin](https://stackoverflow.com/questions/26444145/extend-data-class-in-kotlin)
- [SO: Uncle Bob's clean architecture - An entity/model class for each layer?](https://softwareengineering.stackexchange.com/questions/303478/uncle-bobs-clean-architecture-an-entity-model-class-for-each-layer)
- [See Change 1](https://www.toptal.com/android/benefits-of-clean-architecture-android)

## How it works

It basically checks that two classes have: same amount of fields, same field types, same field names + generates mapper. That's it.

Both classes can be one of: class, data class.

Library itself is comprised of 3 artifacts:

- Android Lint module: Executes checks w/o compilation, shows results in IDE and aborts build in case of error.
- Annotation Processor module: Generates mapper (extension function) from target class to origin class.
You need to run Build first. Currently aborts silently if error is detected.
- Annotation module: Plain annotations (currently `@FieldEquality` only).

## Adding to your project

In your `build.gradle`:

```gradle
repositories {
  jcenter()
}

implementation "com.alexshafir.classfieldequality:annotations:1.0.0"
kapt "com.alexshafir.classfieldequality:processor:1.0.0"
lintChecks "com.alexshafir.classfieldequality:lint:1.0.0" // Only on Android
```

## How to use

For example below no error will be shown and mapper will be generated
```kotlin

package com.test.app
import com.alexshafir.classfieldequality.annotations.FieldEquality

data class Class1(
    val param1:String,
    val param2:String
)

@FieldEquality(Class1::class)
data class Class2(
    val param1:String,
    val param2:String
)

val target:Class2 = Class2("a", "b")
fun test() {
    val origin:Class1 = t.mapToOrigin() // Generated after you run Build
}

```

## Contributing
I will gladly take both PR and issues.

Both Lint and Annotation Processor are covered with tests (tested in isolation).

Tests are regular JUnit. Inside project Android app module is present too, so you can test all modules together.

## Roadmap
- Currently Annotation Processor (AP) fails silently as it assumes Android Lint will take care of signalling errors.
However, on Kotlin Multiplatform Lint is not supported, so AP should have setting like silentMode on/off.

## License
Licensed under MIT - see [LICENSE](/LICENSE) file.

