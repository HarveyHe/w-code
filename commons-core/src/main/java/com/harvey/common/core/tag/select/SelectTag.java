package com.harvey.common.core.tag.select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.springframework.util.StringUtils;

import com.harvey.common.core.context.Context;
import com.harvey.common.core.model.DynamicModelClass;
import com.harvey.common.core.service.BacDataService;
import com.harvey.common.core.utils.HttpUtils;

public class SelectTag extends BodyTagSupport {

	private static final BacDataService bacDataService = Context.getBean(BacDataService.class);
	
    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String queryName;
    
    private String param;
    
    private String css;

    private String valueField;

    private String textField;

    private String selectValue;

    private String attribute;
    
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getValueField() {
		return valueField;
	}

	public void setValueField(String valueField) {
		this.valueField = valueField;
	}

	public String getTextField() {
		return textField;
	}

	public void setTextField(String textField) {
		this.textField = textField;
	}

	public String getSelectValue() {
		return selectValue;
	}

	public void setSelectValue(String selectValue) {
		this.selectValue = selectValue;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public SelectTag() {
        super();
        this.css = "";
        this.attribute = "";
    }

    @Override
    public int doEndTag() throws JspException {
    	try{
	    	JspWriter writer = this.pageContext.getOut();
	    	writer.write("<select class='"+this.css+"' name='"+this.name+"' id='"+this.id+"' "+this.attribute+">");
	    	writer.write("<option value=''>请选择</option>");
	    	
	    	Map<String,Object> map = new HashMap<String,Object>();
	    	if(!StringUtils.isEmpty(this.param)){
		    	Map<String,String> mapStr = HttpUtils.parseQueryString(this.param);
		    	map.putAll(mapStr);
	    	}
	    	List<DynamicModelClass> list = bacDataService.findItems(this.queryName,map,null,null);
	    	if(list!=null&&list.size()>0){
		    	for(DynamicModelClass obj : list){
		    		if(obj.get(this.valueField).toString().equals(this.selectValue.toString())){//选中
		    			writer.write("<option value='"+obj.get(this.valueField)+"' selected> "+obj.get(this.textField)+" </option>");
		    		}else{
		    			writer.write("<option value='"+obj.get(this.valueField)+"'> "+obj.get(this.textField)+" </option>");
		    		}
		    	}
	    	}
	    	writer.write("</select>");
    	
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new JspException(ex);
        }
        return EVAL_PAGE;
    }
}
