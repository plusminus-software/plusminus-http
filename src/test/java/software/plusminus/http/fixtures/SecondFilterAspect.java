package software.plusminus.http.fixtures;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import software.plusminus.aspect.After;
import software.plusminus.aspect.Around;
import software.plusminus.aspect.Before;
import software.plusminus.aspect.ExceptionListener;
import software.plusminus.aspect.Finally;
import software.plusminus.aspect.ThrowingRunnable;
import software.plusminus.http.HttpFilter;
import software.plusminus.listener.Joinpoint;

@Order(2)
@Component
public class SecondFilterAspect implements Before, Around, After, ExceptionListener<IllegalStateException>, Finally {

    @Override
    public Joinpoint joinpoint() {
        return HttpFilter.JOINPOINT;
    }

    @Override
    public void before() {
        CallsContext.add("filter2 before");
    }

    @Override
    public void around(ThrowingRunnable runnable) {
        CallsContext.add("filter2 aroundStart");
        runnable.asRunnable().run();
        CallsContext.add("filter2 aroundEnd");
    }

    @Override
    public void after() {
        CallsContext.add("filter2 after");
    }

    @Override
    public void onException(IllegalStateException exception) {
        CallsContext.add("filter2 exception " + exception.getClass().getSimpleName());
    }

    @Override
    public void onFinally() {
        CallsContext.add("filter2 finally");
    }
}
