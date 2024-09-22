package cn.dev33.satoken.solon.integration;

import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.filter.SaFilter;
import cn.dev33.satoken.filter.SaFilterAuthStrategy;
import cn.dev33.satoken.filter.SaFilterErrorStrategy;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import org.noear.solon.Solon;
import org.noear.solon.core.handle.*;
import org.noear.solon.core.route.RoutingTable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * sa-token 基于路由的过滤式鉴权（增加了注解的处理）；使用优先级要低些
 *
 * 对静态文件有处理效果
 *
 * order: -100 (SaTokenInterceptor 和 SaTokenFilter 二选一；不要同时用)
 *
 * @author noear
 * @since 1.10
 */
public class SaTokenFilter implements SaFilter, Filter { //之所以改名，为了跟 SaTokenInterceptor 形成一对

    /**
     * 是否打开注解鉴权
     */
    public boolean isAnnotation = true;

    // ------------------------ 设置此过滤器 拦截 & 放行 的路由

    /**
     * 拦截路由
     */
    public List<String> includeList = new ArrayList<>();

    /**
     * 放行路由
     */
    public List<String> excludeList = new ArrayList<>();

    @Override
    public SaTokenFilter addInclude(String... paths) {
        includeList.addAll(Arrays.asList(paths));
        return this;
    }

    @Override
    public SaTokenFilter addExclude(String... paths) {
        excludeList.addAll(Arrays.asList(paths));
        return this;
    }

    @Override
    public SaTokenFilter setIncludeList(List<String> pathList) {
        includeList = pathList;
        return this;
    }

    @Override
    public SaTokenFilter setExcludeList(List<String> pathList) {
        excludeList = pathList;
        return this;
    }

    /**
     * 获取 [拦截路由] 集合
     *
     * @return see note
     */
    public List<String> getIncludeList() {
        return includeList;
    }

    /**
     * 获取 [放行路由] 集合
     *
     * @return see note
     */
    public List<String> getExcludeList() {
        return excludeList;
    }

    // ------------------------ 钩子函数

    /**
     * 认证函数：每次请求执行
     */
    public SaFilterAuthStrategy auth = r -> {
    };

    /**
     * 异常处理函数：每次[认证函数]发生异常时执行此函数
     */
    public SaFilterErrorStrategy error = e -> {
        if (e instanceof SaTokenException) {
            throw (SaTokenException) e;
        } else {
            throw new SaTokenException(e);
        }
    };

    /**
     * 前置函数：在每次[认证函数]之前执行
     *      <b>注意点：前置认证函数将不受 includeList 与 excludeList 的限制，所有路由的请求都会进入 beforeAuth</b>
     */
    public SaFilterAuthStrategy beforeAuth = r -> {
    };

    @Override
    public SaTokenFilter setAuth(SaFilterAuthStrategy auth) {
        this.auth = auth;
        return this;
    }

    @Override
    public SaTokenFilter setError(SaFilterErrorStrategy error) {
        this.error = error;
        return this;
    }

    @Override
    public SaTokenFilter setBeforeAuth(SaFilterAuthStrategy beforeAuth) {
        this.beforeAuth = beforeAuth;
        return this;
    }


    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        try {
            //查找当前主处理
            Handler mainHandler = Solon.app().router().matchMain(ctx);
            if (mainHandler instanceof Gateway) {
                //支持网关处理
                Gateway gateway = (Gateway) mainHandler;
                RoutingTable<Handler> mainRouting = gateway.getMainRouting();
                MethodType method = MethodTypeUtil.valueOf(ctx.method());
                mainHandler = mainRouting.matchOne(ctx.pathNew(), method);
            }
            Action action = (mainHandler instanceof Action ? (Action) mainHandler : null);

            //1.执行前置处理（主要是一些跨域之类的）
            if(beforeAuth != null) {
                beforeAuth.run(mainHandler);
            }

            //先路径过滤下（包括了静态文件）
            Handler finalMainHandler = mainHandler;
            SaRouter.match(includeList).notMatch(excludeList).check(r -> {
                //2.执行注解处理
                if(authAnno(action)) {
                    //3.执行规则处理（如果没有被 @SaIgnore 忽略）
                    auth.run(finalMainHandler);
                }
            });
        } catch (StopMatchException e) {
            // StopMatchException 异常代表：停止匹配，进入Controller
        } catch (SaTokenException e) {
            // 1. 获取异常处理策略结果
            Object result;
            if (e instanceof BackResultException) {
                result = e.getMessage();
            } else {
                result = error.run(e);
            }

            // 2. 写入输出流
            if (result != null) {
                ctx.render(result);
            }
            ctx.setHandled(true);
            return;
        }

        chain.doFilter(ctx);
    }

    private boolean authAnno(Action action) {
        //2.验证注解处理
        if (isAnnotation && action != null) {
            // 注解校验
            try{
                Method method = action.method().getMethod();
                SaAnnotationStrategy.instance.checkMethodAnnotation.accept(method);
            } catch (StopMatchException ignored) {
                return false;
            }
        }

        return true;
    }
}
