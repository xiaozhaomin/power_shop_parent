package com.powershop.fallback;

import com.powershop.feign.ContentServiceFeign;
import com.powershop.utils.AdNode;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContentServiceFallback implements FallbackFactory<ContentServiceFeign> {
    @Override
    public ContentServiceFeign create(Throwable throwable) {
        return new ContentServiceFeign() {
            @Override
            public List<AdNode> selectFrontendContentByAD() {
                return null;
            }
        };
    }
}
