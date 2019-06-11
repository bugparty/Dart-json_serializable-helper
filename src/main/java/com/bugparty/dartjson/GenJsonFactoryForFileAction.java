package com.bugparty.dartjson;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenJsonFactoryForFileAction extends BaseDartGenerateAction {
    @NotNull
    @Override
    protected BaseDartJsonSerializerHandler getGenerateHandler() {
        return new DartGenJsonFactoryForFileHandler();
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        final Pair<Editor, PsiFile> editorAndPsiFile = getEditorAndPsiFile(e);
        final Editor editor = editorAndPsiFile.first;
        final PsiFile psiFile = editorAndPsiFile.second;


        final int caretOffset = editor == null ? -1 : editor.getCaretModel().getOffset();
        final boolean enable = psiFile != null && doEnable(psiFile);
        //PsiTreeUtil.collectElements(psiFile, (element -> PsiTreeUtil.findChildOfType(element, DartClass.class) != null));
        e.getPresentation().setEnabledAndVisible(enable);
    }

    protected boolean doEnable(@Nullable final PsiFile psiFile) {
        DartClass cls = PsiTreeUtil.findChildOfType(psiFile, DartClass.class);
        return cls != null;
    }
}
