(ns monkey.ci.plugin.clj
  "Functions for patching files in the clj dir"
  (:require [clojure.edn :as edn]
            [clojure.pprint :as cp])
  (:import [java.io StringReader StringWriter PushbackReader]))

(def env->path
  (comp (partial format "clj/resources/%s/versions.edn") name))

(defn parse-edn [s]
  (with-open [r (PushbackReader. (StringReader. s))]
    (edn/read r)))

(defn update-img-version [conf img new-v]
  (assoc conf (keyword img) new-v))

(defn- pprint [obj]
  (with-open [pw (StringWriter.)]
    (cp/pprint obj pw)
    (.toString pw)))

(defn update-version [edn img new-v]
  (some-> edn
          (parse-edn)
          (update-img-version img new-v)
          (pprint)))

(defn patch-version
  "Patches the `versions.edn` file in the clj resources for the given env."
  [updater env img new-v]
  (updater (env->path env) #(update-version % img new-v)))
