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

(defn update-img-versions [conf images]
  (reduce-kv update-img-version conf images))

(defn update-versions [yaml images]
  (some-> yaml
          (yaml->)
          (update-img-versions images)
          (->yaml)))

(def env->path
  (comp (partial format "kubernetes/monkeyci/%s/kustomization.yaml") name))

(defn patch-versions
  "Fetches the `kustomization.yaml` at path for given env, and updates the 
   version of the named images.  Returns the updated changeset."
  [updater env images]
  (updater (env->path env) #(update-versions % images)))

