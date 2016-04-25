package com.harvey.common.core.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

public class BeanNameGenerator extends AnnotationBeanNameGenerator {
	
	public String generateBeanName(BeanDefinition definition,
			BeanDefinitionRegistry registry) {
		String beanName = super.generateBeanName(definition, registry);
		if (beanName.endsWith("Impl")) {
			beanName = beanName.substring(0, beanName.length() - 4);
		}
		return beanName;
	}

}
