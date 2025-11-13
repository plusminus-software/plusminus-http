package software.plusminus.http;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import software.plusminus.aspect.AspectContext;
import software.plusminus.context.Context;
import software.plusminus.context.WritableContext;
import software.plusminus.listener.Joinpoint;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(AspectContext.class)
public class HttpFilter extends OncePerRequestFilter {

    @SuppressWarnings("java:S2440")
    public static final Joinpoint JOINPOINT = new Joinpoint() { };

    private AspectContext aspectContext;
    private WritableContext<HttpServletRequest> httpServletRequestContext;
    private WritableContext<HttpServletResponse> httpServletResponseContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) {
        try {
            Context.init();
            httpServletRequestContext.set(request);
            httpServletResponseContext.set(response);
            aspectContext.run(
                    () -> filterChain.doFilter(httpServletRequestContext.get(), httpServletResponseContext.get()),
                    HttpFilter.JOINPOINT,
                    Joinpoint.DEFAULT
            );
        } finally {
            Context.clear();
        }
    }
}
