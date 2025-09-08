
需要禁用 JMX

```java
import org.noear.solon.Solon;

public class DemoApp {
    public static void main(String[] args) {
        // Disable JMX for Apache Commons Pool2 which is used by Redis connection pools
        System.setProperty("org.apache.commons.pool2.impl.BaseGenericObjectPool.jmxEnabled", "false");
        
        Solon.start(DemoApp.class, args);
    }
}
```