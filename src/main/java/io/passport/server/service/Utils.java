package io.passport.server.service;

import org.springframework.beans.BeanUtils;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

public class Utils {
    public static void copyNonNullProperties(Object source, Object target) throws InvocationTargetException, IllegalAccessException {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(target.getClass());
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            Object value = BeanUtils.getPropertyDescriptor(source.getClass(), propertyDescriptor.getName())
                    .getReadMethod().invoke(source);
            if (value != null) {
                BeanUtils.getPropertyDescriptor(target.getClass(), propertyDescriptor.getName())
                        .getWriteMethod().invoke(target, value);
            }
        }
    }
}
