package net.yangziwen.bookshelf.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.yangziwen.bookshelf.util.UserAgentMatcher;

import org.apache.commons.lang.StringUtils;

import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.util.PatternMatcher;

public class ExclusionCustomizedWebStatFilter extends WebStatFilter {
	
	public static final String PARAM_NAME_REQUEST_URI_EXCLUSIONS = "requestURIExclusions";
	public static final String PARAM_NAME_USER_AGENT_EXCLUSIONS = "userAgentExclusions";
	
	protected PatternMatcher userAgentMatcher = new UserAgentMatcher();
	
	private Set<String> requestURIExcludesPattern;
	private Set<String> userAgentExcludesPattern;
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		
		String userAgentExclusions = config.getInitParameter(PARAM_NAME_USER_AGENT_EXCLUSIONS);
		if(StringUtils.isNotBlank(userAgentExclusions)) {
			userAgentExcludesPattern = new HashSet<String>(Arrays.asList(userAgentExclusions.split("\\s*,\\s*")));
		}
		
		String requestURIExclusions = config.getInitParameter(PARAM_NAME_REQUEST_URI_EXCLUSIONS);
		if(StringUtils.isNotBlank(requestURIExclusions)) {
			requestURIExcludesPattern = new HashSet<String>(Arrays.asList(requestURIExclusions.split("\\s*,\\s*")));
		}
		
		super.init(config);
		
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(isExclusion((HttpServletRequest) request)) {
			chain.doFilter(request, response);
			return;
		}
		super.doFilter(request, response, chain);
	}
	
	public boolean isExclusion(HttpServletRequest httpRequest) {
		if(isRequestURIExcluded(httpRequest.getRequestURI())) {
			return true;
		}
		if(isUserAgentExcluded(httpRequest.getHeader("User-Agent"))) {
			return true;
		}
		return false;
	}
	
	public boolean isRequestURIExcluded(String requestURI) {
		if(requestURIExcludesPattern == null) {
			return super.isExclusion(requestURI);
		}
		
		if (contextPath != null && requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
            if (!requestURI.startsWith("/")) {
                requestURI = "/" + requestURI;
            }
        }
        for (String pattern : requestURIExcludesPattern) {
            if (pathMatcher.matches(pattern, requestURI)) {
                return true;
            }
        }
        return false;
	}
	
	public boolean isUserAgentExcluded(String userAgent) {
		if(StringUtils.isEmpty(userAgent)) {
			return true;
		}
		for(String excludePattern: userAgentExcludesPattern) {
			if(userAgentMatcher.matches(excludePattern, userAgent)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	@Deprecated
	public boolean isExclusion(String requestURI) {
		return false;
	}
}
