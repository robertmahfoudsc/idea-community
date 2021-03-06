/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.psi.impl.source;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaCodeReferenceCodeFragment;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.ElementType;
import org.jetbrains.annotations.NonNls;

/**
 * @author ven
 */
public class PsiJavaCodeReferenceCodeFragmentImpl extends PsiCodeFragmentImpl implements PsiJavaCodeReferenceCodeFragment {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.PsiJavaCodeReferenceCodeFragmentImpl");
  private final boolean myIsClassesAccepted;

  public PsiJavaCodeReferenceCodeFragmentImpl(final Project project,
                                              final boolean isPhysical,
                                              final @NonNls String name,
                                              final CharSequence text,
                                              boolean isClassesAccepted) {
    super(project, Constants.REFERENCE_TEXT, isPhysical, name, text);
    myIsClassesAccepted = isClassesAccepted;
  }

  public PsiJavaCodeReferenceElement getReferenceElement() {
    final CompositeElement treeElement = calcTreeElement();
    LOG.assertTrue (treeElement.getFirstChildNode().getElementType() == ElementType.JAVA_CODE_REFERENCE);
    return (PsiJavaCodeReferenceElement)SourceTreeToPsiMap.treeElementToPsi(treeElement.getFirstChildNode());
  }

  public boolean isClassesAccepted() {
    return myIsClassesAccepted;
  }
}
