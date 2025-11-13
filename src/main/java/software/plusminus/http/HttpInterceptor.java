package software.plusminus.http;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.plusminus.aspect.AspectContext;
import software.plusminus.context.WritableContext;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class HttpInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    private AspectContext aspectContext;
    private WritableContext<HandlerMethod> handlerMethodContext;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).order(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        if (request.getDispatcherType() == DispatcherType.REQUEST) {
            populateHandlerContext(handler);
            aspectContext.before();
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            return;
        }
        aspectContext.after();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            return;
        }
        if (ex != null) {
            aspectContext.onException(ex);
        }
        aspectContext.onFinally();
    }

    private void populateHandlerContext(Object handler) {
        if (handler instanceof HandlerMethod) {
            handlerMethodContext.set((HandlerMethod) handler);
        }
    }
}
