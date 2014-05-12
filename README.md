Bean Validation / Collection Validators
=======================================
[![Build Status](https://travis-ci.org/jirutka/validator-collection.svg?branch=master)](https://travis-ci.org/jirutka/validator-collection)
[![Coverage Status](https://coveralls.io/repos/jirutka/validator-collection/badge.png?branch=master)](https://coveralls.io/r/jirutka/validator-collection?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.validator/validator-collection/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.validator/validator-collection)

Neither [Bean Validation 1.1][JSR-349] (JSR 303/349) nor [Hibernate Validator], the reference _(and the only one…)_
implementation of it, provide simple way to validate a collection of basic types like String, Integer, Date… (i.e.
validate each element of the collection).

This library allows you to easily create a “pseudo constraint” (typically named as `@EachX`) for _any_ validation
constraint to annotate a collection of simple types, without writing an extra validator or unnecessary wrapper classes
for every collection. `EachX` constraint is supported for all standard Bean Validation constraints and Hibernate
specific constraints. For example:

```java
@EachSize(min = 5, max = 255)
Collection<String> values;

@EachFuture
List<Date> dates;

@EachEmail
Set<String> emails;
```

How to create a custom constraint
---------------------------------

Every `@EachX` pseudo constraint uses the same validator, [CommonEachValidator]. To create an `@EachAwesome` for your
own `@Awesome` constraint, just copy&paste the annotation class (i.e. all the attributes and boilerplate meta
annotations), replace `@Constraint` annotation with `@Constraint(validatedBy = CommonEachValidator.class)` and add the
annotation `@EachConstraint(validateAs = Awesome.class)`. That’s all!

```java
// common boilerplate
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE})
// this is important!
@EachConstraint(validateAs = Awesome.class)
@Constraint(validatedBy = CommonEachValidator.class)
public @interface EachAwesome {

    // copy&paste all attributes from Awesome annotation here
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String someAttribute();
}
```

### The old way

The previous versions (before 2.1.0) used a different approach to write `@EachX` annotations (see
[here](https://github.com/jirutka/validator-collection/tree/v2.0.2)). It is still supported for custom constraints, but
all the built-in annotations has been already updated to the new style.

If you’re upgrading from an older version of Collection Validators, then you must update all built-in annotations
to the new style. For example:

    @EachSize(@Size(min = 5, max = 255)) -> @EachSize(min = 5, max = 255)

You _should_ also update custom annotations. The old style is still supported, but may be deprecated in the future.


Maven
-----

Released versions are available in The Central Repository. Just add this artifact to your project:

```xml
<dependency>
    <groupId>cz.jirutka.validator</groupId>
    <artifactId>validator-collection</artifactId>
    <version>2.1.0</version>
</dependency>
```

However if you want to use the last snapshot version, you have to add the Sonatype OSS repository:

```xml
<repository>
    <id>sonatype-snapshots</id>
    <name>Sonatype repository for deploying snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```

Requirements
------------

[Hibernate Validator] 4.3.1.Final and newer is supported, but 5.× is recommended.

Please note that on older versions some Hibernate specific constraints doesn’t exist, so their `@EachX` annotations will
not work (e.g. `@EachEAN`, `@EachMod10Check`, …). It’s described in JavaDoc.


License
-------

This project is licensed under [MIT license](http://opensource.org/licenses/MIT).

[JSR-349]: http://beanvalidation.org/1.1/spec/
[Hibernate Validator]: http://hibernate.org/validator/
[CommonEachValidator]: src/main/java/cz/jirutka/validator/collection/CommonEachValidator.java
