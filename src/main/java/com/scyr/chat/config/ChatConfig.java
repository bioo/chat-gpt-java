package com.scyr.chat.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@ConfigurationProperties(prefix = "open-ai")
@Component
@Data
public class ChatConfig {

    @Value("${run.env.active:}")
    private String runENV;

    private final Set<String> tokens = new HashSet<>();

    private Integer timeoutSeconds;

    private ChatGPTConfig chatGPT;

    private final String invalidKey = "sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    @PostConstruct
    public void init() throws IOException {
        try {
            log.info("runENV is: {}", runENV);
            File configFile;
            File currentPathFile = new File("api-key.conf");
            File configPathFile = new File("conf/api-key.conf");
            if (!currentPathFile.exists() || currentPathFile.isDirectory()) {
                throw new FileNotFoundException("default config api-key.conf is not found");
            }
            if (StringUtils.equalsIgnoreCase(runENV, "docker")) {
                if (!configPathFile.exists() || configPathFile.isDirectory()) {
                    log.info("conf/api-key.conf not found, start copying default api-key.conf to conf/api-key.conf");
                    FileUtil.copyFile(currentPathFile, new File("conf/api-key.conf"));
                }
                configFile = configPathFile;
            } else {
                configFile = currentPathFile;
            }
            List<String> list = FileUtil.readLines(configFile, "UTF-8");
            list.forEach(line -> {
                if (StringUtils.isNotBlank(line)) {
                    line = StringUtils.trim(line);
                    if (StringUtils.length(line) == 51 && StringUtils.startsWith(line, "sk-")) {
                        tokens.add(line);
                    }
                }
            });
            log.info("api-key.conf effective key has : {}", tokens);
            if (ObjectUtils.isEmpty(tokens)) {
                log.error("config api-key.conf no legal apiKey, add an invalid key to make the program run");
                FileUtil.writeString(invalidKey, currentPathFile, CharsetUtil.CHARSET_UTF_8);
                if (StringUtils.equalsIgnoreCase(runENV, "docker")) {
                    FileUtil.writeString(invalidKey, configPathFile, CharsetUtil.CHARSET_UTF_8);
                }
                tokens.add(invalidKey);
            }
        } catch (Exception e) {
            log.error(" init chat config error : ", e);
            throw e;
        }
    }

}
