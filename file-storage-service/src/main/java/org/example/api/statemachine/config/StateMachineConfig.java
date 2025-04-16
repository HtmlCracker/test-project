package org.example.api.statemachine.config;

import lombok.RequiredArgsConstructor;
import org.example.api.statemachine.enums.FileEvent;
import org.example.api.statemachine.enums.FileState;
import org.example.api.services.UploadProcessor;
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
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<FileState, FileEvent> {
    private final UploadProcessor uploadProcessor;

    @Override
    public void configure(StateMachineStateConfigurer<FileState, FileEvent> states) throws Exception {
        states
                .withStates()
                .initial(FileState.UPLOADED)
                .end(FileState.STORED)
                .states(EnumSet.allOf(FileState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<FileState, FileEvent> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(FileState.UPLOADED)
                .target(FileState.COMPRESSED)
                .event(FileEvent.COMPRESS)
                .action(compressAction())

                .and()
                .withExternal()
                .source(FileState.COMPRESSED)
                .target(FileState.ENCRYPTED)
                .event(FileEvent.ENCRYPT)
                .action(encryptAction())

                .and()
                .withExternal()
                .source(FileState.ENCRYPTED)
                .target(FileState.STORED)
                .event(FileEvent.STORE)
                .action(storeAction());
    }

    @Bean
    public Action<FileState, FileEvent> compressAction() {
        return context -> {
            String fileId = (String) context.getExtendedState().getVariables().get("fileId");
            uploadProcessor.compress(fileId);
        };
    }

    @Bean
    public Action<FileState, FileEvent> encryptAction() {
        return context -> {
            String fileId = (String) context.getExtendedState().getVariables().get("fileId");
            uploadProcessor.encrypt(fileId);
        };
    }

    @Bean
    public Action<FileState, FileEvent> storeAction() {
        return context -> {
            String fileId = (String) context.getExtendedState().getVariables().get("fileId");
            uploadProcessor.store(fileId);
        };
    }
}
