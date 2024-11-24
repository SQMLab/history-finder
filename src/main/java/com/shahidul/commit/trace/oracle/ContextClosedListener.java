package com.shahidul.commit.trace.oracle;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@AllArgsConstructor
@Slf4j
public class ContextClosedListener implements ApplicationListener<ContextClosedEvent> {
    AppProperty appProperty;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (appProperty.getTraceCacheDirectoryCleanStart()) {
            File file = new File(appProperty.getTraceCacheDirectory());
            log.info("Deleting cache directory : {}", file.getPath());
            if (file.exists()) {
                FileUtils.deleteQuietly(file);
            }
        }
    }
}
