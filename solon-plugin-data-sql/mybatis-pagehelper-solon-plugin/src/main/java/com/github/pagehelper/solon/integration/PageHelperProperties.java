package com.github.pagehelper.solon.integration;

import org.noear.solon.Solon;
import org.noear.solon.Utils;

import java.util.Properties;

/**
 * 分页插件属性
 *
 * @author noear
 * @since 1.5
 */
public class PageHelperProperties {
    private final Properties properties;

    public Properties getProperties() {
        return properties;
    }

    public PageHelperProperties() {
        this(null);
    }

    public PageHelperProperties(String propPrefix) {
        if (Utils.isEmpty(propPrefix)) {
            propPrefix = "pagehelper";
        }

        properties = Solon.cfg().getProp(propPrefix);
    }


    public Boolean getOffsetAsPageNum() {
        return Boolean.valueOf(properties.getProperty("offsetAsPageNum"));
    }

    public void setOffsetAsPageNum(Boolean offsetAsPageNum) {
        properties.setProperty("offsetAsPageNum", offsetAsPageNum.toString());
    }

    public Boolean getRowBoundsWithCount() {
        return Boolean.valueOf(properties.getProperty("rowBoundsWithCount"));
    }

    public void setRowBoundsWithCount(Boolean rowBoundsWithCount) {
        properties.setProperty("rowBoundsWithCount", rowBoundsWithCount.toString());
    }

    public Boolean getPageSizeZero() {
        return Boolean.valueOf(properties.getProperty("pageSizeZero"));
    }

    public void setPageSizeZero(Boolean pageSizeZero) {
        properties.setProperty("pageSizeZero", pageSizeZero.toString());
    }

    public Boolean getReasonable() {
        return Boolean.valueOf(properties.getProperty("reasonable"));
    }

    public void setReasonable(Boolean reasonable) {
        properties.setProperty("reasonable", reasonable.toString());
    }

    public Boolean getSupportMethodsArguments() {
        return Boolean.valueOf(properties.getProperty("supportMethodsArguments"));
    }

    public void setSupportMethodsArguments(Boolean supportMethodsArguments) {
        properties.setProperty("supportMethodsArguments", supportMethodsArguments.toString());
    }

    public String getDialect() {
        return properties.getProperty("dialect");
    }

    public void setDialect(String dialect) {
        properties.setProperty("dialect", dialect);
    }

    public String getHelperDialect() {
        return properties.getProperty("helperDialect");
    }

    public void setHelperDialect(String helperDialect) {
        properties.setProperty("helperDialect", helperDialect);
    }

    public Boolean getAutoRuntimeDialect() {
        return Boolean.valueOf(properties.getProperty("autoRuntimeDialect"));
    }

    public void setAutoRuntimeDialect(Boolean autoRuntimeDialect) {
        properties.setProperty("autoRuntimeDialect", autoRuntimeDialect.toString());
    }

    public Boolean getAutoDialect() {
        return Boolean.valueOf(properties.getProperty("autoDialect"));
    }

    public void setAutoDialect(Boolean autoDialect) {
        properties.setProperty("autoDialect", autoDialect.toString());
    }

    public Boolean getCloseConn() {
        return Boolean.valueOf(properties.getProperty("closeConn"));
    }

    public void setCloseConn(Boolean closeConn) {
        properties.setProperty("closeConn", closeConn.toString());
    }

    public String getParams() {
        return properties.getProperty("params");
    }

    public void setParams(String params) {
        properties.setProperty("params", params);
    }

    public Boolean getDefaultCount() {
        return Boolean.valueOf(properties.getProperty("defaultCount"));
    }

    public void setDefaultCount(Boolean defaultCount) {
        properties.setProperty("defaultCount", defaultCount.toString());
    }

    public String getDialectAlias() {
        return properties.getProperty("dialectAlias");
    }

    public void setDialectAlias(String dialectAlias) {
        properties.setProperty("dialectAlias", dialectAlias);
    }
}