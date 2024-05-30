(ns user
  (:require [config.core :as cc]
            [monkey.ci.plugin.infra :as i]))

(defn patch+commit!
  "Patches version of specified env and image, then commits the changeset."
  ([token env img new-v]
   (-> (i/make-client token)
       (i/patch+commit env img new-v)))
  ([env img new-v]
   (patch+commit! (-> cc/env
                      :github-token
                      (i/make-client))
                  env img new-v)))
