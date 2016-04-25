package com.harvey.common.core.beetl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.exception.ErrorInfo;

public class ParseErrorHandler implements org.beetl.core.ErrorHandler {

    @Override
    public void processExcption(BeetlException ex, Writer writer) {
        ErrorInfo error = new ErrorInfo(ex);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        if (error.getErrorCode().equals(BeetlException.CLIENT_IO_ERROR_ERROR)) {
            // 不输出详细提示信息
            pw.println("客户端IO异常:" + getResourceName(ex.resourceId) + ":" + error.getMsg());
            /*if (ex.getCause() != null) {
                this.printThrowable(sw, ex.getCause());
            }*/

        } else {

            int line = error.getErrorTokenLine();
            
            pw.append(">>").append(error.getType())
              .append(":").append(error.getErrorTokenText())
              .append(" 位于").append(Integer.toString(line))
              .append("行").append(" 资源:").append(getResourceName(ex.resourceId));

            if (error.getErrorCode().equals(BeetlException.TEMPLATE_LOAD_ERROR) && error.getMsg() != null) {
                pw.println(error.getMsg());
            }

            if (ex.getMessage() != null) {
                pw.println(ex.getMessage());
            }

            ResourceLoader resLoader = ex.gt.getResourceLoader();
            // 潜在问题，此时可能得到是一个新的模板，不过可能性很小，忽略！

            String content = null;
            try {
                Resource res = resLoader.getResource(ex.resourceId);
                // 显示前后三行的内容
                int[] range = this.getRange(line);
                content = res.getContent(range[0], range[1]);
                if (content != null) {
                    String[] strs = content.split(ex.cr);
                    int lineNumber = range[0];
                    for (int i = 0; i < strs.length; i++) {
                        pw.println("" + lineNumber);
                        pw.println("|");
                        pw.println(strs[i]);
                        lineNumber++;
                    }

                }
            } catch (IOException e) {

                // ingore

            }

            if (error.hasCallStack()) {
                pw.println("  ========================");
                pw.println("  调用栈:");
                for (int i = 0; i < error.getResourceCallStack().size(); i++) {
                    pw.println("  " + error.getResourceCallStack().get(i) + " 行：" + error.getTokenCallStack().get(i).line);
                }
            }
        }
        throw new ParseException(sw.toString(),error.getCause());
    }

    protected String getResourceName(String resourceId) {
        return resourceId;
    }

    protected int[] getRange(int line) {
        int startLine = 0;
        int endLine = 0;
        if (line > 3) {
            startLine = line - 3;
        } else {
            startLine = 1;
        }

        endLine = startLine + 6;
        return new int[] { startLine, endLine };
    }

}
