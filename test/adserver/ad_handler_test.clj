(ns adserver.ad-handler-test
  (:require [clojure.test             :as t]
            [adserver.core            :as adserver]
            [adserver.ad-handler      :as sut]
            [adserver.ad              :as ad]
            [ring.mock.request        :as mock]
            [clojure.spec.gen.alpha   :as gen]
            [clojure.spec.alpha       :as s]
            [cheshire.core            :as cheshire]))

(defn shadow-generator [gen shadow-values]
  (gen/fmap (fn [a-map] (merge a-map shadow-values))
            gen))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))


(t/deftest any-ad-for-channel-is-returned-aka-fallback-roundtrip
  ;; given:
  (let [handler       adserver/handler

        example-ad    (gen/generate (shadow-generator (s/gen ::ad/request)
                                                   {:channel "foo"}))

        ;; when:
        post-response (handler (-> (mock/request :post "/api/v1/ad" example-ad)
                                   (mock/json-body example-ad)))

        ;; then:
        _             (t/testing "Sanity check when posting an ad"
                        (t/is (= (:status post-response) 201)))

        ;; when:
        ;; NOTE: Consider GET vs POST here on large number of params? Will stick with GET for now as it
        ;;       is idempotent, and natural as we're GETting an ad.
        result     (handler (mock/request :get "/api/v1/ad" {:channel "foo"}))]

    ;; then
  (t/testing "Ad server matches correct ads"
    (t/is (= example-ad result)))))





(comment
  ;; Generator helper, that would generate specific ads
  (gen/sample (shadow-generator (s/gen ::ad/request) {:channel "bar" :locale "pl"})) 
  )

