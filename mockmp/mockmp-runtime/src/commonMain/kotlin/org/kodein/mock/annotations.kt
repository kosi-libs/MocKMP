package org.kodein.mock

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public annotation class Mock

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FILE)
public annotation class UsesMocks(vararg val types: KClass<*>)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public annotation class Fake

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
public annotation class FakeProvider

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FILE)
public annotation class UsesFakes(vararg val types: KClass<*>)
