package com.wbd.distribute.workflowsyncservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WsConfig {


    @Bean("applicationJsonRestTemplate")
    @Scope("prototype")
    public RestTemplate applicationJsonRestTemplate() {

        return restTemplateByMediaType(MediaType.APPLICATION_JSON);
    }

    @Bean("applicationXmlRestTemplate")
    @Scope("prototype")
    public RestTemplate applicationXmlRestTemplate() {

        return restTemplateByMediaType(MediaType.APPLICATION_XML);
    }

    RestTemplate restTemplateByMediaType(final MediaType mediaType) {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        final RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(requestFactory));

        final List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(mediaType);

        final StringHttpMessageConverter stringHttpMessageConverter =
                new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);

        final MappingJackson2HttpMessageConverter jackson2HttpMessageConverter =
                new MappingJackson2HttpMessageConverter();
        jackson2HttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);

        final List<HttpMessageConverter<?>> messageConverters = Arrays.asList(
                stringHttpMessageConverter,
                jackson2HttpMessageConverter);

        restTemplate.setMessageConverters(messageConverters);

        return restTemplate;
    }
}