package com.harvey.common.core.tag.layout;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class ContentTag extends BodyTagSupport {
    private static final long serialVersionUID = 1L;

    @Override
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    @Override
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            int len = this.bodyContent.getBufferSize() - this.bodyContent.getRemaining();
            if (len > 0) {
                char[] buffer = new char[len];
                this.bodyContent.getReader().read(buffer);
                ContentPageTag.addContent(this.pageContext.getRequest(), this.getId(), buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }
}
