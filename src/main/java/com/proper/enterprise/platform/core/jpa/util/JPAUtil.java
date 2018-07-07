package com.proper.enterprise.platform.core.jpa.util;

import com.proper.enterprise.platform.core.utils.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JPAUtil {

    private JPAUtil() {

    }

    /**
     * 获得entity列非空的字段名称
     * 无法获取持有的关联对象的空字段名称
     * 仅获得自身的空字段名称
     *
     * @param source 当前对象
     * @return entity列非空的字段名称集合
     */
    public static String[] getNotNullColumnNames(Object source) {
        Assert.notNull(source, "Source must not be null");
        List<String> notNullValueColumns = new ArrayList<>();
        for (Field field : BeanUtil.getAllFields(source.getClass())) {
            if (null != field.getAnnotation(Transient.class)
                || null != field.getAnnotation(ManyToMany.class)
                || null != field.getAnnotation(ManyToOne.class)
                || null != field.getAnnotation(OneToMany.class)
                || null != field.getAnnotation(OneToOne.class)) {
                continue;
            }
            PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(source.getClass(), field.getName());
            if (null == propertyDescriptor) {
                continue;
            }
            Method readMethod = propertyDescriptor.getReadMethod();
            if (null == readMethod) {
                continue;
            }
            try {
                if (null != readMethod.invoke(source)) {
                    notNullValueColumns.add(field.getName());
                }
            } catch (Throwable ex) {
                throw new FatalBeanException(
                    "Could getValue'" + propertyDescriptor.getName() + "' from source", ex);
            }
        }
        return notNullValueColumns.toArray(new String[0]);
    }
}
