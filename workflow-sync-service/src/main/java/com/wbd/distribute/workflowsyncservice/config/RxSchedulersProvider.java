package com.wbd.distribute.workflowsyncservice.config;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.Executors;

@Configuration
public class RxSchedulersProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RxSchedulersProvider.class);

    @Inject
    RemoteServicesConfig remoteServicesConfig;

    @Inject
    BeanFactory beanFactory;

    int schedulerMaxThreadsChanged = 1;
    int httpClientSchedulerCurrent = 1;

    @PostConstruct
    void init() {

        int httpClientSchedulerCurrentTmp = remoteServicesConfig.getSchedulerMaxThreads();

        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();
        final Set<ConstraintViolation<RemoteServicesConfig>> constraintViolations =
                validator.validate(remoteServicesConfig, PerformanceConfigGroup.class);

        final StringBuilder msgs = new StringBuilder();

        if (!constraintViolations.isEmpty()) {

            msgs.append("Config validation errors: ");

            constraintViolations.forEach(constraint ->
                    msgs.append("'" + constraint.getPropertyPath().toString() + "' - " + constraint.getMessage())
            );

            LOGGER.error("Error {}", msgs);

        } else {
            LOGGER.warn("requires application restart!");
            httpClientSchedulerCurrent = httpClientSchedulerCurrentTmp;
        }

        LOGGER.info("Setting {} httpClientSchedulerCurrent to {}", remoteServicesConfig.getClass().getName(),
                httpClientSchedulerCurrent);
    }


    @Bean("HttpClientScheduler")
    public Scheduler httpClientScheduler() {

        return Schedulers.from(Executors.newWorkStealingPool(httpClientSchedulerCurrent));
    }

}