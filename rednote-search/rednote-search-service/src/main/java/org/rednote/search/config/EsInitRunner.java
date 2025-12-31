package org.rednote.search.config;

import lombok.RequiredArgsConstructor;
import org.rednote.search.service.IEsSyncService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 服务启动时，初始化 ES
 */
@Component
@RequiredArgsConstructor
public class EsInitRunner implements CommandLineRunner {

    private final IEsSyncService esSyncService;

    @Override
    public void run(String... args) {
        esSyncService.fullSyncNotesToEs();
    }
}
