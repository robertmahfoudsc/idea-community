package com.intellij.codeInsight.template.macro;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.ExpectedTypeInfo;
import com.intellij.codeInsight.ExpectedTypesProvider;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.template.*;
import com.intellij.codeInsight.template.impl.JavaTemplateUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.text.BlockSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public class ExpectedTypeMacro implements Macro{

  public String getName() {
    return "expectedType";
  }

  public String getDescription() {
    return CodeInsightBundle.message("macro.expected.type");
  }

  public String getDefaultValue() {
    return "A";
  }

  public Result calculateResult(@NotNull Expression[] params, ExpressionContext context) {
    PsiType[] types = getExpectedTypes(params, context);
    if (types == null || types.length == 0) return null;
    return new PsiTypeResult(types[0], context.getProject());
  }

  public Result calculateQuickResult(@NotNull Expression[] params, ExpressionContext context) {
    return null;
  }

  public LookupElement[] calculateLookupItems(@NotNull Expression[] params, ExpressionContext context) {
    PsiType[] types = getExpectedTypes(params, context);
    if (types == null || types.length < 2) return null;
    Set<LookupItem> set = new LinkedHashSet<LookupItem>();
    for (PsiType type : types) {
      JavaTemplateUtil.addTypeLookupItem(set, type);
    }
    return set.toArray(new LookupItem[set.size()]);
  }

  @Nullable
  private static PsiType[] getExpectedTypes(Expression[] params, final ExpressionContext context) {
    if (params.length != 0) return null;

    final Project project = context.getProject();
    PsiDocumentManager.getInstance(project).commitAllDocuments();
    PsiType[] types = null;

    final int offset = context.getTemplateStartOffset();
    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(context.getEditor().getDocument());
    assert file != null;
    final PsiFile fileCopy = (PsiFile)file.copy();
    
    new WriteCommandAction(project) {
      protected void run(com.intellij.openapi.application.Result result) throws Throwable {
        final BlockSupport blockSupport = ServiceManager.getService(project, BlockSupport.class);
        blockSupport.reparseRange(fileCopy, offset, offset, CompletionUtil.DUMMY_IDENTIFIER);
      }
    }.execute();
    PsiElement element = fileCopy.findElementAt(offset);

    if (element instanceof PsiIdentifier && element.getParent() instanceof PsiExpression) {
      ExpectedTypeInfo[] infos = ExpectedTypesProvider.getInstance(project).getExpectedTypes((PsiExpression)element.getParent(), true);
      if (infos.length > 0){
        types = new PsiType[infos.length];
        for(int i = 0; i < infos.length; i++) {
          ExpectedTypeInfo info = infos[i];
          types[i] = info.getType();
        }
      }
    }

    return types;
  }
}