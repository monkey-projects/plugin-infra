(ns user
  (:require [config.core :as cc]
            [monkey.ci.plugin.infra :as i]))

(defn patch+commit!
  "Patches version of specified env and image, then commits the changeset."
  [env img new-v]
  (-> cc/env
      :github-token
      (i/make-client)
      (i/patch+commit env img new-v)))
