package org.apache.dubbo.solon;

import org.apache.dubbo.config.ProtocolConfig;

import java.util.ArrayList;

/**
 * Legacy list holder for protocol configs.
 * <p>
 * Binding is now handled by {@link DubboConfigBinder}; this class is retained only for
 * binary/source compatibility and should not be used in new code.
 *
 * @author noear
 * @since 1.9
 * @deprecated use {@link DubboConfigBinder#bindMulti(org.noear.solon.core.Props, String, Class)}
 */
@Deprecated
public class Protocols extends ArrayList<ProtocolConfig> {
}
