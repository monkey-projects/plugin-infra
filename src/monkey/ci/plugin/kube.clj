(ns monkey.ci.plugin.kube
  "Kubernetes specific functions"
  (:require [clj-yaml.core :as yaml]))

(def yaml-> yaml/parse-string)
(defn ->yaml [x]
  (yaml/generate-string x :dumper-options {:flow-style :block}))

(defn update-img-version [conf img new-v]
  (letfn [(replace-version [imgs]
            (if-let [match (->> imgs
                                (filter (comp (partial = img) :name))
                                (first))]
              (replace {match (assoc match :newTag new-v)} imgs)
              ;; No change
              imgs))]
    (update conf :images replace-version)))

(defn update-version [yaml img new-v]
  (some-> yaml
          (yaml->)
          (update-img-version img new-v)
          (->yaml)))

(def env->path
  (comp (partial format "kubernetes/monkeyci/%s/kustomization.yaml") name))

(defn patch-version
  "Fetches the `kustomization.yaml` at path for given env, and updates the 
   version of the named image.  Returns the updated changeset."
  [updater env img new-v]
  (updater (env->path env) #(update-version % img new-v)))

