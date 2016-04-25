package com.harvey.common.core.hibernate;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.transform.ResultTransformer;

import com.harvey.common.core.dao.utils.EntityUtils;
import com.harvey.common.core.model.DynamicModelClass;

public class DynamicModelClassResultTransformer implements ResultTransformer {

    private static final ResultTransformer instance = new DynamicModelClassResultTransformer();

    public static ResultTransformer getInstance() {
        return instance;
    }

    @Override
    public List transformList(List paramList) {
        return paramList;
    }

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        DynamicModelClass result = new DynamicModelClass();
        for (int i = 0; i < aliases.length; i++) {
            String aliase = aliases[i];
            if ("UUID__".equals(aliase)) {
                if (ArrayUtils.contains(aliases, "uuid")) {
                    aliase = "uuid__";
                } else {
                    aliase = "uuid";
                }
            } else {
                aliase = EntityUtils.toPascalCase(aliase, false);
            }
            result.put(aliase, tuple[i]);
        }
        return result;
    }

}
