package features;

import com.drools.solon.KieTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.runtime.KieSession;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit5Extension;

@ExtendWith(SolonJUnit5Extension.class)
public class DemoAppTests1 {

	@Inject
	private KieTemplate kieTemplate;

	@Test
	public void test() {
		KieSession ks = kieTemplate.getKieSession("fifuNine.drl");

		System.out.println(ks);
	}
	
}
