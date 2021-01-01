package com.test.app

import com.alexshafir.classfieldequality.annotations.FieldEquality

@FieldEquality(Class1::class)
data class Class2(
    val param1:String,
    val param2:String
)

val t:Class2 = Class2("a", "b")
fun test() {
    t.mapToOrigin()
}