package org.example.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class BeanFactory {
    private final Set<Class<?>> preInstantiatedClazz;
    private Map<Class<?>, Object> beans = new HashMap<>();

    public BeanFactory(Set<Class<?>> preInstantiatedClazz) throws Exception {
        this.preInstantiatedClazz = preInstantiatedClazz;
        initialize();
    }

    private void initialize() {
        for (Class<?> clazz : this.preInstantiatedClazz) {
            Object instance = createInstance(clazz);
            beans.put(clazz, instance);                              // 8. newInstance UserService() put // 10. newInstance UserController(UserService) put
        }
    }

    private Object createInstance(Class<?> clazz) {                  // 1. UserController in // 5-1. UserService in
        // 생성자
        Constructor<?> constructor = findConstructor(clazz);         // 2. UserController constructor find // 5-2. UserService constructor

        // 파라미터
        List<Object> parameters = new ArrayList<>();
        for (Class<?> typeClass : constructor.getParameterTypes()) { // 3-1. UserController constructor 파라미터는 UserService 타입 한개 // 6-1. UserService constructor 파라미터 없음 // 9-1. UserController 루프 아웃
            parameters.add(getParametersByClass(typeClass));         // 3-2. getParametersByClass(UserService)
        }

        // 인스턴스 생성
        try {
            return constructor.newInstance(parameters.toArray());              // 7. constructor = UserService, parameters = 없음 // 9-2. constructor = UserController, parameters = UserService
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Constructor<?> findConstructor(Class<?> clazz) {
        Constructor<?> constructor = BeanFactoryUtils.getInjectedConstructor(clazz);

        if (Objects.nonNull(constructor)) {
            return constructor;
        }

        return clazz.getConstructors()[0];
    }

    private Object getParametersByClass(Class<?> typeClass) {       // 4-1. UserService in
        Object bean = getBean(typeClass);

        if (Objects.nonNull(bean)) {                                // 4-2. UserService 가 등록된 빈이 아님
            return bean;
        }

        return createInstance(typeClass);                           // 4-3. UserService 를 인스턴스로 생성후 리턴
    }

    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }
}
