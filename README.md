Bean Validation / Collection Validators
=======================================
[![Build Status](https://travis-ci.org/jirutka/validator-collection.svg?branch=2.x)](https://travis-ci.org/jirutka/validator-collection)
[![Coverage Status](https://coveralls.io/repos/jirutka/validator-collection/badge.png?branch=2.x)](https://coveralls.io/r/jirutka/validator-collection?branch=2.x)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.validator/validator-collection/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.validator/validator-collection)

Neither [Bean Validation 1.1][JSR-349] (JSR 303/349) nor [Hibernate Validator], the reference _(and the only one…)_
implementation of it, has any way how to simply validate a collection of simple types like String, Integer… (i.e.
validate each element of the collection).

This library provides a very simple way how to create a “pseudo constraint” (typically named as `@EachX`) for **any**
validation constraint to annotate a collection of simple types, without writing any extra validator or some ugly
wrapper classes for every collection. EachX constraints for every standard Bean Validation constraints are included.
For an example:

```java
@EachSize(min = 5, max = 255)
List<String> values;
```

How to create a custom constraint
---------------------------------

There’s a magic constraint validator [CommonEachValidator] that is used in any `@EachX` pseudo constraint. To create an
`@EachAwesome` for your own `@Awesome` constraint, just copy&paste the annotation class (i.e. all the attributes and
boilerplate meta annotations), replace `@Constraint` annotation with
`@Constraint(validatedBy = CommonEachValidator.class)` and add the annotation
`@EachConstraint(validateAs = Awesome.class)`. That’s all!

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


Hibernate Validator 4.x vs. 5.x
-------------------------------

This code uses some internal classes of [Hibernate Validator]. Unfortunately, Hibernate has changed some internal APIs
between versions 4.x and 5.x, therefore there are currently two main branches of this library:

*  [branch 1.x](https://github.com/jirutka/validator-collection/tree/1.x) for Hibernate Validator 4.x
*  [branch 2.x](https://github.com/jirutka/validator-collection/tree/2.x) for Hibernate Validator 5.x

Sadly, they changed it again in 5.1.x, so if you’re using Hibernate Validator 5.0.x, please update to 5.1.x or use
[prior version](https://github.com/jirutka/validator-collection/tree/v2.0.2) of this library.


Maven
-----

Released versions are available in The Central Repository. Just add this artifact to your project:

```xml
<dependency>
    <groupId>cz.jirutka.validator</groupId>
    <artifactId>validator-collection</artifactId>
    <version>2.1.0-SNAPSHOT</version>
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


License
-------

This project is licensed under [MIT license](http://opensource.org/licenses/MIT).

[JSR-349]: http://beanvalidation.org/1.1/spec/
[Hibernate Validator]: http://hibernate.org/validator/
[CommonEachValidator]: src/main/java/cz/jirutka/validator/collection/CommonEachValidator.java
