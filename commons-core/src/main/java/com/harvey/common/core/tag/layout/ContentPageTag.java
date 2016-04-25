package com.harvey.common.core.tag.layout;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * EVAL_BODY_INCLUDE：把Body读入存在的输出流中，doStartTag()函数可用
 * EVAL_PAGE：继续处理页面，doEndTag()函数可用
 * SKIP_BODY：忽略对Body的处理，doStartTag()和doAfterBody()函数可用
 * SKIP_PAGE：忽略对余下页面的处理，doEndTag()函数可用 EVAL_BODY_TAG：已经废止，由EVAL_BODY_BUFFERED取代
 * EVAL_BODY_BUFFERED
 * ：申请缓冲区，由setBodyContent()函数得到的BodyContent对象来处理tag的body，如果类实现了BodyTag
 * ，那么doStartTag()可用，否则非法
 * 
 * @author admin
 * 
 */
public class ContentPageTag extends BodyTagSupport {

    private static final long serialVersionUID = 1L;

    private String masterPageUrl;

    private static final String CONTENT_ATTRIBUTE_KEY = ContentPageTag.class.getName();

    public static void addContent(ServletRequest request, String contentId, Object content) {
        @SuppressWarnings("unchecked")
        Map<String, Object> contentMap = (Map<String, Object>) request.getAttribute(CONTENT_ATTRIBUTE_KEY);
        if (contentMap == null) {
            contentMap = new HashMap<String, Object>();
            request.setAttribute(CONTENT_ATTRIBUTE_KEY, contentMap);
        }
        contentMap.put(contentId, content);
    }

    public static Object getContent(ServletRequest request, String contentId) {
        @SuppressWarnings("unchecked")
        Map<String, Object> contentMap = (Map<String, Object>) request.getAttribute(CONTENT_ATTRIBUTE_KEY);
        if (contentMap == null) {
            return null;
        }
        return contentMap.remove(contentId);
    }

    @Override
    public int doAfterBody() throws JspException {
        // 忽略对Body的处理
        return SKIP_BODY;
    }

    @Override
    public int doStartTag() throws JspException {
        // 执行子标签
        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            this.pageContext.getServletContext().getRequestDispatcher(this.getMasterPageUrl()).include(this.pageContext.getRequest(), this.pageContext.getResponse());
        } catch (Exception e) {
            e.printStackTrace();
            throw new JspException(e);
        }
        return SKIP_PAGE;
    }

    public String getMasterPageUrl() {
        return this.masterPageUrl;
    }

    public void setMasterPageUrl(String masterPageUrl) {
        this.masterPageUrl = masterPageUrl;
    }

}
