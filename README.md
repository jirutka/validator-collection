JSR-303 Collection Validators
=============================

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
@Target({METHOD, FIELD})
@Constraint(validatedBy = CommonEachValidator.class)
public @interface EachYourConstraint {

    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    YourConstraint[] value();
}
```

… and that’s all!


Maven
-----

```xml
<dependency>
    <groupId>cz.jirutka.validator</groupId>
    <artifactId>validator-collection</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

<repository>
    <id>cvut-local-repos</id>
    <name>CVUT Repository Local</name>
    <url>http://repository.fit.cvut.cz/maven/local-repos/</url>
</repository>
```

License
-------

This project is licensed under [MIT license](http://opensource.org/licenses/MIT).
