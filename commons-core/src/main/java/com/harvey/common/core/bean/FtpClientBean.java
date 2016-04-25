package com.harvey.common.core.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FtpClientBean {

    protected static final Log log = LogFactory.getLog(FtpClientBean.class);
    private static final String ANONYMOUS_LOGIN = "anonymous";
    protected FtpConfigBean ftpConfig;
    private static final ThreadLocal<FTPClient> clients = new ThreadLocal<FTPClient>();

    public void setFtpConfig(FtpConfigBean ftpConfig) {
        this.ftpConfig = ftpConfig;
    }

    public FTPFile[] getRemoteFilesInfo(String folder) throws Exception {
        FTPClient client = getFtpClient();
        if (Boolean.TRUE.equals(ftpConfig.getEnablePassiveMode())) {
            client.enterLocalPassiveMode();
        }
        return folder == null || folder.length() == 0 ? client.listFiles() : client.listFiles(folder);
    }

    public List<String> getRemoteFilesList(String folder) throws Exception {
        FTPFile[] files = getRemoteFilesInfo(folder);
        List<String> fileList = new ArrayList<String>();
        for (FTPFile file : files) {
            if (file.isFile()) {
                fileList.add(file.getName());
            }
        }
        return fileList;
    }

    public void uploadFile(String fileName, InputStream is) throws Exception {
        FTPClient client = getFtpClient();
        if (Boolean.TRUE.equals(ftpConfig.getEnablePassiveMode())) {
            client.enterLocalPassiveMode();
        }
        String tempName = fileName + ".tmp";
        client.storeFile(tempName, is);
        client.rename(tempName, fileName);
    }

    public String downloadTextFile(String fileName) throws Exception {
        return new String(downloadFile(fileName));
    }

    public byte[] downloadFile(String fileName) throws Exception {
        FTPClient client = getFtpClient();
        if (Boolean.TRUE.equals(ftpConfig.getEnablePassiveMode())) {
            client.enterLocalPassiveMode();
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (!client.retrieveFile(fileName, os)) {
            throw new IOException("Error get remote file " + fileName + " from FTP server. Check FTP permissions and path.");
        }
        return os.toByteArray();
    }

    public void deleteFile(String fileName) throws Exception {
        FTPClient client = getFtpClient();
        client.deleteFile(fileName);
    }

    public void disConnect() {
        FTPClient client = clients.get();
        if (client != null) {
            try {
                client.logout();
                client.disconnect();
            } catch (Exception ex) {
                log.error(ex);
            } finally {
                clients.remove();
            }
        }
    }

    public void changeCurrentFolder(String folder) throws Exception {
        if (folder != null && folder.length() > 0) {
            String[] folders = folder.split("/");
            for (String f : folders) {
                getFtpClient().changeWorkingDirectory(f);
            }
        }
    }

    public FTPClient getFtpClient() throws Exception {
        FTPClient client = clients.get();
        if (client != null) {
            if (!client.isConnected()) {
                connect(client);
            }
            try {
                client.noop();
                return client;
            } catch (Exception ex) {
            }
        }
        if (Boolean.TRUE.equals(ftpConfig.getProxyEnabled())) {
            client = new FTPHTTPClient(ftpConfig.getProxyHost(), ftpConfig.getProxyPort(), ftpConfig.getProxyUser(), ftpConfig.getProxyPassword());
        } else {
            client = new FTPClient();
        }

        if (ftpConfig.getConnectTimeout() != null) {
            client.setConnectTimeout(ftpConfig.getConnectTimeout() * 1000);
            client.setDataTimeout(ftpConfig.getConnectTimeout() * 1000);
            client.setDefaultTimeout(ftpConfig.getConnectTimeout() * 1000);
        }

        if (log.isDebugEnabled()) {
            client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
        }
        clients.set(client);
        connect(client);
        client.noop();
        return client;
    }

    private void connect(FTPClient client) throws Exception {
        client.connect(ftpConfig.getFtpServer(), ftpConfig.getFtpPort());
        if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
            throw new RuntimeException("FTP server refused the connection.");
        }

        String user = ftpConfig.getFtpAccount();
        if (user == null || user.length() == 0) {
            user = ANONYMOUS_LOGIN;
        }
        if (!client.login(user, ftpConfig.getFtpPassword())) {
            throw new RuntimeException("Could not login to server");
        }

        if (Boolean.TRUE.equals(ftpConfig.getBinaryTransfer())) {
            client.setFileType(FTP.BINARY_FILE_TYPE);
        } else {
            client.setFileType(FTP.ASCII_FILE_TYPE);
        }

        if (log.isDebugEnabled()) {
            log.info("Remote system is " + client.getSystemType());
        }

        if (ftpConfig.getRemoteFolder() != null) {
            client.changeWorkingDirectory(ftpConfig.getRemoteFolder());
        }
    }

}
