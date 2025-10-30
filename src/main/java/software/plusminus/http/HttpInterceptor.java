package software.plusminus.http;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import software.plusminus.aspect.AspectContext;
import software.plusminus.context.WritableContext;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class HttpInterceptor extends HandlerInterceptorAdapter implements WebMvcConfigurer {

    private AspectContext aspectContext;
    private WritableContext<HandlerMethod> handlerMethodContext;
    private WritableContext<ResourceHttpRequestHandler> resourceHttpRequestHandlerContext;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
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
        } else if (handler instanceof ResourceHttpRequestHandler) {
            resourceHttpRequestHandlerContext.set((ResourceHttpRequestHandler) handler);
        } else {
            throw new IllegalArgumentException("Unknown handler type " + handler.getClass());
        }
    }
}
