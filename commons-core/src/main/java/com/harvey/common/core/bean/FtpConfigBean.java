package com.harvey.common.core.bean;

public class FtpConfigBean {

    /**
     * 账号
     */
    private String ftpAccount;
    
    /**
     * 密码
     */
    private String ftpPassword;
    
    /**
     * 通信服务器地址
     */
    private String ftpServer;
    
    /**
     * 通信端口
     */
    private Integer ftpPort;
    
    /**
     * 是否启用Passive模式
     */
    private Boolean enablePassiveMode;
    
    /**
     * 连接超时
     */
    private Integer connectTimeout;
    
    /**
     * 远端目录
     */
    private String remoteFolder;
    
    /**
     * 代理服务器
     */
    private String proxyHost;
    /**
     * 代理端口
     */
    private Integer proxyPort;
    /**
     * 代理登录用户
     */
    private String proxyUser;
    /*
     * 代理登录密码
     */
    private String proxyPassword;
    
    /*
     * 是否使用代理
     */
    private Boolean proxyEnabled;
    
    /**
     * 是否用二进制传输
     */
    private Boolean binaryTransfer;
    
    public String getFtpAccount() {
        return ftpAccount;
    }

    public void setFtpAccount(String ftpAccount) {
        this.ftpAccount = ftpAccount;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public Integer getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(Integer ftpPort) {
        this.ftpPort = ftpPort;
    }

    public String getRemoteFolder() {
        return remoteFolder;
    }

    public void setRemoteFolder(String remoteFolder) {
        this.remoteFolder = remoteFolder;
    }

    public Boolean getEnablePassiveMode() {
        return enablePassiveMode;
    }

    public void setEnablePassiveMode(Boolean enablePassiveMode) {
        this.enablePassiveMode = enablePassiveMode;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public Boolean getProxyEnabled() {
        return proxyEnabled;
    }

    public void setProxyEnabled(Boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    public Boolean getBinaryTransfer() {
        return binaryTransfer;
    }

    public void setBinaryTransfer(Boolean binaryTransfer) {
        this.binaryTransfer = binaryTransfer;
    }

    @Override
    public String toString() {
        return "FtpConfigBean [ftpAccount=" + ftpAccount + ", ftpPassword=" + ftpPassword + ", ftpServer=" + ftpServer + ", ftpPort=" + ftpPort + ", enablePassiveMode=" + enablePassiveMode + ", connectTimeout=" + connectTimeout
                + ", remoteFolder=" + remoteFolder + ", proxyHost=" + proxyHost + ", proxyPort=" + proxyPort + ", proxyUser=" + proxyUser + ", proxyPassword=" + proxyPassword + ", proxyEnabled=" + proxyEnabled + ", binaryTransfer=" + binaryTransfer
                + "]";
    }
}
