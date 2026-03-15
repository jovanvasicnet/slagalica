package com.securevault.security;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

public class SuggestionSocket extends TextWebSocketHandler {

    private static Map<Integer,List<WebSocketSession>> teamSessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        int teamId = Integer.parseInt(
                session.getUri().getQuery().split("=")[1]
        );

        teamSessions
                .computeIfAbsent(teamId,k->new ArrayList<>())
                .add(session);

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();

        String[] parts = payload.split("\\|");

        int teamId = Integer.parseInt(parts[0]);
        String suggestion = parts[1];

        List<WebSocketSession> sessions = teamSessions.get(teamId);

        if(sessions != null){

            for(WebSocketSession s : sessions){

                s.sendMessage(new TextMessage(suggestion));

            }

        }

    }

}
