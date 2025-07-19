package com.example.chatservice.config;

import com.example.chatservice.state.MessageEvent;
import com.example.chatservice.state.MessageState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachine
public class MessageStateMachineConfig extends StateMachineConfigurerAdapter<MessageState, MessageEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<MessageState, MessageEvent> states) throws Exception {
        states.withStates()
                .initial(MessageState.SENT)
                .state(MessageState.DELIVERED)
                .end(MessageState.READ)
                .end(MessageState.EDITED)
                .end(MessageState.FAILED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<MessageState, MessageEvent> transitions) throws Exception {
        transitions
                // SENT -> DELIVERED: сообщение доставлено через WebSocket
                .withExternal()
                    .source(MessageState.SENT).target(MessageState.DELIVERED)
                    .event(MessageEvent.MESSAGE_DELIVERED)
                // DELIVERED -> READ: пользователь прочитал сообщение
                .and()
                .withExternal()
                    .source(MessageState.DELIVERED).target(MessageState.READ)
                    .event(MessageEvent.MESSAGE_READ)
                // DELIVERED -> EDITED: автор редактирует сообщение
                .and()
                .withExternal()
                    .source(MessageState.DELIVERED).target(MessageState.EDITED)
                    .event(MessageEvent.MESSAGE_EDITED)
                // SENT -> FAILED: если сообщение не доставлено в течение 30 секунд или пользователя не существует
                .and()
                .withExternal().source(MessageState.SENT).target(MessageState.FAILED)
                .event(MessageEvent.MESSAGE_FAILED);
    }
}
