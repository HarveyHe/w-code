package com.harvey.common.core.tag.pagination;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang3.StringUtils;
import org.beetl.core.BeetlKit;

import com.harvey.common.core.model.PagingInfo;

public class PaginationTag extends BodyTagSupport {

    private static final long serialVersionUID = 1L;
    private static final String JAVASCRIPT_PREFIX = "javascript:";

    private String boxCss;

    private String activeCss;

    private String disabledCss;

    private String pagingInfo;

    private String link;

    private String skipBox;

    private String firstPageLabel;
    private String prevPageLabel;
    private String nextPageLabel;
    private String lastPageLabel;
    private String gotoPageLabel;
    private String totalPageLabelFmt;

    public String getFirstPageLabel() {
        return firstPageLabel;
    }

    public void setFirstPageLabel(String firstPageLabel) {
        this.firstPageLabel = firstPageLabel;
    }

    public String getPrevPageLabel() {
        return prevPageLabel;
    }

    public void setPrevPageLabel(String prevPageLabel) {
        this.prevPageLabel = prevPageLabel;
    }

    public String getNextPageLabel() {
        return nextPageLabel;
    }

    public void setNextPageLabel(String nextPageLabel) {
        this.nextPageLabel = nextPageLabel;
    }

    public String getLastPageLabel() {
        return lastPageLabel;
    }

    public void setLastPageLabel(String lastPageLabel) {
        this.lastPageLabel = lastPageLabel;
    }

    public PaginationTag() {
        super();
        boxCss = "pagination";
        activeCss = "active";
        disabledCss = "disabled";
        pagingInfo = "pagingInfo";
        this.firstPageLabel = "第一页";
        this.prevPageLabel = "«";
        this.nextPageLabel = "»";
        this.lastPageLabel = "最后一页";
        this.gotoPageLabel = "跳转到";
        this.totalPageLabelFmt = "共有%s页";
    }

    public String getBoxCss() {
        return boxCss;
    }

    public String getDisabledCss() {
        return disabledCss;
    }

    public void setDisabledCss(String disabledCss) {
        this.disabledCss = disabledCss;
    }

    public String getActiveCss() {
        return activeCss;
    }

    public void setActiveCss(String activeCss) {
        this.activeCss = activeCss;
    }

    public void setBoxCss(String boxCss) {
        this.boxCss = boxCss;
    }

    public String getPagingInfo() {
        return pagingInfo;
    }

    public void setPagingInfo(String pagingInfo) {
        this.pagingInfo = pagingInfo;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSkipBox() {
        return skipBox;
    }

    public void setSkipBox(String skipBox) {
        this.skipBox = skipBox;
    }

    private String getLinkInternal() {
        if (StringUtils.startsWithIgnoreCase(this.link, JAVASCRIPT_PREFIX)) {
            return this.link;
        }
        HttpServletRequest request = (HttpServletRequest) (this.pageContext.getRequest());
        String url = StringUtils.isNotBlank(this.link) ? this.link : request.getRequestURI() + "?pageNo=${index}";
        StringBuilder sb = new StringBuilder(url);
        Map<String, String> queryMap = getParameterMap();// HttpUtils.parseQueryString(request.getQueryString());
        if (!queryMap.isEmpty()) {
            queryMap.remove("pageNo");
            Iterator<Map.Entry<String, String>> iterator = queryMap.entrySet().iterator();
            if (url.indexOf('?') < 0) {
                sb.append('?');
            } else {
                sb.append('&');
            }
            while (iterator.hasNext()) {
                Map.Entry<String, String> item = iterator.next();
                sb.append(item.getKey()).append('=').append(item.getValue());
                if (iterator.hasNext()) {
                    sb.append('&');
                }
            }
        }
        return sb.toString();
    }

    private Map<String, String> getParameterMap() {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration<String> names = this.pageContext.getRequest().getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String val = this.pageContext.getRequest().getParameter(name);
            if (!StringUtils.isEmpty(val)) {
                try {
                    val = URLEncoder.encode(val, "UTF-8");
                    map.put(name, val);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    private Map<String, Object> getAttributeMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        Enumeration<String> names = this.pageContext.getRequest().getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object val = this.pageContext.getRequest().getAttribute(name);
            if (val != null) {
                map.put(name, val);
            }
        }
        return map;
    }

    @Override
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    private int[] calcIndex(int pageNo, int totalPage) {
        int start, end;
        start = Math.max(pageNo - 2, 1);
        end = Math.min(pageNo + 2, totalPage);
        if(pageNo < 3){
            end = Math.min(5,totalPage);
        }else if(pageNo + 2 > totalPage){
            start = Math.max(Math.min(totalPage - 4,pageNo),1);
        }
        return new int[] { start, end };
    }

    @Override
    public int doEndTag() throws JspException {
        PagingInfo pi = null;
        Object obj = this.pageContext.getRequest().getAttribute(this.pagingInfo);
        if (!(obj instanceof PagingInfo)) {
            return EVAL_PAGE;
        }
        pi = (PagingInfo) obj;
        boolean isSkipBox = "yes".equalsIgnoreCase(this.skipBox) || "true".equalsIgnoreCase(this.skipBox);
        JspWriter writer = this.pageContext.getOut();
        String linkUrl = this.getLinkInternal();
        try {
            BodyContent content = this.getBodyContent();
            String template = content != null ? StringUtils.trimToEmpty(this.getBodyContent().getString()) : null;
            Map<String, Object> paramMap = this.getAttributeMap();
            paramMap.put("link", linkUrl);
            paramMap.put("pagingInfo", pi);
            paramMap.put("index", 1);
            paramMap.put("text", null);
            if (!isSkipBox) {
                writer.write("<div class=\"");
                writer.write(this.boxCss);
                writer.write("\"><ul>");
            }
            //writer.write("<li><a href=\"#\">" + (pi.getPageNo() * pi.getPageSize()) + '/' + pi.getTotalRows() + "</a></li>");
            int totalPage = pi.getTotalPages();
            if (StringUtils.isEmpty(template)) {
                template = linkUrl;
                if (totalPage == 0 || pi.getPageNo() <= 1) {
                    writer.write("<li class=\"" + this.disabledCss + "\"><a href=\"#\">" + this.firstPageLabel + "</a></li>");
                    writer.write("<li class=\"" + this.disabledCss + "\"><a href=\"#\">" + this.prevPageLabel + "</a></li>");
                } else {
                    paramMap.put("index", 1);
                    paramMap.put("text", this.firstPageLabel);
                    writer.write("<li><a href=\"");
                    BeetlKit.renderTo(template, writer, paramMap);
                    writer.write("\">" + this.firstPageLabel + "</a></li>");

                    paramMap.put("index", pi.getPageNo() - 1);
                    paramMap.put("text", this.prevPageLabel);
                    writer.write("<li><a href=\"");
                    BeetlKit.renderTo(template, writer, paramMap);
                    writer.write("\">" + this.prevPageLabel + "</a></li>");
                }
                int[] indexs = calcIndex(pi.getPageNo(),totalPage);
                if(indexs[0] > 1){
                    writer.write("<li><a href=\"#\">...</a></li>");
                }
                for (int i = indexs[0]; i <= indexs[1]; i++) {
                    paramMap.put("text", i);
                    paramMap.put("index", i);
                    if (i == pi.getPageNo()) {
                        writer.write("<li class=\"" + this.activeCss + "\"><a href=\"#\">" + i + "</a></li>");
                    } else {
                        paramMap.put("index", i);
                        paramMap.put("text", i);
                        writer.write("<li><a href=\"");
                        BeetlKit.renderTo(template, writer, paramMap);
                        writer.write("\">" + i + "</a></li>");
                    }
                }
                if(indexs[1] < totalPage){
                    writer.write("<li><a href=\"#\">...</a></li>");
                }
                if (totalPage == 0) {
                    // 默认输出一项
                    writer.write("<li class=\"" + this.disabledCss + "\"><a href=\"#\">1</a></li>");
                }

                if (totalPage == 0 || pi.getPageNo() >= totalPage) {
                    writer.write("<li class=\"" + this.disabledCss + "\"><a href=\"#\">" + this.nextPageLabel + "</a></li>");
                    writer.write("<li class=\"" + this.disabledCss + "\"><a href=\"#\">" + this.lastPageLabel + "</a></li>");
                } else {
                    paramMap.put("index", pi.getPageNo() + 1);
                    paramMap.put("text", this.nextPageLabel);
                    writer.write("<li><a href=\"");
                    BeetlKit.renderTo(template, writer, paramMap);
                    writer.write("\">" + this.nextPageLabel + "</a></li>");

                    paramMap.put("index", totalPage);
                    paramMap.put("text", this.lastPageLabel);
                    writer.write("<li><a href=\"");
                    BeetlKit.renderTo(template, writer, paramMap);
                    writer.write("\">" + this.lastPageLabel + "</a></li>");
                }

            } else {
                // 首页
                paramMap.put("text", this.firstPageLabel);
                BeetlKit.renderTo(template, writer, paramMap);
                // 上一页
                paramMap.put("text", this.prevPageLabel);
                paramMap.put("index", pi.getPageNo() - 1);
                BeetlKit.renderTo(template, writer, paramMap);

                for (int i = 1; i <= totalPage; i++) {
                    paramMap.put("text", i);
                    paramMap.put("index", i);
                    BeetlKit.renderTo(template, writer, paramMap);
                }
                if (totalPage == 0) {
                    // 默认输出一项
                    paramMap.put("text", 1);
                    paramMap.put("index", 1);
                    BeetlKit.renderTo(template, writer, paramMap);
                }

                // 下一页
                paramMap.put("text", this.nextPageLabel);
                paramMap.put("index", pi.getPageNo() + 1);
                BeetlKit.renderTo(template, writer, paramMap);

                // 最后一页
                paramMap.put("text", this.lastPageLabel);
                paramMap.put("index", totalPage);
                BeetlKit.renderTo(template, writer, paramMap);
            }
            
            //输出分页信息
            writer.write("<li><span>");
            writer.write(this.gotoPageLabel);
            writer.write("</span></li>");
            writer.write("<li><span style=\"padding-top:2px\">");
            writer.write(String.format("<input type=\"text\" value=\"%s\" style=\"width:30px;height:11px;margin-bottom:0px;font-size:12px\" id=\"txt_%s\" />",pi.getPageNo(),this.hashCode()));
            writer.write("</span></li>");
            writer.write("<li><span>");
            writer.write(String.format("<a href=\"#\" tar-id=\"txt_%s\" onclick=\"document.location.href='%s'.replace('${index}',$('#'+$(this).attr('tar-id')).val()||1);\" />",this.hashCode(),linkUrl));
            writer.write("GO</a></span></li>");
            writer.write("<li><span>");
            writer.write(String.format(this.totalPageLabelFmt, totalPage));
            writer.write("</span></li>");
            if (!isSkipBox) {
                writer.write("</ul></div>");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new JspException(ex);
        }
        return EVAL_PAGE;
    }

    public String getGotoPageLabel() {
        return gotoPageLabel;
    }

    public void setGotoPageLabel(String gotoPageLabel) {
        this.gotoPageLabel = gotoPageLabel;
    }

    public String getTotalPageLabelFmt() {
        return totalPageLabelFmt;
    }

    public void setTotalPageLabelFmt(String totalPageLabelFmt) {
        this.totalPageLabelFmt = totalPageLabelFmt;
    }
}
