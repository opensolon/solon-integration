package com.baomidou.mybatisplus.solon.integration;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.solon.service.IService;
import com.baomidou.mybatisplus.solon.service.impl.ServiceImpl;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;
import org.noear.solon.core.VarHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author noear
 * @since 2.8
 */
public class MybatisAdapterPlusExt extends MybatisAdapterPlus {
    protected MybatisAdapterPlusExt(BeanWrap dsWrap) {
        super(dsWrap);
    }

    protected MybatisAdapterPlusExt(BeanWrap dsWrap, Props dsProps) {
        super(dsWrap, dsProps);
    }

    @Override
    public void injectTo(VarHolder vh) {
        //@Db("db1") IService service;
        if (IService.class.isAssignableFrom(vh.getType())) {
            vh.context().getWrapAsync(vh.getType(), serviceBw -> {
                if (serviceBw.raw() instanceof ServiceImpl) {
                    //如果是 ServiceImpl
                    injectService(vh, serviceBw);
                } else {
                    //如果不是 ServiceImpl
                    vh.setValue(serviceBw.get());
                }
            });
        } else {
            super.injectTo(vh);
        }
    }

    /**
     * 服务缓存
     */
    private Map<Class<?>, ServiceImpl> serviceCached = new HashMap<>();

    /**
     * 注入服务 IService
     */
    private void injectService(VarHolder vh, BeanWrap serviceBw) {
        ServiceImpl service = serviceBw.raw();

        if (serviceCached.containsKey(vh.getType())) {
            //从缓存获取
            service = serviceCached.get(vh.getType());
        } else {
            Object baseMapperOld = service.getBaseMapper();

            if (baseMapperOld != null) {
                Class<?> baseMapperClass = null;
                for (Class<?> clz : baseMapperOld.getClass().getInterfaces()) {
                    //baseMapperOld.getClass() 是个代理类，所以要从基类接口拿
                    if (BaseMapper.class.isAssignableFrom(clz)) {
                        baseMapperClass = clz;
                        break;
                    }
                }

                if (baseMapperClass != null) {
                    //如果有 baseMapper ，说明正常；；创建新实例，并更换 baseMapper
                    service = serviceBw.create();

                    BaseMapper baseMapper = (BaseMapper) this.getMapper(baseMapperClass);
                    service.setBaseMapper(baseMapper);

                    //缓存
                    serviceCached.put(vh.getType(), service);
                }
            }
        }

        vh.setValue(service);
    }
}