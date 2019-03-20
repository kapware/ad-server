(ns adserver.ad-request-test
  (:require [adserver.ad-request    :as ad-request]
            [clojure.test           :as t]
            [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as gen]))

(t/deftest ad-request-examples
  (t/testing "Should conform to basic examples"
    ;; given:
    (let [valid-ad-request {:channel   "123432-121" ;; uuid
                            :ad-ids    ["1"] ;; if undefined, then return available ad
                            :locale    "pl_PL"
                            :country   "PL"
                            :device    ""
                            :interests ["IAB3-9", "IAB4-1"]}]
      ;; when/then:
      (t/is (s/valid? ::ad-request/ad-request valid-ad-request)))))
