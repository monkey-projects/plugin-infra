(ns monkey.ci.plugin.infra
  (:require [clj-github
             [changeset :as cs]
             [httpkit-client :as ghc]
             [repository :as repo]]
            [clj-yaml.core :as yaml]))

(def org "monkey-projects")
(def repo "oci-infra")
(def branch "main")

(defn make-client
  "Creates a new client using the given github access token."
  [token]
  (ghc/new-client {:token token}))

(defn make-changeset [c]
  (cs/from-branch! c org repo branch))

(defn get-file [cs path]
  (cs/get-content cs path))

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
  [cs env img new-v]
  (cs/update-content cs (env->path env) #(update-version % img new-v)))

(defn commit-msg [env img new-v]
  (format "Upgraded %s %s to version %s" (name env) img new-v))

(defn patch+commit!
  "Patches version of specified env and image, then commits the changeset."
  [c env img new-v]
  (some-> (make-changeset c)
          (patch-version env img new-v)
          (cs/commit! (commit-msg env img new-v))
          (cs/update-branch!)))
