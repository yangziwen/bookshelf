package net.yangziwen.bookshelf.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.alibaba.druid.util.PatternMatcher;

public class UserAgentMatcher implements PatternMatcher {
	
	private Map<String, Pattern> patterns = new HashMap<String, Pattern>();

	@Override
	public boolean matches(String pattern, String source) {
		if(StringUtils.isBlank(pattern) || StringUtils.isBlank(source)) {
			return false;
		}
		return getRegexPattern(pattern).matcher(source).matches();
	}
	
	private Pattern getRegexPattern(String pattern) {
		final String matchAll = ".*", matchStart = "^", matchEnd = "$";
		if(!pattern.startsWith(matchAll) && !pattern.startsWith(matchStart)) {
			pattern = matchAll + pattern;
		}
		if(!pattern.endsWith(matchAll) && !pattern.endsWith(matchEnd)) {
			pattern = pattern + matchAll;
		}
		Pattern regexPattern = patterns.get(pattern);
		if(regexPattern == null) {
			regexPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			patterns.put(pattern, regexPattern);
		}
		return regexPattern;
	}
	
}
