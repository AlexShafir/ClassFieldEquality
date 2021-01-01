package com.alexshafir.classfieldequality.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class FieldEquality(
    val value: KClass<*>
)