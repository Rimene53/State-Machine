package com.example.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.generic.Dispatcher;
import com.example.generic.StateMachine;
import com.example.generic.ConfigLoader;
import com.example.initialisation.InitFunctional;
import com.example.initialisation.Initializer;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class main implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(main.class, args);
    }

@Override
public void run(String... args) throws Exception {
    String controllerPath = "etc/controller.json";
    List<String> configFiles = ConfigLoader.getConfigFiles(controllerPath);

    Dispatcher dispatcher = new Dispatcher();

    for (String configFilePath : configFiles) {
        Map<String, String> config = ConfigLoader.loadSingleConfig(configFilePath);

        String key = config.keySet().iterator().next(); 
        String path = config.get("path");
        String className = config.get("class");
        Map<String, Initializer> initializers = InitFunctional.getInitializers(
            Map.of(key, Map.of("class", className))
        );

        StateMachine runner = new StateMachine(dispatcher, initializers);
        System.out.println("===== Exécution de : " + key + " (" + path + ") =====");
        runner.run(key, path);
    }
}

}
