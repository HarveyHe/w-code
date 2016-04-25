package com.harvey.common.core.beetl;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.ResourceLoader;

import com.harvey.common.core.context.Context;

public class BeetlTemplate extends GroupTemplate {

    public BeetlTemplate() {
        super();
    }

    public BeetlTemplate(Configuration conf) {
        super(conf);
    }

    public BeetlTemplate(ResourceLoader loader, Configuration conf) {
        super(loader, conf);
    }

    @Override
    protected void init() {
        Collection<TemplateInitializing> values = getInitializings();
        for (TemplateInitializing v : values) {
            v.onInitial(this);
        }        
        super.init();
    }

    private static Collection<TemplateInitializing> getInitializings() {
        if (Context.getContext() != null) {
            Collection<TemplateInitializing> values = Context.getContext().getBeansOfType(TemplateInitializing.class).values();
            if (values.size() == 0) {
                DefaultInitializing defInit = new DefaultInitializing();
                defInit.setApplicationContext(Context.getContext());
                values = Arrays.asList((TemplateInitializing) defInit);
            }
            return values;
        }
        return Collections.emptyList();
    }
}
