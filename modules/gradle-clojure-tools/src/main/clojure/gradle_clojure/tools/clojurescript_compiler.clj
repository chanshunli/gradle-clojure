(ns gradle-clojure.tools.clojurescript-compiler
  (:require [cljs.build.api :as api]
            [clojure.edn :as edn]
            [clojure.pprint]))

(defn enum->keyword
  [e]
  (when (some? e)
    (keyword (.name e))))

(defn empty->nil
  [coll]
  (if-not (empty? coll)
    coll
    nil))

(defn remove-nil-vals
  [m]
  (->> m
       (filter (comp some? second))
       (into (empty m))))

(defn output-dir [opts]
  (.getAbsolutePath (.getOutputDir opts)))

(defn output-to [opts]
  (.getAbsolutePath (.getOutputTo opts)))

(defn preprocess [lib]
  (when-some [preprocess (.getPreprocess lib)]
    (edn/read-string preprocess)))

(defn global-exports [lib]
  (when-some [exports (seq (.getGlobalExports lib))]
    (into {}
          (for [[k v] exports] [(symbol k) (symbol v)]))))

(defn source-map [opts]
  (let [source-map (.getSourceMap opts)]
    (cond
      (instance? Boolean source-map) (boolean source-map)
      (some? source-map) (.toString source-map)
      :otherwise nil)))

(defn foreign-lib [lib]
  (remove-nil-vals
    {:file (.getFile lib)
     :file-min (.getFileMin lib)
     :provides (.getProvides lib)
     :requires (.getRequires lib)
     :module-type (enum->keyword (.getModuleType lib))
     :preprocess (preprocess lib)
     :global-exports (global-exports lib)}))

(defn foreign-libs [opts]
  (when-some [libs (seq (.getForeignLibs opts))]
    (map foreign-lib libs)))

(defn module [m]
  (remove-nil-vals
    {:output-to (.getOutputTo module)
     :entries (.getEntries module)
     :depends-on (.getDependsOn module)}))

(defn modules [opts]
  (when-some [modules (seq (.getModules opts))]
    (into {}
          (for [[k v] modules]
            [(edn/read-string k) (module v)]))))

(defn preloads [opts]
  (when-some [preloads (seq (.getPreloads opts))]
    (map symbol preloads)))

(defn cljs-opts
  [gradle-compile-opts]
  (remove-nil-vals
    {:output-dir (output-dir gradle-compile-opts)
     :output-to (output-to gradle-compile-opts)
     :optimizations (enum->keyword (.getOptimizations gradle-compile-opts))
     :main (.getMain gradle-compile-opts)
     :asset-path (.getAssetPath gradle-compile-opts)
     :source-map (source-map gradle-compile-opts)
     :verbose (.getVerbose gradle-compile-opts)
     :pretty-print (.getPrettyPrint gradle-compile-opts)
     :target (.getTarget gradle-compile-opts)
     :foreign-libs (foreign-libs gradle-compile-opts)
     :externs (empty->nil (.getExterns gradle-compile-opts))
     :modules (modules gradle-compile-opts)
     :preloads (preloads gradle-compile-opts)
     :npm-deps (empty->nil (.getNpmDeps gradle-compile-opts))
     :install-deps (.getInstallDeps gradle-compile-opts)
     :checked-arrays (enum->keyword (.getCheckedArrays gradle-compile-opts))}))

(defn compiler [source-dirs destination-dir gradle-compile-options]
  (let [opts (cljs-opts gradle-compile-options)]
    (println "Clojurescript compile options:\n" (with-out-str (clojure.pprint/pprint opts)))
    (try
      (api/build
        (apply api/inputs source-dirs)
        opts)
      (catch Throwable e
        (binding [*out* *err*]
          (loop [ex e]
            (if-let [msg (and ex (.getMessage ex))]
              (println "ERROR: " (-> ex .getClass .getCanonicalName) " " msg)
              (recur (.getCause ex)))))
        (throw e)))))
