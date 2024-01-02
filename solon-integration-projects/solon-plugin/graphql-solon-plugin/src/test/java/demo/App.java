package demo;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Import;

/**
 * @author fuzi1996
 * @since 2.3
 */
public class App {

    public static void main(String[] args) {
        Solon.start(App.class, args, app -> {
            app.enableWebSocket(false);
        });
    }
}
