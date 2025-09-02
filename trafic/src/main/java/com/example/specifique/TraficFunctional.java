package com.example.specifique;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.TriggerEvent;

public class TraficFunctional {

    public static void initialize(Context context) {
        System.out.println("Trafic context initialized.");
        context.set("mode", "auto");
        context.set("duration", 1); 
    }

    public static void process(String stateId, SCXMLExecutor executor, Context context) throws Exception {
        String mode = (String) context.get("mode");
        switch (stateId) {
            case "rouge" -> System.out.println("Feu Rouge allumé ! Stop des voitures.");
            case "orange" -> System.out.println("Feu Orange allumé ! Préparez-vous à avancer.");
            case "vert" -> System.out.println("Feu Vert allumé ! Les voitures peuvent avancer.");
            default -> System.out.println("État inconnu : " + stateId);
        }

        Object durationObj = context.get("duration");
        int duration = 0;
        if (durationObj instanceof Number) {
            duration = ((Number) durationObj).intValue();
        }
        if (duration > 0) {
            System.out.println("Attente de " + duration + "s dans l'état: " + stateId);
            Thread.sleep(duration * 1000L);
        }
        executor.triggerEvent(new TriggerEvent("com.example.specifique.TraficFunctional::next", TriggerEvent.SIGNAL_EVENT));
    }
}
