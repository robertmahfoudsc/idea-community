package com.intellij.testFramework.exceptionCases;

import java.util.EmptyStackException;

/**
 * @author Roman Chernyatchik
 */
public abstract class EmptyStackExceptionCase extends AbstractExceptionCase<EmptyStackException> {
  public Class<EmptyStackException> getExpectedExceptionClass() {
    return EmptyStackException.class;
  }
}