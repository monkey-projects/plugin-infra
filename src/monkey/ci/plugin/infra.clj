(ns monkey.ci.plugin.infra
  "MonkeyCI infra functions, that are used in build scripts to auto-deploy updates."
  (:require [clj-github
             [changeset :as cs]
             [httpkit-client :as ghc]]
            [monkey.ci.plugin
             [clj :as clj]
             [kube :as kube]]))

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

(def patcher-by-env
  {:prod kube/patch-version
   :staging clj/patch-version})

(defn- patch-version [cs env img new-v]
  (if-let [p (get patcher-by-env env)]
    (p (partial cs/update-content cs) env img new-v)
    (throw (ex-info "Unsupported environment" {:env env}))))

(defn commit-msg [env img new-v]
  (format "Upgraded %s %s to version %s" (name env) img new-v))

(defn patch+commit!
  "Patches version of specified env and image, then commits the changeset."
  [c env img new-v]
  (some-> (make-changeset c)
          (patch-version env img new-v)
          (cs/commit! (commit-msg env img new-v))
          (cs/update-branch!)))
