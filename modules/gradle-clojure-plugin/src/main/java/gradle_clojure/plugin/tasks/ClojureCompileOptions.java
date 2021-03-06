package gradle_clojure.plugin.tasks;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;

public final class ClojureCompileOptions implements Serializable {
  private final ClojureForkOptions forkOptions = new ClojureForkOptions();

  private boolean aotCompile = false;
  private Boolean copySourceSetToOutput = null;
  private ReflectionWarnings reflectionWarnings = new ReflectionWarnings(false, false, false);

  private boolean disableLocalsClearing = false;
  private List<String> elideMeta = Collections.emptyList();
  private boolean directLinking = false;

  @Nested
  public ClojureForkOptions getForkOptions() {
    return forkOptions;
  }

  public ClojureCompileOptions forkOptions(Action<? super ClojureForkOptions> configureAction) {
    configureAction.execute(forkOptions);
    return this;
  }

  /*
   * We only have this variant (instead of just Action) since Gradle doesn't currently (as of 4.7)
   * instrument Action methods on nested config objects
   */
  public ClojureCompileOptions forkOptions(Closure<?> configureAction) {
    configureAction.setResolveStrategy(Closure.DELEGATE_FIRST);
    configureAction.setDelegate(forkOptions);
    configureAction.call(forkOptions);
    return this;
  }

  @Input
  public boolean isAotCompile() {
    return aotCompile;
  }

  public void setAotCompile(boolean aotCompile) {
    this.aotCompile = aotCompile;
  }

  @Input
  public boolean isCopySourceSetToOutput() {
    return copySourceSetToOutput == null ? !aotCompile : copySourceSetToOutput;
  }

  public void setCopySourceSetToOutput(boolean copySourceSetToOutput) {
    this.copySourceSetToOutput = copySourceSetToOutput;
  }

  @Nested
  public ReflectionWarnings getReflectionWarnings() {
    return reflectionWarnings;
  }

  public void setReflectionWarnings(ReflectionWarnings reflectionWarnings) {
    this.reflectionWarnings = reflectionWarnings;
  }

  public ClojureCompileOptions reflectionWarnings(Action<? super ReflectionWarnings> configureAction) {
    configureAction.execute(reflectionWarnings);
    return this;
  }

  /*
   * We only have this variant (instead of just Action) since Gradle doesn't currently (as of 4.7)
   * instrument Action methods on nested config objects
   */
  public ClojureCompileOptions reflectionWarnings(Closure<?> configureAction) {
    configureAction.setResolveStrategy(Closure.DELEGATE_FIRST);
    configureAction.setDelegate(reflectionWarnings);
    configureAction.call(reflectionWarnings);
    return this;
  }

  @Input
  public boolean isDisableLocalsClearing() {
    return disableLocalsClearing;
  }

  public void setDisableLocalsClearing(boolean disableLocalsClearing) {
    this.disableLocalsClearing = disableLocalsClearing;
  }

  @Input
  public List<String> getElideMeta() {
    return elideMeta;
  }

  public void setElideMeta(List<String> elideMeta) {
    this.elideMeta = elideMeta;
  }

  @Input
  public boolean isDirectLinking() {
    return directLinking;
  }

  public void setDirectLinking(boolean directLinking) {
    this.directLinking = directLinking;
  }
}
