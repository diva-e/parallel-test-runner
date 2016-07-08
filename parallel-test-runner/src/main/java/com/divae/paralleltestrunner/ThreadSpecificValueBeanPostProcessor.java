package com.divae.paralleltestrunner;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.ReflectionUtils;

public class ThreadSpecificValueBeanPostProcessor implements BeanPostProcessor, InitializingBean, BeanFactoryAware {

	private final PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer;
	private final ParallelTestInfo testInfo;

	private ConfigurablePropertyResolver propertyResolver;
	private TypeConverter converter;

	@Autowired
	public ThreadSpecificValueBeanPostProcessor(
			PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer, ParallelTestInfo testInfo) {
		this.propertySourcesPlaceholderConfigurer = propertySourcesPlaceholderConfigurer;
		this.testInfo = testInfo;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		for (Class<?> beanClass = bean.getClass(); beanClass != Object.class; beanClass = beanClass.getSuperclass()) {
			for (Field field : beanClass.getDeclaredFields()) {
				ThreadSpecificValue runValue = field.getAnnotation(ThreadSpecificValue.class);
				if (null != runValue) {
					field.setAccessible(true);

					String resolvedValue = propertyResolver.resolveRequiredPlaceholders(runValue.value());
					Object convertedValue = converter.convertIfNecessary(resolvedValue, field.getType(), field);
					ReflectionUtils.setField(field, bean, convertedValue);
				}
			}
		}

		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		propertyResolver = new PropertySourcesPropertyResolverExtension(
				propertySourcesPlaceholderConfigurer.getAppliedPropertySources());
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		converter = ((ConfigurableBeanFactory) beanFactory).getTypeConverter();
	}

	private final class PropertySourcesPropertyResolverExtension extends PropertySourcesPropertyResolver {
		private PropertySourcesPropertyResolverExtension(PropertySources propertySources) {
			super(propertySources);
		}

		@Override
		public boolean containsProperty(String key) {
			String threadSpecificKey = threadSpecificKey(key);
			if (super.containsProperty(threadSpecificKey)) {
				return true;
			}
			return super.containsProperty(key);
		}

		@Override
		public <T> Class<T> getPropertyAsClass(String key, Class<T> targetType) {
			String threadSpecificKey = threadSpecificKey(key);
			if (super.containsProperty(threadSpecificKey)) {
				return super.getPropertyAsClass(threadSpecificKey, targetType);
			}
			return super.getPropertyAsClass(key, targetType);
		}

		@Override
		protected <T> T getProperty(String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {
			String threadSpecificKey = threadSpecificKey(key);
			if (super.containsProperty(threadSpecificKey)) {
				return super.getProperty(threadSpecificKey, targetValueType, resolveNestedPlaceholders);
			}
			return super.getProperty(key, targetValueType, resolveNestedPlaceholders);
		}

		private String threadSpecificKey(String key) {
			return key + ".thread." + testInfo.getCurrentThread();
		}
	}
}
