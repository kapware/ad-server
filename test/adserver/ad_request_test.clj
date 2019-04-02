(ns adserver.ad-request-test
  (:require [adserver.ad            :as ad]
            [clojure.test           :as t]
            [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as gen]))


(t/deftest ad-request-examples
  (t/testing "Should conform to basic request examples"
    ;; given:
    (let [valid-ad-request {:channel   "123432-121" ;; uuid
                            :ad-ids    ["1"] ;; if undefined, then return available ad
                            :locale    "pl_PL"
                            :country   "PL"
                            :device    ""
                            :interests ["IAB3-9", "IAB4-1"]}]
      ;; when/then:
      (t/is (s/valid? ::ad/request valid-ad-request)))))


(t/deftest ad-response-examples
  (t/testing "Should conform to basic response examples"
    ;; given:
    (let [valid-ad-response [{:ad-id    "1"
                              :ad-content-url "s3://ads-bucket/ad-1"}
                             {:ad-id    "2"
                              :ad-content-url "s3://ads-bucket/ad-2"}
                             {:ad-id    "3"
                              :ad-content-url "s3://ads-bucket/ad-3"}]]
      ;; when/then:
      (t/is (s/valid? ::ad/response valid-ad-response)))))

