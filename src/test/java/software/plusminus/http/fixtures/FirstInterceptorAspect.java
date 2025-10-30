package software.plusminus.http.fixtures;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import software.plusminus.aspect.After;
import software.plusminus.aspect.Before;
import software.plusminus.aspect.ExceptionListener;
import software.plusminus.aspect.Finally;
import software.plusminus.listener.Joinpoint;

@Order(1)
@Component
public class FirstInterceptorAspect implements Before, After, ExceptionListener<IllegalStateException>, Finally {

    @Override
    public Joinpoint joinpoint() {
        return Joinpoint.DEFAULT;
    }

    @Override
    public void before() {
        CallsContext.add("interceptor1 before");
    }

    @Override
    public void after() {
        CallsContext.add("interceptor1 after");
    }

    @Override
    public void onException(IllegalStateException exception) {
        CallsContext.add("interceptor1 exception " + exception.getClass().getSimpleName());
    }

    @Override
    public void onFinally() {
        CallsContext.add("interceptor1 finally");
    }
}
