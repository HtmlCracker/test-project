package org.example.api.statemachine.config;

import lombok.RequiredArgsConstructor;
import org.example.api.statemachine.state.upload.UploadFileEvent;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.example.api.services.FileStorageService;
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
@EnableStateMachineFactory(name = "uploadStateMachineFactory")
public class UploadStateMachineConfig extends StateMachineConfigurerAdapter<UploadFileState, UploadFileEvent> {
    private final FileStorageService fileStorageService;

    @Override
    public void configure(StateMachineStateConfigurer<UploadFileState, UploadFileEvent> states) throws Exception {
        states
                .withStates()
                .initial(UploadFileState.UPLOADED)
                .end(UploadFileState.STORED)
                .states(EnumSet.allOf(UploadFileState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<UploadFileState, UploadFileEvent> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(UploadFileState.UPLOADED)
                .target(UploadFileState.COMPRESSED)
                .event(UploadFileEvent.COMPRESS)
                .action(compressAction())

                .and()
                .withExternal()
                .source(UploadFileState.COMPRESSED)
                .target(UploadFileState.ENCRYPTED)
                .event(UploadFileEvent.ENCRYPT)
                .action(encryptAction())

                .and()
                .withExternal()
                .source(UploadFileState.ENCRYPTED)
                .target(UploadFileState.STORED)
                .event(UploadFileEvent.STORE)
                .action(storeAction());
    }

    @Bean
    public Action<UploadFileState, UploadFileEvent> compressAction() {
        return context -> {
            String fileId = (String) context.getExtendedState().getVariables().get("fileId");
            fileStorageService.compress(fileId);
        };
    }

    @Bean
    public Action<UploadFileState, UploadFileEvent> encryptAction() {
        return context -> {
            String fileId = (String) context.getExtendedState().getVariables().get("fileId");
            fileStorageService.encrypt(fileId);
        };
    }

    @Bean
    public Action<UploadFileState, UploadFileEvent> storeAction() {
        return context -> {
            String fileId = (String) context.getExtendedState().getVariables().get("fileId");
            fileStorageService.store(fileId);
        };
    }
}
