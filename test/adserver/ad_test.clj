(ns adserver.ad-test
  (:require [adserver.ad            :as ad]
            [clojure.test           :as t]
            [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as gen]))

(t/deftest ad-examples
  (t/testing "Should conform to basic examples"
    ;; given:
    (let [valid-ad {:ad-id     "1"
                    :channel   "g"
                    :locale    "pl_PL"
                    :country   "PL"}]
      ;; when/then:
      (t/is (s/valid? ::ad/ad valid-ad)))))


;; TODO: limits, since those are expected to be updated frequently, model separately.
;;       events?
