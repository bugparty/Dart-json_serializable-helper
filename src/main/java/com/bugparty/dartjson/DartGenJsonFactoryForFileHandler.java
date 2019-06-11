package com.bugparty.dartjson;

import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.DartNamedElementNode;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartClassDefinition;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DartGenJsonFactoryForFileHandler extends BaseDartJsonSerializerHandler {
    @Override
    public boolean isValidFor(@NotNull final Editor editor, @NotNull final PsiFile file) {
        return file instanceof DartFile &&
                ((DartFile) file).findChildrenByClass(DartClass.class) != null;
    }

    @NotNull
    @Override
    protected BaseCreateMethodsFix createFix(@NotNull DartClass dartClass) {
        return new CreateJsonSerializerJsonFix(dartClass);
    }

    @NotNull
    @Override
    protected String getTitle() {
        return "Generate Json factory for file";
    }

    @Override
    protected void collectCandidates(@NotNull DartClass dartClass, @NotNull List<DartComponent> candidates) {

    }


    protected void collectCandidates2(@NotNull PsiFile dartFile, @NotNull List<DartComponent> candidates) {
        candidates.addAll(ContainerUtil.findAll(computeClasssMap(dartFile, false).values(),
                component -> DartComponentType.typeOf(component) == DartComponentType.CLASS));
    }
    @NotNull
    protected final Map<Pair<String, Boolean>, DartComponent> computeClasssMap(@NotNull final PsiFile dartFile,
                                                                                     final boolean doIncludeStatics) {
        Collection<DartClass> classes = DartResolveUtil.getClassDeclarations(dartFile);
        classes = ContainerUtil.filter(classes, NOT_CONSTRUCTOR_CONDITION);
        if (!doIncludeStatics) {
            classes = ContainerUtil.filter(classes, NOT_STATIC_CONDITION);
        }
        return DartResolveUtil.namedComponentToMap((List<? extends DartComponent>) classes);
    }


    @Override
    protected boolean doAllowEmptySelection() {
        return false;
    }

    public void invoke(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file, final int offset) {
        final DartClass dartClass = PsiTreeUtil.getParentOfType(file.findElementAt(offset), DartClassDefinition.class);
        if (dartClass == null) return;

        final List<DartComponent> candidates = new ArrayList<>();
        collectCandidates2(file, candidates);

        List<DartNamedElementNode> selectedElements = Collections.emptyList();
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            selectedElements = ContainerUtil.map(candidates, DartNamedElementNode::new);
        }
        else if (!candidates.isEmpty()) {
            final MemberChooser<DartNamedElementNode> chooser = createMemberChooserDialog(project, dartClass, candidates, getTitle());
            chooser.show();
            if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;

            selectedElements = chooser.getSelectedElements();
        }

        doInvoke(project, editor, file, selectedElements);
    }

    protected void doInvoke(@NotNull final Project project,
                            @NotNull final Editor editor,
                            @NotNull final PsiFile file,
                            @NotNull final Collection<DartNamedElementNode> selectedElements) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                BaseCreateMethodsFix createMethodsFix;
                final List<BaseCreateMethodsFix> fixes = new ArrayList<>(selectedElements.size());
                for(DartNamedElementNode dnen : selectedElements) {
                    DartClass cls = (DartClass) dnen.getPsiElement();
                    createMethodsFix = createFix(cls);
                    createMethodsFix.addElementsToProcessFrom(selectedElements);
                    createMethodsFix.beforeInvoke(project, editor, file);
                    fixes.add(createMethodsFix);
                }

                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (BaseCreateMethodsFix createMethodsFix : fixes) {
                                createMethodsFix.invoke(project, editor, file);
                            }

                        }
                        catch (IncorrectOperationException ex) {
                            Logger.getInstance(getClass().getName()).error(ex);
                        }
                    }
                });
            }
        };

        if (CommandProcessor.getInstance().getCurrentCommand() == null) {
            CommandProcessor.getInstance().executeCommand(project, runnable, getClass().getName(), null);
        }
        else {
            runnable.run();
        }
    }
}
