package graphql.solon.controller;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;

/**
 * @author fuzi1996
 * @since 2.3
 */
@Controller
public class GraphiqlController {

    @Mapping("/graphiql")
    public ModelAndView graphiql() {
        return new ModelAndView("index.html");
    }
}
