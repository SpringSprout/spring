package SpringSprout.ioc;

import java.util.HashMap;
import lombok.Getter;

@Getter
public class BeanFactory {

    HashMap<Object, Object> beans = new HashMap<>();
}
