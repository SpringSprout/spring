package com.spring.sprout.context;

import com.spring.sprout.annotation.Autowired;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SpringApplication {

    private static BeanFactory beanFactory;

    public static void run() {
        beanFactory = new BeanFactory();
        refresh();
    }

    private static void injectDependency() throws Exception {
        injectByConstructor();
        injectByAutowired();
    }

    private static void injectByConstructor() throws Exception {
        for (Object bean : beanFactory.getBeans().values()) {
            Class<?> clazz = bean.getClass();
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> targetConstructor = null;

            // 주입할 생성자 가져오기
            for (Constructor constructor : constructors) {
                if (constructor.isAnnotationPresent(Autowired.class)) {
                    if (targetConstructor != null) {
                        throw new RuntimeException("Autowired 생성자는 여러개가 존재할 수 없습니다");
                    }
                    targetConstructor = constructor;
                }
            }

            if (targetConstructor == null) {
                throw new RuntimeException("다수의 생성자가 존재할 경우에는 하나에 @Autowired를 붙여야합니다");
            }

            // 일단 의존성 주입에 사용할 생성자는 확정됨, 그 후 동작
            Class<?>[] parameterTypes = targetConstructor.getParameterTypes();

//           @Autowired
//            public NormalJoinServiceImpl(UserRepository userRepository,
//                BCryptPasswordEncoder bCryptPasswordEncoder) {
//                this.userRepository = userRepository;
//                this.bCryptPasswordEncoder = bCryptPasswordEncoder;
//            }

            Object[] args = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                Object dependency = beanFactory.getBeans().get(parameterType.getName());
                args[i] = dependency;
            }

            beanFactory.getBeans()
                .put(clazz.getName(), targetConstructor.newInstance(args));
        }
    }

    private static void injectByAutowired() throws IllegalAccessException {
        for (Object bean : beanFactory.getBeans().values()) {
            Field[] fields = bean.getClass().getDeclaredFields();
            List<Field> autowiredFields = new ArrayList<>();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }
                autowiredFields.add(field);
            }
            for (Field field : autowiredFields) {
                Object toInjectBean = beanFactory.getBeans().get(field.getType().getName());
                field.setAccessible(true);
                field.set(bean, toInjectBean);
            }
        }
    }

    private static void refresh() {
        try {
            injectDependency();
        } catch (Exception e) {
            throw new RuntimeException("Springboot load failed");
        }
    }
}
