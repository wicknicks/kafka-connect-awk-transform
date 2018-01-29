# Run AWK Scripts in a Kafka Connect Transform

This Kafka Connect evaluates a given AWK script against record values in a stream. It uses the 
[JAWK evaluator](https://github.com/hoijui/Jawk/) to parse and evaluate AWK scripts in Java.

## Building the Plugin Package
Build this repo using the maven command:

```
mvn clean package
```
This should create the `awk-transform-*-package` which should look like:

```
$ tree awk-transform/target/awk-transform-0.1-package/
awk-transform/target/awk-transform-0.1-package/
├── bcel-5.2.jar
├── jakarta-regexp-1.4.jar
├── jawk-1.03-SNAPSHOT.jar
└── slf4j-api-1.7.21.jar
```

Copy this package to your plugins directory (for example, a directory like 
`/usr/share/java/plugins/awk-transform-0.1-package`), and add the following property
to your plugin.path connect worker configuration:

```
plugin.path=/usr/share/java/plugins/
```

Note how we selected the root directory instead of the `awk-transform-0.1-package` directory.

## Configuring the Transform

The awk transform only operates on String values. If your object is more complex than a String, consider 
adding a `org.apache.kafka.connect.transforms.ExtractField` prior to the `AwkTransform` in your tranformation pipeline. 
To configure this transform, we need to add the following lines to our worker property file:

```
transforms=awktransformation
transforms.awktransformation.type=io.wicknicks.kafka.connect.awk.transform.AwkTransform
transforms.awktransformation.awk.script="{print $1, $2}"
transforms.awktransformation.awk.arguments="-F ,"
```

The above configuration will add a transformation which takes a comma separated String, and return the first 
two columns. For example, the stream:

```
Amelia,555-5553,amelia.zodiacusque@gmail.com,F
Anthony,555-3412,anthony.asserturo@hotmail.com,A
...
```

will yield records where the value is:

```
Amelia 555-5553
Anthony 555-3412
...
```

Note how the comma was replaced by a space. 
