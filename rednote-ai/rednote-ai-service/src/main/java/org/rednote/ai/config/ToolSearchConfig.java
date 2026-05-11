package org.rednote.ai.config;

import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor;
import org.springaicommunity.tool.search.ToolSearcher;
import org.springaicommunity.tool.searcher.LuceneToolSearcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolSearchConfig {

    @Bean
    ToolSearcher toolSearcher() {
        return new LuceneToolSearcher();
    }

    @Bean
    ToolSearchToolCallAdvisor toolSearchToolCallAdvisor(ToolSearcher toolSearcher) {
        return ToolSearchToolCallAdvisor.builder()
                .toolSearcher(toolSearcher)
                .build();
    }
}
