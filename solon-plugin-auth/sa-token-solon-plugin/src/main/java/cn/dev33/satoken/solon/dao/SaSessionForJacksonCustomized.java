package cn.dev33.satoken.solon.dao;

import cn.dev33.satoken.session.SaSession;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson定制版SaSession，忽略 timeout 等属性的序列化 
 *  
 * @author click33
 * @since 1.34.0
 */
@JsonIgnoreProperties({"timeout"})
public class SaSessionForJacksonCustomized extends SaSession {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7600983549653130681L;

	public SaSessionForJacksonCustomized() {
		super();
	}

	/**
	 * 构建一个Session对象
	 * @param id Session的id
	 */
	public SaSessionForJacksonCustomized(String id) {
		super(id);
	}

}
