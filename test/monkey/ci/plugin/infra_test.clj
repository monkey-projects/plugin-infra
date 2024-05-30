(ns monkey.ci.plugin.infra-test
  (:require [clojure.test :refer [deftest testing is]]
            [clj-github
             [changeset :as cs]
             [test-helpers :as th]]
            [cheshire.core :as json]
            [monkey.ci.plugin.infra :as sut]))

(defn- ->json [x]
  (json/generate-string x))

(defn- ->base64 [s]
  (.. (java.util.Base64/getEncoder)
      (encodeToString (.getBytes s java.nio.charset.StandardCharsets/UTF_8))))

(def base-path "/repos/monkey-projects/oci-infra")

(deftest patch+commit!
  (testing "updates version in the kustomization file and commits the changes"
    (with-redefs [cs/from-branch!
                  (constantly {:base-revision ::test-changeset})
                  
                  cs/get-content
                  (constantly (sut/->yaml
                               {:images
                                [{:name "website"
                                  :newTag "old-version"}]}))
                  
                  cs/put-content
                  (fn [cs path content]
                    (when (= {:base-revision ::test-changeset} cs)
                      {:base-revision ::new-changeset}))
                  
                  cs/update-branch!
                  (fn [cs]
                    cs)]
      (is (= ::new-changeset
             (-> (sut/patch+commit!
                  (sut/make-client "test-token")
                  :staging "website" "test-version")
                 :base-revision))))))
