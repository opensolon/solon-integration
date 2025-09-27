package cn.afterturn.easypoi.integration;

import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

/**
 * 插件入口
 *
 * <code><pre>
 * @Controller
 * public class DemoController{
 *     @Mapping("test")
 *     public ModelAndView test(ModelAndView mv){
 *         ExportParams exportParams = null;
 *         mv.put(BigExcelConstants.PARAMS, exportParams);
 *
 *         return mv.view(BigExcelConstants.EASYPOI_BIG_EXCEL_VIEW);
 *     }
 * }
 * </pre></code>
 *
 * @author noear 2022/10/7 created
 **/
public class EasyPoiPlugin implements Plugin {

    @Override
    public void start(AppContext context) {
        if(context.cfg().getBool("easy.poi.base.enable",true) == false){
            return;
        }

        context.beanScan("cn.afterturn.easypoi");

        //注册视图渲染器
        context.app().renders().register(".poi", new EasypoiRender());
    }
}
