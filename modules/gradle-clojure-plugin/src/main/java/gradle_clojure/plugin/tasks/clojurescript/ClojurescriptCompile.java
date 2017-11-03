/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gradle_clojure.plugin.tasks.clojurescript;

import gradle_clojure.plugin.internal.ClojureWorkerExecutor;
import gradle_clojure.plugin.tasks.clojure.ClojureCompile;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClojurescriptCompile extends AbstractCompile {
  private static final Logger logger = Logging.getLogger(ClojureCompile.class);

  private final ClojureWorkerExecutor workerExecutor;
  private ClojurescriptCompileOptions options = new ClojurescriptCompileOptions();

  @Inject
  public ClojurescriptCompile(WorkerExecutor workerExecutor) {
    this.workerExecutor = new ClojureWorkerExecutor(getProject(), workerExecutor);
  }

  @Nested
  public ClojurescriptCompileOptions getOptions() {
    return options;
  }

  public ClojurescriptCompile options(Action<? super ClojurescriptCompileOptions> configureAction) {
    configureAction.execute(options);
    return this;
  }

  @Override
  @TaskAction
  protected void compile() {
    FileCollection classpath = getClasspath().plus(getProject().files(getSourceRootsFiles()));

    workerExecutor.submit(config -> {
      config.setClasspath(classpath);
      config.setNamespace("gradle-clojure.tools.clojurescript-compiler");
      config.setFunction("compiler");
      config.setArgs(getSourceRoots(), getDestinationDir(), getOptions());
      config.forkOptions(fork -> {
        fork.setJvmArgs(options.getForkOptions().getJvmArgs());
        fork.setMinHeapSize(options.getForkOptions().getMemoryInitialSize());
        fork.setMaxHeapSize(options.getForkOptions().getMemoryMaximumSize());
        fork.setDefaultCharacterEncoding(StandardCharsets.UTF_8.name());
      });
    });
  }

  @Internal
  private Set<String> getSourceRoots() {
    return getSourceRootsFiles().stream().map(it -> {
      try {
        return it.getCanonicalPath();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }).collect(Collectors.toSet());
  }

  @Internal
  private List<File> getSourceRootsFiles() {
    // accessing the List<Object> field not the FileTree from getSource
    return source.stream()
        .filter(it -> it instanceof SourceDirectorySet)
        .flatMap(it -> ((SourceDirectorySet) it).getSrcDirs().stream())
        .collect(Collectors.toList());
  }
}
