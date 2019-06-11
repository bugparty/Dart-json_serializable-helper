package com.bugparty.dartjson;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.DartNamedElementNode;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartClassDefinition;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class DartGenJsonFactoryHandler extends BaseDartJsonSerializerHandler {
    @NotNull
    @Override
    protected BaseCreateMethodsFix createFix(@NotNull DartClass dartClass) {
        return new CreateJsonSerializerJsonFix(dartClass);
    }

    @NotNull
    @Override
    protected String getTitle() {
        return "Generate Json factory";
    }

    @Override
    protected void collectCandidates(@NotNull DartClass dartClass, @NotNull List<DartComponent> candidates) {
        candidates.addAll(ContainerUtil.findAll(computeClassMembersMap(dartClass, false).values(),
                component -> DartComponentType.typeOf(component) == DartComponentType.FIELD));
    }
    public void invoke(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file, final int offset) {
        final DartClass dartClass = PsiTreeUtil.getParentOfType(file.findElementAt(offset), DartClassDefinition.class);
        if (dartClass == null) return;
        List<DartNamedElementNode> selectedElements = Collections.emptyList();
        doInvoke(project, editor, file, selectedElements, createFix(dartClass));
    }

    @Override
    protected boolean doAllowEmptySelection() {
        return false;
    }
}
