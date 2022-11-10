package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.IpUtil;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 在服务网关添加fillter
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {
    @Resource
    private RedisTemplate redisTemplate;
    // 匹配路径的工具类
    AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Value("${authUrls.url}")
    private String authUrls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取到请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 获取Url
        String path = request.getURI().getPath();
        // 如果是内部接口，则网关拦截不允许外部访问！
        if (antPathMatcher.match("/**/inner/**", path)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
        // 获取用户Id
        String userId = getUserId(request);

        //如果token被盗用
        if ("-1".equals(userId)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
        // 用户登录认证
        //api接口，异步请求，校验用户必须登录
        if (antPathMatcher.match("/api/**/auth/**", path)) {
            if (StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }
        // 验证url
        for (String authUrl : authUrls.split(",")) {
            // 当前的url包含登录的控制器域名，但是用户Id 为空！
            if (path.indexOf(authUrl) != -1 && StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                //303状态码表示由于请求对应的资源存在着另一个URI，应使用重定向获取请求的资源
                response.setStatusCode(HttpStatus.SEE_OTHER);
                response.getHeaders().set(HttpHeaders.LOCATION,
                        "http://www.gmall.com/login.html?originUrl=" + request.getURI());
            }
        }

        // 将userId 传递给后端,将userTempId 添加header请求头
        String userTempId = getUserTempId(request);
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)) {
            if (!StringUtils.isEmpty(userId)) {
                request.mutate().header("userId", userId).build();
            }
            if (!StringUtils.isEmpty(userTempId)) {
                request.mutate().header("userTempId", userTempId).build();
            }
            // 将现在的request 变成 exchange对象
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);
    }

    /**
     * 获取当前登录用户id
     *
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if (tokenList != null) {
            token = tokenList.get(0);
        } else {
            MultiValueMap<String, HttpCookie> cookieMultiValueMap = request.getCookies();
            HttpCookie cookie = cookieMultiValueMap.getFirst("token");
            if (cookie != null) {
                token = URLDecoder.decode(cookie.getValue());
            }
        }
        if (!StringUtils.isEmpty(token)) {
            String userStr = (String) redisTemplate.opsForValue().get("user:login:" + token);
            JSONObject userJson = JSONObject.parseObject(userStr);
            String ip = userJson.getString("ip");

            String curIp = IpUtil.getGatwayIpAddress(request);
            //校验token是否被盗用
            if (ip.equals(curIp)) {
                return userJson.getString("userId");
            } else {
                //ip不一致
                return "-1";

            }
        }
        return "";
    }

    // 接口鉴权失败返回数据( 输入信息out 方法)
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
// 返回用户没有权限登录
        Result<Object> result = Result.build(null, resultCodeEnum);

        byte[] bytes = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);

        DataBuffer wrap = response.bufferFactory().wrap(bytes);

        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(wrap));
    }

    /**
     * 获取当前用户临时用户id
     *
     * @param request
     * @return
     */
    public String getUserTempId(ServerHttpRequest request) {
        String userTempId = "";
        List<String> tokenList = request.getHeaders().get("userTempId");
        if (tokenList != null) {
            userTempId = tokenList.get(0);
        } else {
            MultiValueMap<String, HttpCookie> cookieMultiValueMap = request.getCookies();
            HttpCookie cookie = cookieMultiValueMap.getFirst("userTempId");
            if (cookie != null) {
                userTempId = URLDecoder.decode(cookie.getValue());
            }
        }
        return userTempId;
    }
}


