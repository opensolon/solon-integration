package org.apache.dubbo.solon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

/**
 * @author noear
 */
public class DubboServiceInterfaceResolverTest {

    public interface ApiA {
        String a();
    }

    public interface ApiB {
        String b();
    }

    public interface BaseService {
        String base();
    }

    public interface HelloService extends BaseService {
        String hello();
    }

    public static class SingleImpl implements ApiA {
        @Override
        public String a() {
            return "a";
        }
    }

    public static class MultiImpl implements ApiA, ApiB {
        @Override
        public String a() {
            return "a";
        }

        @Override
        public String b() {
            return "b";
        }
    }

    public static class NoIfaceImpl {
    }

    public static class WithSerializable implements ApiA, Serializable {
        @Override
        public String a() {
            return "a";
        }
    }

    /**
     * Inheritance on the service interface must NOT be treated as multi-interface.
     */
    public static class HelloServiceImpl implements HelloService {
        @Override
        public String base() {
            return "base";
        }

        @Override
        public String hello() {
            return "hello";
        }
    }

    /**
     * Rare case: both parent and child interfaces appear as direct interfaces.
     * Resolver should keep only the most specific one.
     */
    public static class BothDirectImpl implements HelloService, BaseService {
        @Override
        public String base() {
            return "base";
        }

        @Override
        public String hello() {
            return "hello";
        }
    }

    @Test
    public void resolve_singleInterface() {
        Class<?> iface = DubboServiceInterfaceResolver.resolve(void.class, "", SingleImpl.class);
        Assertions.assertEquals(ApiA.class, iface);
    }

    @Test
    public void resolve_interfaceClassPriority() {
        Class<?> iface = DubboServiceInterfaceResolver.resolve(ApiB.class, "", MultiImpl.class);
        Assertions.assertEquals(ApiB.class, iface);
    }

    @Test
    public void resolve_interfaceNamePriority() {
        Class<?> iface = DubboServiceInterfaceResolver.resolve(void.class, ApiB.class.getName(), MultiImpl.class);
        Assertions.assertEquals(ApiB.class, iface);
    }

    @Test
    public void resolve_multiInterface_shouldFail() {
        Assertions.assertThrows(IllegalStateException.class,
                () -> DubboServiceInterfaceResolver.resolve(void.class, "", MultiImpl.class));
    }

    @Test
    public void resolve_noInterface_shouldFail() {
        Assertions.assertThrows(IllegalStateException.class,
                () -> DubboServiceInterfaceResolver.resolve(void.class, "", NoIfaceImpl.class));
    }

    @Test
    public void resolve_ignoreJdkInterfaces() {
        Class<?> iface = DubboServiceInterfaceResolver.resolve(void.class, "", WithSerializable.class);
        Assertions.assertEquals(ApiA.class, iface);
    }

    @Test
    public void resolve_interfaceInheritance_shouldNotExpandParent() {
        Class<?> iface = DubboServiceInterfaceResolver.resolve(void.class, "", HelloServiceImpl.class);
        Assertions.assertEquals(HelloService.class, iface);
    }

    @Test
    public void resolve_bothDirectParentAndChild_keepMostSpecific() {
        Class<?> iface = DubboServiceInterfaceResolver.resolve(void.class, "", BothDirectImpl.class);
        Assertions.assertEquals(HelloService.class, iface);
    }
}
