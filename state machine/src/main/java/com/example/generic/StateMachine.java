package com.example.generic;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.env.SimpleErrorHandler;
import org.apache.commons.scxml.env.SimpleErrorReporter;
import org.apache.commons.scxml.env.jexl.JexlEvaluator;
import org.apache.commons.scxml.io.SCXMLParser;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.TransitionTarget;
import org.xml.sax.InputSource;

import com.example.initialisation.Initializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public class StateMachine {
    private final Dispatcher dispatcher;
    private final Map<String, Initializer> initializers;
    public StateMachine(Dispatcher dispatcher, Map<String, Initializer> initializers) {
        this.dispatcher = dispatcher;
        this.initializers = initializers;
    }
    public void run(String configKey, String filePath) throws Exception {
        SCXML scxml = loadScxml(filePath);
        SCXMLExecutor executor = new SCXMLExecutor(
                new JexlEvaluator(), dispatcher, new SimpleErrorReporter()
        );
        executor.setStateMachine(scxml);
        Context context = executor.getRootContext();

        Initializer initializer = initializers.get(configKey);
        if (initializer == null) {
            throw new IllegalArgumentException("Aucun initializer trouvé pour: " + configKey);
        }

        initializer.initializeContext().accept(context);
        executor.go();
        executor.triggerEvent(new TriggerEvent("init", TriggerEvent.SIGNAL_EVENT));

        while (!executor.getCurrentStatus().isFinal()) {
            @SuppressWarnings("unchecked")
            Set<TransitionTarget> states = (Set<TransitionTarget>) executor.getCurrentStatus().getStates();

            if (states.isEmpty()) {
                System.err.println("Aucun état actif !");
                break;
            }

            TransitionTarget current = states.iterator().next();
            System.out.println("État actuel : " + current.getId());

            initializer.processState().accept(current.getId(), executor, context);
            Thread.sleep(500);
        }

        System.out.println("Machine d'état terminée.");
    }

    private SCXML loadScxml(String filePath) throws Exception {
        InputStream input;
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("Chargement depuis le disque : " + file.getAbsolutePath());
            input = new FileInputStream(file);
        } else {
            System.out.println("Chargement depuis le classpath : " + filePath);
            input = getClass().getClassLoader().getResourceAsStream(filePath);
            if (input == null) {
                throw new IllegalArgumentException("Fichier SCXML introuvable : " + filePath);
            }
        }
        return SCXMLParser.parse(new InputSource(input), new SimpleErrorHandler());
    }
}
