package org.example.api.statemachine.config;

import lombok.RequiredArgsConstructor;
import org.example.api.services.FileStorageService;
import org.example.api.statemachine.state.download.DownloadFileEvent;
import org.example.api.statemachine.state.download.DownloadFileState;
import org.example.api.statemachine.state.upload.UploadFileEvent;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@RequiredArgsConstructor
@Configuration
@EnableStateMachineFactory(name = "downloadStateMachineFactory")
public class DownloadStateMachineConfig extends StateMachineConfigurerAdapter<DownloadFileState, DownloadFileEvent> {
    private final FileStorageService fileStorageService;

    @Override
    public void configure(StateMachineStateConfigurer<DownloadFileState, DownloadFileEvent> states) throws Exception {
        states
                .withStates()
                .initial(DownloadFileState.STORED)
                .end(DownloadFileState.DECOMPRESSED)
                .states(EnumSet.allOf(DownloadFileState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<DownloadFileState, DownloadFileEvent> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(DownloadFileState.STORED)
                .target(DownloadFileState.DECRYPTED)
                .event(DownloadFileEvent.DECRYPT)
                .action(decryptAction())

                .and()
                .withExternal()
                .source(DownloadFileState.DECRYPTED)
                .target(DownloadFileState.DECOMPRESSED)
                .event(DownloadFileEvent.DECOMPRESS)
                .action(decompressAction())

                .and()
                .withExternal()
                .source(DownloadFileState.DECOMPRESSED)
                .target(DownloadFileState.READY)
                .event(DownloadFileEvent.DELIVER)
                .action(deliverAction());
    }

    @Bean
    public Action<DownloadFileState, DownloadFileEvent> decryptAction() {
        return context -> {
            String filePath = (String) context.getExtendedState().getVariables().get("filePath");
            fileStorageService.decrypt(filePath);
        };
    }

    @Bean
    public Action<DownloadFileState, DownloadFileEvent> decompressAction() {
        return context -> {
            String filePath = (String) context.getExtendedState().getVariables().get("filePath");
            fileStorageService.compress(filePath);
        };
    }

    @Bean
    public Action<DownloadFileState, DownloadFileEvent> deliverAction() {
        return context -> {
            String filePath = (String) context.getExtendedState().getVariables().get("filePath");
            fileStorageService.encrypt(filePath);
        };
    }
}
