package dev.sanskar.fileboi.utilities;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;

import java.net.URL;

public class GlideUrlWithCustomCacheKey extends GlideUrl {

    private String cacheKey;

    public GlideUrlWithCustomCacheKey(URL url) {
        super(url);
    }

    public GlideUrlWithCustomCacheKey(String url) {
        super(url);
    }

    public GlideUrlWithCustomCacheKey(URL url, Headers headers) {
        super(url, headers);
    }

    public GlideUrlWithCustomCacheKey(String url, Headers headers) {
        super(url, headers);
    }

    public GlideUrlWithCustomCacheKey(String url, String cacheKey) {
        super(url);
        this.cacheKey = cacheKey;
    }

    @Override
    public String getCacheKey() {
       if (cacheKey != null) {
           return cacheKey;
       }
       return toStringUrl();
    }
}
