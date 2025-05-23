/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.seata.solon.autoconfigure.properties.core;

import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import static org.apache.seata.common.DefaultValues.*;
import static org.apache.seata.solon.autoconfigure.StarterConstants.TRANSPORT_PREFIX;

@Configuration
@Inject(value = "${" + TRANSPORT_PREFIX + "}",required = false)
public class TransportProperties {
    /**
     * tcp, unix-domain-socket
     */
    private String type = "TCP";
    /**
     * NIO, NATIVE
     */
    private String server = "NIO";
    /**
     * enable heartbeat
     */
    private boolean heartbeat = DEFAULT_TRANSPORT_HEARTBEAT;
    /**
     * serialization
     */
    private String serialization = "seata";
    /**
     * compressor
     */
    private String compressor = "none";

    private String protocol = "seata";

    /**
     * enable client batch send request
     */
    private boolean enableClientBatchSendRequest = DEFAULT_ENABLE_CLIENT_BATCH_SEND_REQUEST;

    /**
     * enable TM client batch send request
     */
    private boolean enableTmClientBatchSendRequest = DEFAULT_ENABLE_TM_CLIENT_BATCH_SEND_REQUEST;

    /**
     * enable RM client batch send request
     */
    private boolean enableRmClientBatchSendRequest = DEFAULT_ENABLE_RM_CLIENT_BATCH_SEND_REQUEST;

    /**
     * enable TC server batch send response
     */
    private boolean enableTcServerBatchSendResponse = DEFAULT_ENABLE_TC_SERVER_BATCH_SEND_RESPONSE;

    /**
     * rpcRmRequestTimeout
     */
    private long rpcRmRequestTimeout = DEFAULT_RPC_RM_REQUEST_TIMEOUT;

    /**
     * rpcRmRequestTimeout
     */
    private long rpcTmRequestTimeout = DEFAULT_RPC_TM_REQUEST_TIMEOUT;

    /**
     * rpcTcRequestTimeout
     */
    private long rpcTcRequestTimeout = DEFAULT_RPC_TC_REQUEST_TIMEOUT;


    public String getType() {
        return type;
    }

    public TransportProperties setType(String type) {
        this.type = type;
        return this;
    }

    public String getServer() {
        return server;
    }

    public TransportProperties setServer(String server) {
        this.server = server;
        return this;
    }

    public boolean isHeartbeat() {
        return heartbeat;
    }

    public TransportProperties setHeartbeat(boolean heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }

    public String getSerialization() {
        return serialization;
    }

    public TransportProperties setSerialization(String serialization) {
        this.serialization = serialization;
        return this;
    }

    public String getCompressor() {
        return compressor;
    }

    public TransportProperties setCompressor(String compressor) {
        this.compressor = compressor;
        return this;
    }

    public boolean isEnableClientBatchSendRequest() {
        return enableClientBatchSendRequest;
    }

    public TransportProperties setEnableClientBatchSendRequest(boolean enableClientBatchSendRequest) {
        this.enableClientBatchSendRequest = enableClientBatchSendRequest;
        return this;
    }

    public boolean isEnableTmClientBatchSendRequest() {
        return enableTmClientBatchSendRequest;
    }

    public TransportProperties setEnableTmClientBatchSendRequest(boolean enableTmClientBatchSendRequest) {
        this.enableTmClientBatchSendRequest = enableTmClientBatchSendRequest;
        return this;
    }

    public boolean isEnableRmClientBatchSendRequest() {
        return enableRmClientBatchSendRequest;
    }

    public TransportProperties setEnableRmClientBatchSendRequest(boolean enableRmClientBatchSendRequest) {
        this.enableRmClientBatchSendRequest = enableRmClientBatchSendRequest;
        return this;
    }

    public boolean isEnableTcServerBatchSendResponse() {
        return enableTcServerBatchSendResponse;
    }

    public void setEnableTcServerBatchSendResponse(boolean enableTcServerBatchSendResponse) {
        this.enableTcServerBatchSendResponse = enableTcServerBatchSendResponse;
    }

    public long getRpcRmRequestTimeout() {
        return rpcRmRequestTimeout;
    }

    public void setRpcRmRequestTimeout(long rpcRmRequestTimeout) {
        this.rpcRmRequestTimeout = rpcRmRequestTimeout;
    }

    public long getRpcTmRequestTimeout() {
        return rpcTmRequestTimeout;
    }

    public void setRpcTmRequestTimeout(long rpcTmRequestTimeout) {
        this.rpcTmRequestTimeout = rpcTmRequestTimeout;
    }

    public long getRpcTcRequestTimeout() {
        return rpcTcRequestTimeout;
    }

    public void setRpcTcRequestTimeout(long rpcTcRequestTimeout) {
        this.rpcTcRequestTimeout = rpcTcRequestTimeout;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
