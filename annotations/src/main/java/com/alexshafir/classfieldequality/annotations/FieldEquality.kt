package com.alexshafir.classfieldequality.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class FieldEquality(
    val value: KClass<*>
)