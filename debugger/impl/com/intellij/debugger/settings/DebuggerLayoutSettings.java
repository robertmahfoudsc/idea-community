package com.intellij.debugger.settings;

import com.intellij.debugger.ui.DebuggerContentInfo;
import com.intellij.debugger.ui.content.newUI.NewContentState;
import com.intellij.debugger.ui.content.newUI.PlaceInGrid;
import com.intellij.debugger.ui.content.newUI.Tab;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.Key;
import com.intellij.ui.content.Content;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@State(
  name = "DebuggerLayoutSettings",
  storages = {
    @Storage(
      id ="other",
      file = "$APP_CONFIG$/debugger.layout.xml"
    )}
)
public class DebuggerLayoutSettings implements PersistentStateComponent<Element>, ApplicationComponent {

  private static final String VIEW_STATE = "viewState";

  private Map<String, NewContentState> myContentStates = new HashMap<String, NewContentState>();
  private Set<Tab> myTabs = new HashSet<Tab>();

  public Element getState() {
    return write(new Element("layout"));
  }

  public void loadState(final Element state) {
    read(state);
  }

  public Element read(final Element parentNode)  {
    final List newContents = parentNode.getChildren(VIEW_STATE);
    myContentStates.clear();
    for (Object content : newContents) {
      final NewContentState state = new NewContentState(this, (Element)content);
      myContentStates.put(state.getID(), state);
    }

    myTabs.clear();
    List tabs = parentNode.getChildren(Tab.TAB);
    for (Object eachTabElement : tabs) {
      myTabs.add(new Tab((Element)eachTabElement));
    }

    return parentNode;
  }

  public Element write(final Element parentNode) {
    for (NewContentState eachState : myContentStates.values()) {
      final Element content = new Element(VIEW_STATE);
      parentNode.addContent(content);
      eachState.write(content);
    }

    for (Tab eachTab : myTabs) {
      eachTab.write(parentNode);
    }

    return parentNode;
  }

  public Tab getOrCreateTab(final int index) {
    for (Tab each : myTabs) {
      if (index == each.getIndex()) return each;
    }

    Tab tab = new Tab(index, index == 0 ? "Debugger" : null, null);
    myTabs.add(tab);

    return tab;
  }

  public NewContentState getStateFor(Content content) {
    Key key = content.getUserData(DebuggerContentInfo.CONTENT_KIND);
    NewContentState state = myContentStates.get(key.toString());
    return state != null ? state : getDefaultContentState(content);
  }

  private NewContentState getDefaultContentState(final Content content) {
    NewContentState state;

    final Key kind = content.getUserData(DebuggerContentInfo.CONTENT_KIND);
    if (DebuggerContentInfo.FRAME_CONTENT.equals(kind)) {
      state =  new NewContentState(kind.toString(), getOrCreateTab(0), PlaceInGrid.left, false);
    } else if (DebuggerContentInfo.VARIABLES_CONTENT.equals(kind)) {
      state =  new NewContentState(kind.toString(), getOrCreateTab(0), PlaceInGrid.center, false);
    } else if (DebuggerContentInfo.WATCHES_CONTENT.equals(kind)) {
      state =  new NewContentState(kind.toString(), getOrCreateTab(0), PlaceInGrid.right, false);
    } else if (DebuggerContentInfo.CONSOLE_CONTENT.equals(kind)) {
      state =  new NewContentState(kind.toString(), getOrCreateTab(1), PlaceInGrid.bottom, false);
    } else {
      state =  new NewContentState(content.getDisplayName(), getOrCreateTab(Integer.MAX_VALUE), PlaceInGrid.unknown, false);
    }

    myContentStates.put(state.getID(), state);

    return state;
  }

  public Tab getSelectedTab() {
    return getOrCreateTab(0);
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "DebuggerLayoutSettings";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }
}
