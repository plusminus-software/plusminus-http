package software.plusminus.http;

import org.springframework.stereotype.Component;
import org.springframework.web.util.NestedServletException;
import software.plusminus.aspect.ExceptionExtractor;

import java.util.Optional;

@Component
public class NestedServletExceptionExtractor implements ExceptionExtractor<NestedServletException, Exception> {

    @Override
    public Optional<? extends Exception> extract(NestedServletException originalException) {
        Throwable cause = originalException.getCause();
        if (cause instanceof Exception) {
            return Optional.of((Exception) cause);
        } else {
            return Optional.empty();
        }
    }
}
