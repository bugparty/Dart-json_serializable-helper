package com.bugparty.dartjson;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CreateJsonSerializerJsonFix extends BaseCreateMethodsFix<DartComponent> {
    public CreateJsonSerializerJsonFix(@NotNull final DartClass dartClass) {
        super(dartClass);
    }

    @Override
    protected void processElements(@NotNull final Project project,
                                   @NotNull final Editor editor,
                                   @NotNull final Set<DartComponent> elementsToProcess) {
        final TemplateManager templateManager = TemplateManager.getInstance(project);
        anchor = doAddMethodsForOne(editor, templateManager, buildFunctionsText(templateManager, elementsToProcess), anchor);
    }

    @Override
    @NotNull
    protected String getNothingFoundMessage() {
        return ""; // can't be called actually because processElements() is overridden
    }

    protected Template buildFunctionsText(TemplateManager templateManager, Set<DartComponent> elementsToProcess) {
        final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
        template.setToReformat(true);
/*
  factory Inbound.fromJson(Map<String, dynamic> json) => _$InboundFromJson(json);
  Map<String, dynamic> toJson() => _$InboundToJson(this);
 */
        //noinspection ConstantConditions
        template.addTextSegment("factory ");
        template.addTextSegment(myDartClass.getName());
        template.addTextSegment(".fromJson(Map<String, dynamic> json) => _$");
        template.addTextSegment(myDartClass.getName());
        template.addTextSegment("FromJson(json);\n");
        template.addTextSegment("Map<String, dynamic> toJson() => _$");
        template.addTextSegment(myDartClass.getName());
        template.addTextSegment("ToJson(this);");
        return template;
    }

    @Override
    protected Template buildFunctionsText(TemplateManager templateManager, DartComponent e) {
        // ignore
        return null;
    }
}
