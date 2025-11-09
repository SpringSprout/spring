package SpringSprout;

import SpringSprout.annotation.Autowired;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IllegalAccessException {
        BeanFactory beanFactory = new BeanFactory();
        for(Object bean: beanFactory.getBeans().values()){
            Field[] fields = bean.getClass().getDeclaredFields();
            List<Field> autowiredFields = findAutowiredField(fields);
            for(Field field: autowiredFields){
                Object toInjectBean = beanFactory.getBeans().get(field.getName());
                field.setAccessible(true);
                field.set(bean, toInjectBean);
            }
        }
    }

    private static List<Field> findAutowiredField(Field[] fields){
        List<Field> autowiredFields = new ArrayList<>();
        for(Field field: fields){
            if(!field.isAnnotationPresent(Autowired.class)){
                continue;
            }
            autowiredFields.add(field);
        }
        return autowiredFields;
    }

}