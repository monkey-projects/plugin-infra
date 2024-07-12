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

(def img-map
  {"website" :website
   "monkeyci-gui" :gui
   "monkeyci-api" :api
   "notifier" :notifier})

(defn update-img-versions [conf images]
  (reduce-kv (fn [conf img new-v]
               (assoc conf (img-map img) new-v))
             conf
             images))

(defn- pprint [obj]
  (with-open [pw (StringWriter.)]
    (cp/pprint obj pw)
    (.toString pw)))

(defn update-versions [edn images]
  (some-> edn
          (parse-edn)
          (update-img-versions images)
          (pprint)))

(defn patch-versions
  "Patches the `versions.edn` file in the clj resources for the given env."
  [updater env images]
  (updater (env->path env) #(update-versions % images)))
