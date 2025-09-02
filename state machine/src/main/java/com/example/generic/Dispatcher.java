package com.example.generic;
import org.apache.commons.scxml.EventDispatcher;
import java.util.*;

public class Dispatcher implements EventDispatcher {
    private final List<String> dispatchedEvents = new ArrayList<>();
    public List<String> getDispatchedEvents() {
        return dispatchedEvents;
    }
    @Override
    public void send(String sendId, String target, String type, String event, Map data, Object hints, long delay, List externalNodes) {
        System.out.println("Event sent: " + event);
        dispatchedEvents.add(event);
    }
    @Override
    public void cancel(String sendId) {
        System.out.println("Event canceled: " + sendId);
    }
}
