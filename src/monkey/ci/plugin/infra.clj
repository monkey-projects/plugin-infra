(ns monkey.ci.plugin.infra
  "MonkeyCI infra functions, that are used in build scripts to auto-deploy updates."
  (:require [clj-github
             [changeset :as cs]
             [httpkit-client :as ghc]]
            [clojure.string :as s]
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
  {:prod kube/patch-versions
   :staging kube/patch-versions})

(defn- patch-versions [cs env images]
  (if-let [p (get patcher-by-env env)]
    (p (partial cs/update-content cs) env images)
    (throw (ex-info "Unsupported environment" {:env env}))))

(defn commit-msg [env images]
  (letfn [(upgrade-msg [[img new-v]]
            (str img " to " new-v))]
    (->> images
         (map upgrade-msg)
         (s/join ", ")
         (format "Upgraded %s: %s" (name env)))))

(defn patch+commit!
  "Patches version of specified env and images, then commits the changeset.
   The images is a map of image + version"
  [c env images]
  (some-> (make-changeset c)
          (patch-versions env images)
          (cs/commit! (commit-msg env images))
          (cs/update-branch!)))
