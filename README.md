[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/regitail/promptpay-emvco-generator-java/master/LICENSE) [![Actions Status](https://github.com/regitail/promptpay-emvco-generator-java/workflows/Java%20CI/badge.svg)](https://github.com/regitail/promptpay-emvco-generator-java/actions)
### PromptPay Generator for Java and JVM languages

#### Dependencies:
QRGen: https://github.com/kenglxn/QRGen

java-crc: https://github.com/snksoft/java-crc

#### Basic Usage

Maven:
```xml
<dependencies>
    <dependency>
        <groupId>com.regitail</groupId>
        <artifactId>promptpay-emvco-generator</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

Gradle:

```gradle
dependencies {
    compile 'com.regitail:promptpay-emvco-generator:1.0.0'
}
```

Java file:
```java
String target = "0812345678";
Double amount = 1025.25;

PromptPayEMVCoGenerator promptpay = new PromptPayEMVCoGenerator(target, amount);
File f = promptpay.generateQRCode(filename, ImageType.PNG);
```
