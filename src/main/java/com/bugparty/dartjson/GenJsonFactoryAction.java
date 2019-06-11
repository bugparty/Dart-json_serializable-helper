package com.bugparty.dartjson;

import org.jetbrains.annotations.NotNull;

public class GenJsonFactoryAction extends BaseDartGenerateAction {

    @NotNull
    @Override
    protected BaseDartJsonSerializerHandler getGenerateHandler() {
        return new DartGenJsonFactoryHandler();
    }
}