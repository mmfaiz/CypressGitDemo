/* Copyright 2012 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.cache.web.filter.redis;

import grails.plugin.cache.SerializableByteArrayOutputStream;
import grails.plugin.cache.web.GenericResponseWrapper;
import grails.plugin.cache.web.PageInfo;
import grails.plugin.cache.web.filter.PageFragmentCachingFilter;
import org.codehaus.groovy.grails.web.servlet.WrappedResponseHolder;
import org.codehaus.groovy.grails.web.util.WebUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis-based implementation of PageFragmentCachingFilter.
 *
 * @author Burt Beckwith
 */
public class RedisPageFragmentCachingFilter extends PageFragmentCachingFilter {

    @Override
    protected int getTimeToLive(ValueWrapper wrapper) {
        // ttl not supported
        return Integer.MAX_VALUE;
    }

    @Override
    protected RedisCacheManager getNativeCacheManager() {
        return (RedisCacheManager) super.getNativeCacheManager();
    }

    @Override
    protected void put(Cache cache, String key, PageInfo pageInfo, Integer timeToLiveSeconds) {
        // just store, ttl not supported
        cache.put(key, pageInfo);
    }

    @Override
    protected PageInfo buildPage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Invoke the next entity in the chain
        SerializableByteArrayOutputStream out = new SerializableByteArrayOutputStream();
        GenericResponseWrapper wrapper = new GenericResponseWrapper(response, out);
        Map<String, Serializable> cacheableRequestAttributes = new HashMap<String, Serializable>();

        // TODO: split the special include handling out into a separate method
        HttpServletResponse originalResponse = null;
        boolean isInclude = WebUtils.isIncludeRequest(request);
        if (isInclude) {
            originalResponse = WrappedResponseHolder.getWrappedResponse();
            WrappedResponseHolder.setWrappedResponse(wrapper);
        }
        try {
            List<String> attributesBefore = toList(request.getAttributeNames());
            chain.doFilter(request, wrapper);
            List<String> attributesAfter = toList(request.getAttributeNames());
            attributesAfter.removeAll(attributesBefore);
            for (String attrName : attributesAfter) {
				// Hack to remove attributes that will break in serialization
				if (!"org.codehaus.groovy.grails.REQUEST_FORMATS".equals(attrName)) {
					Object value = request.getAttribute(attrName);
					if (value instanceof Serializable) {
						cacheableRequestAttributes.put(attrName, (Serializable) value);
					}
				}
			}
        } finally {
            if (isInclude) {
                WrappedResponseHolder.setWrappedResponse(originalResponse);
            }
        }
        wrapper.flush();

        long timeToLiveSeconds = Integer.MAX_VALUE; // TODO cacheManager.getEhcache(context.cacheName).cacheConfiguration.timeToLiveSeconds;

        String contentType = wrapper.getContentType();
        if (!StringUtils.hasLength(contentType)) {
            contentType = response.getContentType();
        }

        return new PageInfo(wrapper.getStatus(), contentType, out.toByteArray(),
                false, timeToLiveSeconds, wrapper.getAllHeaders(), wrapper.getCookies(), cacheableRequestAttributes);
    }
}

