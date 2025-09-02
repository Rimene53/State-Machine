package com.example.initialisation;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import java.util.function.Consumer;

public record Initializer(
        Consumer<Context> initializeContext,
        TriConsumer<String, SCXMLExecutor, Context> processState
) {}
