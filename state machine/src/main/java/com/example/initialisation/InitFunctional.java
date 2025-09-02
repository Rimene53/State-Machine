package com.example.initialisation;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InitFunctional {
    private static final Logger logger = Logger.getLogger(InitFunctional.class.getName());
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false); 
    }
    public static Map<String, Initializer> getInitializers(Map<String, Map<String, String>> config) throws Exception {
        Map<String, Initializer> initializers = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : config.entrySet()) {
            String key = entry.getKey();
            String className = entry.getValue().get("class");
            try {
                logger.info("Chargement de la classe : " + className);
                Class<?> clazz = Class.forName(className);
                Method initializeMethod = clazz.getMethod("initialize", Context.class);
                Method processMethod = clazz.getMethod("process", String.class, SCXMLExecutor.class, Context.class);
                Initializer initializer = new Initializer(
                        context -> {
                            try {
                                logger.fine("Initialisation du contexte pour la clé : " + key);
                                initializeMethod.invoke(null, context);
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Erreur pendant initialize() de " + className, e);
                                throw new RuntimeException(e);
                            }
                        },
                        (state, executor, context) -> {
                            try {
                                logger.fine("Exécution process() pour l’état : " + state);
                                processMethod.invoke(null, state, executor, context);
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Erreur pendant process() de " + className, e);
                                throw new RuntimeException(e);
                            }
                        }
                );
                initializers.put(key, initializer);
                logger.info("Initializer ajouté avec succès pour la clé : " + key);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Impossible de charger la classe : " + className, e);
                throw e;
            }
        }
        return initializers;
    }
}
