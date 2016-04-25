package com.harvey.common.core.beetl;

import org.beetl.core.Tag;
import org.beetl.core.TagFactory;

public class BeetlTagFactory implements TagFactory {

    private Tag tag;
    private String name;
    
    public BeetlTagFactory(BeetlTag tag){
        this(tag, tag.getName());
    }
    
    public BeetlTagFactory(Tag tag,String name){
        this.tag = tag;
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
    
    @Override
    public Tag createTag() {
        return this.tag;
    }

}
