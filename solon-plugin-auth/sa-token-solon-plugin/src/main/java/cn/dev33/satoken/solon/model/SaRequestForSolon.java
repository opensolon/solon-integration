package cn.dev33.satoken.solon.model;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.util.SaFoxUtil;
import org.noear.solon.core.handle.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author noear
 * @since 1.4
 */
public class SaRequestForSolon implements SaRequest {

    protected Context ctx;

    public SaRequestForSolon() {
        ctx = Context.current();
    }

    @Override
    public Object getSource() {
        return ctx;
    }

    @Override
    public String getParam(String s) {
        return ctx.param(s);
    }

    @Override
    public List<String> getParamNames() {
        return new ArrayList<>(ctx.paramNames());
    }

    /**
     * 获取 [请求体] 里提交的所有参数
     *
     * @return 参数列表
     */
    @Override
    public Map<String, String> getParamMap() {
        return ctx.paramMap().toValueMap();
    }

    @Override
    public String getHeader(String s) {
        return ctx.header(s);
    }

    @Override
    public String getCookieValue(String name) {
        return getCookieLastValue(name);
    }

    /**
     * 在 [ Cookie作用域 ] 里获取一个值 (第一个此名称的)
     */
    @Override
    public String getCookieFirstValue(String name) {
        return ctx.cookie(name);
    }

    /**
     * 在 [ Cookie作用域 ] 里获取一个值 (最后一个此名称的)
     *
     * @param name 键
     * @return 值
     */
    @Override
    public String getCookieLastValue(String name) {
        return ctx.cookieMap().holder(name).getLastValue();
    }

    @Override
    public String getRequestPath() {
        return ctx.pathNew();
    }

    @Override
    public String getUrl() {
        String currDomain = SaManager.getConfig().getCurrDomain();
        if (!SaFoxUtil.isEmpty(currDomain)) {
            return currDomain + this.getRequestPath();
        }
        return ctx.url();
    }

    @Override
    public String getMethod() {
        return ctx.method();
    }

    @Override
    public Object forward(String path) {
        ctx.forward(path);
        return null;
    }
}
