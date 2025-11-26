package software.plusminus.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import software.plusminus.context.WritableContext;
import software.plusminus.http.fixtures.CallsContext;
import software.plusminus.http.fixtures.FirstFilterAspect;
import software.plusminus.http.fixtures.FirstInterceptorAspect;
import software.plusminus.http.fixtures.SecondFilterAspect;
import software.plusminus.http.fixtures.SecondInterceptorAspect;
import software.plusminus.test.IntegrationTest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class HttpFilterTest extends IntegrationTest {

    @LocalServerPort
    private int port;

    @SpyBean
    private FirstFilterAspect firstFilterAspect;
    @SpyBean
    private SecondFilterAspect secondFilterAspect;
    @SpyBean
    private FirstInterceptorAspect firstInterceptorAspect;
    @SpyBean
    private SecondInterceptorAspect secondInterceptorAspect;
    @SpyBean
    private WritableContext<HttpServletRequest> requestContext;
    @SpyBean
    private WritableContext<HttpServletResponse> responseContext;
    @SpyBean
    private WritableContext<HandlerMethod> handlerMethodContext;

    @Autowired
    private TestRestTemplate restTemplate;

    @Override
    @AfterEach
    public void afterEach() {
        super.afterEach();
        CallsContext.clear();
    }

    @Test
    void writableContext() {
        String url = "http://localhost:" + port + "/ok";

        String response = restTemplate.getForObject(url, String.class);

        assertThat(response).isEqualTo("ok");
        verify(requestContext).set(any(HttpServletRequest.class));
        verify(responseContext).set(any(HttpServletResponse.class));
        verify(handlerMethodContext).set(any(HandlerMethod.class));
    }

    @Test
    void ok() {
        String url = "http://localhost:" + port + "/ok";
        InOrder inOrder = inOrder(firstFilterAspect, secondFilterAspect,
                firstInterceptorAspect, secondInterceptorAspect);

        String response = restTemplate.getForObject(url, String.class);

        assertThat(response).isEqualTo("ok");
        inOrder.verify(firstFilterAspect).before();
        inOrder.verify(secondFilterAspect).before();
        inOrder.verify(firstFilterAspect).around(any());
        inOrder.verify(secondFilterAspect).around(any());
        inOrder.verify(firstInterceptorAspect).before();
        inOrder.verify(secondInterceptorAspect).before();
        inOrder.verify(firstInterceptorAspect).after();
        inOrder.verify(secondInterceptorAspect).after();
        inOrder.verify(firstInterceptorAspect, never()).onException(any());
        inOrder.verify(secondInterceptorAspect, never()).onException(any());
        inOrder.verify(firstInterceptorAspect).onFinally();
        inOrder.verify(secondInterceptorAspect).onFinally();
        inOrder.verify(firstFilterAspect, never()).onException(any());
        inOrder.verify(secondFilterAspect, never()).onException(any());
        inOrder.verify(firstFilterAspect).after();
        inOrder.verify(secondFilterAspect).after();
        inOrder.verify(firstFilterAspect).onFinally();
        inOrder.verify(secondFilterAspect).onFinally();
        CallsContext.check("filter1 before", "filter2 before",
                "filter1 aroundStart", "filter2 aroundStart",
                "interceptor1 before", "interceptor2 before",
                "interceptor1 after", "interceptor2 after",
                "interceptor1 finally", "interceptor2 finally",
                "filter2 aroundEnd", "filter1 aroundEnd",
                "filter1 after", "filter2 after",
                "filter1 finally", "filter2 finally");
    }

    @Test
    void exception() {
        String url = "http://localhost:" + port + "/exception";
        InOrder inOrder = inOrder(firstFilterAspect, secondFilterAspect,
                firstInterceptorAspect, secondInterceptorAspect);

        ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        inOrder.verify(firstFilterAspect).before();
        inOrder.verify(secondFilterAspect).before();
        inOrder.verify(firstFilterAspect).around(any());
        inOrder.verify(secondFilterAspect).around(any());
        inOrder.verify(firstInterceptorAspect).before();
        inOrder.verify(secondInterceptorAspect).before();
        inOrder.verify(firstInterceptorAspect, never()).after();
        inOrder.verify(secondInterceptorAspect, never()).after();
        inOrder.verify(firstInterceptorAspect).onException(any());
        inOrder.verify(secondInterceptorAspect).onException(any());
        inOrder.verify(firstInterceptorAspect).onFinally();
        inOrder.verify(secondInterceptorAspect).onFinally();
        inOrder.verify(firstFilterAspect).onException(any());
        inOrder.verify(secondFilterAspect).onException(any());
        inOrder.verify(firstFilterAspect, never()).after();
        inOrder.verify(secondFilterAspect, never()).after();
        inOrder.verify(firstFilterAspect).onFinally();
        inOrder.verify(secondFilterAspect).onFinally();

        CallsContext.check("filter1 before", "filter2 before",
                "filter1 aroundStart", "filter2 aroundStart",
                "interceptor1 before", "interceptor2 before",
                "interceptor1 exception IllegalStateException", "interceptor2 exception IllegalStateException",
                "interceptor1 finally", "interceptor2 finally",
                "filter1 exception IllegalStateException", "filter2 exception IllegalStateException",
                "filter1 finally", "filter2 finally");
    }
}