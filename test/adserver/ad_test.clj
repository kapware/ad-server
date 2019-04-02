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
                    :country   "PL"
                    :lov       5000}]
      ;; when/then:
      (t/is (s/valid? ::ad/ad valid-ad)))))


(t/deftest ad-viewed-event-example
  (t/testing "Should conform to viewed event"
    ;; given:
    (let [valid-ad-view {:ad-id   "1"
                         :channel "1234-112"}]
      ;; when/then:
      (t/is (s/valid? ::ad/view valid-ad-view)))))
