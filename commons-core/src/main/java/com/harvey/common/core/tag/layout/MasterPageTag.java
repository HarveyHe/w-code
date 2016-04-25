package com.harvey.common.core.tag.layout;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * EVAL_BODY_INCLUDE：把Body读入存在的输出流中，doStartTag()函数可用   
    EVAL_PAGE：继续处理页面，doEndTag()函数可用   
    SKIP_BODY：忽略对Body的处理，doStartTag()和doAfterBody()函数可用   
    SKIP_PAGE：忽略对余下页面的处理，doEndTag()函数可用   
    EVAL_BODY_TAG：已经废止，由EVAL_BODY_BUFFERED取代   
    EVAL_BODY_BUFFERED：申请缓冲区，由setBodyContent()函数得到的BodyContent对象来处理tag的body，如果类实现了BodyTag，那么doStartTag()可用，否则非法 
 * @author admin
 *
 */
public class MasterPageTag extends BodyTagSupport {
    private static final long serialVersionUID = 1L;

    private String baseMasterPage;
    
    public String getBaseMasterPage() {
        return baseMasterPage;
    }

    public void setBaseMasterPage(String baseMasterPage) {
        this.baseMasterPage = baseMasterPage;
    }

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
        if(this.baseMasterPage != null && this.baseMasterPage.length() > 0){
            try {
                pageContext.getRequest().getRequestDispatcher(baseMasterPage).include(pageContext.getRequest(), pageContext.getResponse());
            } catch (Exception e) {
                e.printStackTrace();
                throw new JspException(e);
            }
            return EVAL_PAGE;
        }else{
            JspWriter out = pageContext.getOut();
            try {
                this.bodyContent.writeOut(out);
            } catch (IOException e) {
                e.printStackTrace();
                throw new JspException(e);
            }
        }
        return SKIP_PAGE;
    }
}
