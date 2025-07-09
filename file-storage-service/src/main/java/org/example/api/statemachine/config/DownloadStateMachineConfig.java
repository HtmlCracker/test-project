package org.example.api.statemachine.config;

import lombok.RequiredArgsConstructor;
import org.example.api.services.FileOperationService;
import org.example.api.statemachine.state.download.DownloadFileEvent;
import org.example.api.statemachine.state.download.DownloadFileState;
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
    private final FileOperationService fileStorageService;

    @Override
    public void configure(StateMachineStateConfigurer<DownloadFileState, DownloadFileEvent> states)
            throws Exception {
        states
                .withStates()
                .initial(DownloadFileState.STORED)
                .end(DownloadFileState.READY)
                .states(EnumSet.allOf(DownloadFileState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<DownloadFileState, DownloadFileEvent> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(DownloadFileState.STORED)
                .target(DownloadFileState.PREPARED)
                .action(prepareAction())

                .and()
                .withExternal()
                .source(DownloadFileState.PREPARED)
                .target(DownloadFileState.DECRYPTED)
                .action(decryptAction())

                .and()
                .withExternal()
                .source(DownloadFileState.DECRYPTED)
                .target(DownloadFileState.DECOMPRESSED)
                .action(decompressAction())

                .and()
                .withExternal()
                .source(DownloadFileState.DECOMPRESSED)
                .target(DownloadFileState.READY)
                .event(DownloadFileEvent.DELIVER)
                .action(deliverAction());
    }

    @Bean
    public Action<DownloadFileState, DownloadFileEvent> prepareAction() {
        return context -> {
            String filePath = (String) context.getExtendedState().getVariables().get("filePath");
            String newPath = fileStorageService.prepare(filePath);
            context.getExtendedState().getVariables().put("filePath", newPath);
        };
    }

    @Bean
    public Action<DownloadFileState, DownloadFileEvent> decryptAction() {
        return context -> {
            String filePath = (String) context.getExtendedState().getVariables().get("filePath");
            String newPath = fileStorageService.decrypt(filePath);
            context.getExtendedState().getVariables().put("filePath", newPath);
        };
    }

    @Bean
    public Action<DownloadFileState, DownloadFileEvent> decompressAction() {
        return context -> {
            String filePath = (String) context.getExtendedState().getVariables().get("filePath");
            String newPath = fileStorageService.decompress(filePath);
            context.getExtendedState().getVariables().put("filePath", newPath);
        };
    }

    @Bean
    public Action<DownloadFileState, DownloadFileEvent> deliverAction() {
        return context -> {
            String filePath = (String) context.getExtendedState().getVariables().get("filePath");
            String newPath = fileStorageService.deliver(filePath);
            System.out.println(newPath + " DELIVERED");
            context.getExtendedState().getVariables().put("filePath", newPath);
        };
    }
}
