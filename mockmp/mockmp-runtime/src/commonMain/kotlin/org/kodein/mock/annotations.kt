package org.kodein.mock

import kotlin.reflect.KClass

/**
 * Marks a `lateinit var` property of a test class to be injected with a generated mock of its type.
 *
 * The MocKMP processor reacts to this annotation by generating a mock implementation of the
 * property's type and a `Mocker.injectMocks(receiver)` extension that assigns it — call
 * `mocker.injectMocks(this)` (typically in a `@BeforeTest` method) to populate every `@Mock` and
 * [Fake] property, inherited ones included.
 *
 * Only *interfaces* and *abstract classes* can be mocked: a mock is a generated implementation of
 * its type. Use [Fake] instead for a collaborator that merely needs to exist, and `@Mock` for one
 * whose calls the test configures with `Mocker.every` or checks with `Mocker.verify`.
 *
 * Retention is [AnnotationRetention.SOURCE]: this is processor input only, absent at runtime.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public annotation class Mock

/**
 * Requests mock generation for types that are not held by a [Mock] property.
 *
 * Place it on the test class (or a test function, or the file) that needs the mocks; the processor
 * generates a mock implementation for each listed type, obtainable with `mocker.mock<T>()`.
 *
 * `mocker.mock<T>()` only resolves a type that was requested *directly* — through `@UsesMocks` or a
 * [Mock] property. Nothing is ever mocked as a side effect of mocking something else.
 *
 * Retention is [AnnotationRetention.SOURCE]: this is processor input only, absent at runtime.
 *
 * @property types The interfaces and abstract classes to generate mocks for.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FILE)
public annotation class UsesMocks(vararg val types: KClass<*>)

/**
 * Marks a `lateinit var` property of a test class to be injected with a generated fake of its type.
 *
 * A fake is an *inert instance*: a value with no behaviour whose data is zero-valued. Data classes
 * are constructed with a faked value for each property; interfaces and abstract classes get a
 * generated implementation whose members return faked values. A fake records nothing and cannot be
 * given behaviour — use [Mock] for a collaborator the test needs to configure or verify.
 *
 * Like [Mock], `@Fake` properties are populated by the generated `mocker.injectMocks(this)`.
 *
 * Retention is [AnnotationRetention.SOURCE]: this is processor input only, absent at runtime.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public annotation class Fake

/**
 * Marks a top-level function as the fake provider for its return type.
 *
 * A class with no public constructor cannot be faked automatically; annotate a function that
 * returns an instance of it with `@FakeProvider` and the processor will call that function wherever
 * a fake of the type is needed. There can be only one provider per type, and it must be top-level.
 *
 * A provider also overrides the built-in faking of a type that MocKMP would otherwise handle
 * itself, for that exact type only (a provider for `List<String>` does not affect a `List<Int>`
 * faked elsewhere).
 *
 * Retention is [AnnotationRetention.SOURCE]: this is processor input only, absent at runtime.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
public annotation class FakeProvider

/**
 * Requests fake generation for types that are not held by a [Fake] property.
 *
 * Place it on the test class (or a test function, or the file) that needs the fakes; the processor
 * generates a fake function for each listed type, obtainable with `fake<T>()`.
 *
 * `fake<T>()` only resolves a type that was requested *directly* — through `@UsesFakes` or a [Fake]
 * property. A type that MocKMP fakes only as a side effect of faking another one (for instance a
 * constructor parameter of a requested type) is deliberately not reachable through `fake<T>()`:
 * request it here yourself if you need it, so that a change to the type that used to pull it in
 * becomes a compile error rather than a silent behaviour change.
 *
 * Retention is [AnnotationRetention.SOURCE]: this is processor input only, absent at runtime.
 *
 * @property types The types to generate fakes for.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FILE)
public annotation class UsesFakes(vararg val types: KClass<*>)
