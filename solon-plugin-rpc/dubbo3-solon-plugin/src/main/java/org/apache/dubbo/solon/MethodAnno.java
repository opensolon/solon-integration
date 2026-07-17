package org.apache.dubbo.solon;

import org.apache.dubbo.config.annotation.Argument;
import org.apache.dubbo.config.annotation.Method;
import org.noear.solon.Solon;

import java.lang.annotation.Annotation;

/**
 * Wrap {@link Method} so string attributes support Solon config templates ({@code ${...}}).
 *
 * @author noear
 * @since 4.0.4
 */
public class MethodAnno implements Method {
    private final Method anno;

    public MethodAnno(Method anno) {
        this.anno = anno;
    }

    private String name;

    @Override
    public String name() {
        if (name == null) {
            name = tmpl(anno.name());
        }
        return name;
    }

    @Override
    public int timeout() {
        return anno.timeout();
    }

    @Override
    public int retries() {
        return anno.retries();
    }

    private String loadbalance;

    @Override
    public String loadbalance() {
        if (loadbalance == null) {
            loadbalance = tmpl(anno.loadbalance());
        }
        return loadbalance;
    }

    @Override
    public boolean async() {
        return anno.async();
    }

    @Override
    public boolean sent() {
        return anno.sent();
    }

    @Override
    public int actives() {
        return anno.actives();
    }

    @Override
    public int executes() {
        return anno.executes();
    }

    @Override
    public boolean deprecated() {
        return anno.deprecated();
    }

    @Override
    public boolean sticky() {
        return anno.sticky();
    }

    @Override
    public boolean isReturn() {
        return anno.isReturn();
    }

    private String oninvoke;

    @Override
    public String oninvoke() {
        if (oninvoke == null) {
            oninvoke = tmpl(anno.oninvoke());
        }
        return oninvoke;
    }

    private String onreturn;

    @Override
    public String onreturn() {
        if (onreturn == null) {
            onreturn = tmpl(anno.onreturn());
        }
        return onreturn;
    }

    private String onthrow;

    @Override
    public String onthrow() {
        if (onthrow == null) {
            onthrow = tmpl(anno.onthrow());
        }
        return onthrow;
    }

    private String cache;

    @Override
    public String cache() {
        if (cache == null) {
            cache = tmpl(anno.cache());
        }
        return cache;
    }

    private String validation;

    @Override
    public String validation() {
        if (validation == null) {
            validation = tmpl(anno.validation());
        }
        return validation;
    }

    private String merger;

    @Override
    public String merger() {
        if (merger == null) {
            merger = tmpl(anno.merger());
        }
        return merger;
    }

    @Override
    public Argument[] arguments() {
        return anno.arguments();
    }

    @Override
    public String[] parameters() {
        // Always empty here: MethodConfig.appendAnnotation uses toStringMap (even pairs only).
        // DubboAnnotationSupport re-applies flexible convertParameters after construction.
        return new String[0];
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Method.class;
    }

    private static String tmpl(String text) {
        if (text == null) {
            return null;
        }
        if (Solon.cfg() == null) {
            return text;
        }
        return Solon.cfg().getByTmpl(text);
    }
}
