package io.lettuce.solon;


/**
 * Lettuce 哨兵
 *
 * @author Sorghum
 * @since 2.3.8
 */
public class LettuceSentinel {

    private String host;

    private int port;

    private String password;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
