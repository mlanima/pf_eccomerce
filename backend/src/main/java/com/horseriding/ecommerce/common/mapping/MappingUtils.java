package com.horseriding.ecommerce.common.mapping;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class providing common mapping functionality for DTOs and entities.
 */
@Component
public class MappingUtils {

    /**
     * Copy properties from source to target object using Spring BeanUtils
     * @param source Source object
     * @param target Target object
     */
    public void copyProperties(Object source, Object target) {
        if (source != null && target != null) {
            BeanUtils.copyProperties(source, target);
        }
    }

    /**
     * Copy properties from source to target object, ignoring specified properties
     * @param source Source object
     * @param target Target object
     * @param ignoreProperties Properties to ignore during copying
     */
    public void copyProperties(Object source, Object target, String... ignoreProperties) {
        if (source != null && target != null) {
            BeanUtils.copyProperties(source, target, ignoreProperties);
        }
    }

    /**
     * Create a new instance of target class and copy properties from source
     * @param source Source object
     * @param targetClass Target class
     * @param <T> Target type
     * @return New instance of target class with copied properties
     */
    public <T> T mapToClass(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        try {
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            T target = constructor.newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map object to class: " + targetClass.getSimpleName(), e);
        }
    }

    /**
     * Map a list of objects to a list of target class instances
     * @param sourceList Source list
     * @param targetClass Target class
     * @param <S> Source type
     * @param <T> Target type
     * @return List of mapped objects
     */
    public <S, T> List<T> mapToClassList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null) {
            return null;
        }

        return sourceList.stream()
                .map(source -> mapToClass(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * Check if an object has all required non-null fields
     * @param object Object to check
     * @param requiredFields Field names that must be non-null
     * @return true if all required fields are non-null
     */
    public boolean hasRequiredFields(Object object, String... requiredFields) {
        if (object == null) {
            return false;
        }

        try {
            for (String fieldName : requiredFields) {
                Object fieldValue = BeanUtils.getPropertyDescriptor(object.getClass(), fieldName)
                        .getReadMethod()
                        .invoke(object);
                if (fieldValue == null) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}