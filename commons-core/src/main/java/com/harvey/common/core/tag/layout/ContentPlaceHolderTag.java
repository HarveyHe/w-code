package com.harvey.common.core.tag.layout;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang3.StringUtils;

public class ContentPlaceHolderTag extends TagSupport {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String ignoreFor;

    public String getIgnoreFor() {
        return ignoreFor;
    }

    public void setIgnoreFor(String ignoreFor) {
        this.ignoreFor = ignoreFor;
    }

    private Boolean isIgnore(){
        return StringUtils.isNotEmpty(ignoreFor) && StringUtils.isNotEmpty(pageContext.getRequest().getParameter(ignoreFor));
    }
    
    @Override
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        String contentId = this.id;
        Object obj = ContentPageTag.getContent(pageContext.getRequest(), contentId);
        try {
            if (obj != null && !isIgnore()) {
                if (obj instanceof char[]) {
                    char[] content = (char[])obj;
                    out.write(content);
                } else {
                    out.write(obj.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }

}
