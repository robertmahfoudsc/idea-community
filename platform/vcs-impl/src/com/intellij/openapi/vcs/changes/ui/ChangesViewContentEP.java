package com.intellij.openapi.vcs.changes.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.NotNullFunction;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class ChangesViewContentEP {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.changes.ui.ChangesViewContentEP");

  public static final ExtensionPointName<ChangesViewContentEP> EP_NAME = new ExtensionPointName<ChangesViewContentEP>("com.intellij.changesViewContent");

  @Attribute("tabName")
  public String tabName;

  @Attribute("className")
  public String className;

  @Attribute("predicateClassName")
  public String predicateClassName;

  private PluginDescriptor myPluginDescriptor;
  private ChangesViewContentProvider myInstance;

  public void setPluginDescriptor(PluginDescriptor pluginDescriptor) {
    myPluginDescriptor = pluginDescriptor;
  }

  public String getTabName() {
    return tabName;
  }

  public void setTabName(final String tabName) {
    this.tabName = tabName;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(final String className) {
    this.className = className;
  }

  public String getPredicateClassName() {
    return predicateClassName;
  }

  public void setPredicateClassName(final String predicateClassName) {
    this.predicateClassName = predicateClassName;
  }

  public ChangesViewContentProvider getInstance(Project project) {
    if (myInstance == null) {
      myInstance = (ChangesViewContentProvider) newClassInstance(project, className); 
    }
    return myInstance;
  }

  @Nullable
  public NotNullFunction<Project, Boolean> newPredicateInstance(Project project) {
    //noinspection unchecked
    return predicateClassName != null ? (NotNullFunction<Project, Boolean>)newClassInstance(project, predicateClassName) : null;
  }

  private Object newClassInstance(final Project project, final String className) {
    try {
      final Class<?> aClass = Class.forName(className, true,
                                            myPluginDescriptor == null ? getClass().getClassLoader()  : myPluginDescriptor.getPluginClassLoader());
      return new ConstructorInjectionComponentAdapter(className, aClass).getComponentInstance(project.getPicoContainer());
    }
    catch(Exception e) {
      LOG.error(e);
      return null;
    }
  }
}