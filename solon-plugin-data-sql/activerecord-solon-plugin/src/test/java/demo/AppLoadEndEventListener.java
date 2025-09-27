package demo;

import com.jfinal.json.JFinalJson;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.Constants;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;

/**
 * @author noear 2023/3/27 created
 */
@Component
public class AppLoadEndEventListener implements EventListener<AppLoadEndEvent> {
    @Override
    public void onEvent(AppLoadEndEvent e) throws Throwable {
        //定制 json 序列化输出（使用新的处理接管 "@json" 指令）
        e.app().renders().register(Constants.RENDER_JSON, (data, ctx) -> {
            String json = JFinalJson.getJson().toJson(data);
            ctx.outputAsJson(json);
        });
    }
}
