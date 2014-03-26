JSR-303 Collection Validators
=============================
[![Build Status](https://travis-ci.org/jirutka/validator-collection.svg)](https://travis-ci.org/jirutka/validator-collection) [![Coverage Status](http://img.shields.io/coveralls/jirutka/validator-collection.svg)](https://coveralls.io/r/jirutka/validator-collection) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.validator/validator-collection/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.validator/validator-collection)

Neither JSR-303 nor Hibernate Validator has any constraint that can validate
each element of Collection containing simple types like String, Integer etc.
I’ve not found any suitable solution so I accepted the challenge...

```java
@EachSize(@Size(min = 5, max = 255))
List<String> values;
```

This library provides “Each” constraint for every JSR-303 constraints and just
one universal ConstraintValidator class which rules them all! Therefore it’s
quite easy to add your own constraints, you have to create just Constraint
annotation.

```java
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, TYPE})
@Constraint(validatedBy = CommonEachValidator.class)
public @interface EachYourConstraint {

    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    YourConstraint[] value();
}
```

… and that’s all!


Hibernate Validator 4.x vs. 5.x
-------------------------------

This code uses some internal classes of [Hibernate Validator](http://www.hibernate.org/subprojects/validator.html), the reference implementation of [JSR 349](http://beanvalidation.org/1.1/) (formerly [JSR 303](http://beanvalidation.org/1.0/)). Unfortunately, Hibernate has changed some internal APIs between versions 4.x and 5.x, therefore there are currently two main branches of this library:

*  [branch 1.x](https://github.com/jirutka/validator-collection/tree/1.x) for Hibernate Validator 4.x
*  [branch 2.x](https://github.com/jirutka/validator-collection/tree/2.x) for Hibernate Validator 5.x


Maven
-----

Released versions are available in The Central Repository. Just add this artifact to your project:

```xml
<dependency>
    <groupId>cz.jirutka.validator</groupId>
    <artifactId>validator-collection</artifactId>
    <version>2.0.1</version>
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
