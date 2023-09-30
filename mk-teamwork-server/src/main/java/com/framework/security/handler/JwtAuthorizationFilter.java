package com.framework.security.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.framework.common.ResultCode;
import com.framework.common.ResultJson;
import com.framework.common.constant.Constants;
import com.framework.common.utils.ServletUtils;
import com.framework.common.utils.StringUtils;
import com.framework.security.util.RedisCache;
import com.framework.security.util.TokenUtil;
import com.projectm.login.entity.LoginUser;

import cn.hutool.core.util.StrUtil;

/**
 * @version V1.0
 * @program: teamwork
 * @package: com.framework.security
 * @description: Token解析
 * @author: lzd
 * @create: 2020-06-25 09:52
 **/
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	@Value("${custom.ignored-role-path}")
	private String[] strings;

	@Autowired
	private TokenUtil tokenUtil;

	@Autowired
	private RedisCache redisCache;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String authorization = request.getHeader(Constants.TOKEN);
		String orgCode = request.getHeader(Constants.ORGCODE);
		String url = request.getRequestURI();
		String requestUrl = url.substring(1).toLowerCase();
		List<String> urlList = new ArrayList<>();
		// Arrays.asList(strings);
		urlList.add("/webjars/**");
		urlList.add("/swagger-ui.html");
		urlList.add("/swagger-resources");
		urlList.add("/swagger-ui/");
		urlList.add("/swagger-ui.html");
		urlList.add("/swagger-resources/**");
		urlList.add("/images/**");
		urlList.add("/webjars/**");
		urlList.add("/v2/api-docs");
		urlList.add("/configuration/ui");
		urlList.add("/configuration/security");

		// 对特定的url放行
		if (urlList.contains(url)) {
			chain.doFilter(request, response);
			return;
		}
		// 请求头中有token，则进行解析，并且设置认证信息
		String token = authorization != null ? authorization.replace(Constants.TOKEN_PREFIX, "") : null;
		String userCode = token != null  ?  tokenUtil.getUserCode(token): null;;
		if (StringUtils.isNotEmpty(userCode)) {
			LoginUser loginUser = redisCache.getCacheObject(Constants.LOGIN_USER_KEY + userCode);
			// 鉴权
			AtomicBoolean contains = new AtomicBoolean(false);
			loginUser.getUser().getMemberAccountList().forEach(memberAccount -> {
				if (StrUtil.isNotEmpty(orgCode)) {
					if (Objects.equals(memberAccount.getOrganization_code(), orgCode)) {
						contains.set(memberAccount.getNodeList().contains(requestUrl));
					}
				} else {
					contains.set(memberAccount.getNodeList().contains(requestUrl));
				}

			});
			if (contains.get()) {
				// if (true) {
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginUser,
						null, null);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				chain.doFilter(request, response);
			} else {
				ServletUtils.renderString(response, ResultJson.failure(ResultCode.FORBIDDEN).toString());
			}
		} else {
			chain.doFilter(request, response);

			//ServletUtils.renderString(response, ResultJson.failure(ResultCode.VERIFY_TOKEN_FAIL).toString());
		}
	}
}