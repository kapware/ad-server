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
  (if body
    (cheshire/parse-string (slurp body) true)))


(t/deftest naive-ad-management
  ;; given:
  (let [handler       adserver/handler

        example-ad    (-> (gen/generate (shadow-generator (s/gen ::ad/ad)
                                                          {:channel "foo"}))
                          (dissoc :ad-id))
        ;; when:
        post-response (handler (-> (mock/request :post "/api/v1/ad" example-ad)
                                   (mock/json-body example-ad)))
        post-body     (-> post-response
                          :body
                          parse-body)
        ad-location   (get-in post-response [:headers "Location"])
        ;; then:
        _             (t/testing "Sanity check when posting an ad"
                        (t/is (nil? post-body))
                        (t/is (= 201 (:status post-response)))
                        (t/is (some? ad-location)))

        ;; when:
        ;; NOTE: Consider GET vs POST here on large number of params? Will stick with GET for now as it
        ;;       is idempotent, and natural as we're GETting an ad.
        get-response  (handler (mock/request :get (str "/api/v1" ad-location)))]

    ;; then
    (t/testing "Ad server returns ad by id"
      (t/is (= 200 (:status get-response)))
      (t/is (= example-ad (parse-body (:body get-response)))))))





(comment
  ;; Generator helper, that would generate specific ads
  (gen/sample (shadow-generator (s/gen ::ad/request) {:channel "bar" :locale "pl"})) 
  )

