package com.intellij.xdebugger.impl.evaluate;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.impl.actions.XDebuggerActions;
import com.intellij.xdebugger.impl.ui.XDebuggerEditorBase;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTreePanel;
import com.intellij.xdebugger.impl.ui.tree.nodes.EvaluatingExpressionRootNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author nik
 */
public class EvaluationDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JPanel myResultPanel;
  private JPanel myInputPanel;
  private final XDebuggerTreePanel myTreePanel;
  private EvaluationInputComponent myInputComponent;
  private final XDebuggerEvaluator myEvaluator;
  private final XDebugSession mySession;
  private final XDebuggerEditorsProvider myEditorsProvider;
  private EvaluationDialogMode myMode;
  private final XSourcePosition mySourcePosition;
  private final SwitchModeAction mySwitchModeAction;

  public EvaluationDialog(@NotNull XDebugSession session, final @NotNull XDebuggerEditorsProvider editorsProvider, @NotNull XDebuggerEvaluator evaluator,
                          @NotNull String text, EvaluationDialogMode mode, final XSourcePosition sourcePosition) {
    super(session.getProject(), true);
    mySession = session;
    myEditorsProvider = editorsProvider;
    mySourcePosition = sourcePosition;
    setModal(false);
    setOKButtonText(XDebuggerBundle.message("xdebugger.button.evaluate"));
    setCancelButtonText(XDebuggerBundle.message("xdebugger.evaluate.dialog.close"));
    myTreePanel = new XDebuggerTreePanel(session, editorsProvider, sourcePosition, XDebuggerActions.EVALUATE_DIALOG_TREE_POPUP_GROUP);
    myResultPanel.add(myTreePanel.getMainPanel(), BorderLayout.CENTER);
    myEvaluator = evaluator;
    mySwitchModeAction = new SwitchModeAction();

    new AnAction(){
      public void actionPerformed(AnActionEvent e) {
        doOKAction();
      }
    }.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK)), getRootPane(), myDisposable);
    new AnAction() {
      public void actionPerformed(AnActionEvent e) {
        IdeFocusManager.getInstance(mySession.getProject()).requestFocus(myTreePanel.getTree(), true);
      }
    }.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK)), getRootPane(), myDisposable);

    switchToMode(mode, text);
    init();
  }

  protected void doOKAction() {
    evaluate();
  }

  @Override
  protected Action[] createActions() {
    return new Action[] {getOKAction(), mySwitchModeAction, getCancelAction()};
  }

  private void switchToMode(EvaluationDialogMode mode, String text) {
    if (myMode == mode) return;
    myMode = mode;

    myInputComponent = createInputComponent(mode, text);
    myInputPanel.removeAll();
    myInputPanel.add(myInputComponent.getComponent(), BorderLayout.CENTER);
    myInputPanel.invalidate();
    setTitle(myInputComponent.getTitle());
    mySwitchModeAction.putValue(Action.NAME, myMode == EvaluationDialogMode.EXPRESSION ? XDebuggerBundle.message("button.text.code.fragment.mode") : XDebuggerBundle.message("button.text.expression.mode"));
    final JComponent preferredFocusedComponent = myInputComponent.getInputEditor().getPreferredFocusedComponent();
    if (preferredFocusedComponent != null) {
      IdeFocusManager.getInstance(mySession.getProject()).requestFocus(preferredFocusedComponent, true);
    }
  }

  private EvaluationInputComponent createInputComponent(EvaluationDialogMode mode, String text) {
    final Project project = mySession.getProject();
    if (mode == EvaluationDialogMode.EXPRESSION) {
      return new ExpressionInputComponent(project, myEditorsProvider, mySourcePosition, text);
    }
    else {
      return new CodeFragmentInputComponent(project, myEditorsProvider, mySourcePosition, text);
    }
  }

  private void evaluate() {
    final XDebuggerTree tree = myTreePanel.getTree();
    final EvaluatingExpressionRootNode root = new EvaluatingExpressionRootNode(this, tree);
    tree.setRoot(root, false);
    myResultPanel.invalidate();
    myInputComponent.getInputEditor().selectAll();
  }

  protected void dispose() {
    myTreePanel.dispose();
    super.dispose();
  }

  protected String getDimensionServiceKey() {
    return "#xdebugger.evaluate";
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public void startEvaluation(XDebuggerEvaluator.XEvaluationCallback evaluationCallback) {
    final XDebuggerEditorBase inputEditor = myInputComponent.getInputEditor();
    inputEditor.saveTextInHistory();
    String expression = inputEditor.getText();
    myEvaluator.evaluate(expression, evaluationCallback);
  }

  public JComponent getPreferredFocusedComponent() {
    return myInputComponent.getInputEditor().getPreferredFocusedComponent();
  }

  private class SwitchModeAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      String text = myInputComponent.getInputEditor().getText();
      if (myMode == EvaluationDialogMode.EXPRESSION) {
        switchToMode(EvaluationDialogMode.CODE_FRAGMENT, text);
      }
      else {
        if (text.indexOf('\n') != -1) text = "";
        switchToMode(EvaluationDialogMode.EXPRESSION, text);
      }
    }
  }
}