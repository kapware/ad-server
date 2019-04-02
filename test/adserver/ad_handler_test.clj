(ns adserver.ad-handler-test
  (:require [clojure.test                            :as t]
            [adserver.core                           :as adserver]
            [adserver.ad-handler                     :as sut]
            [adserver.ad                             :as ad]
            [ring.mock.request                       :as mock]
            [clojure.spec.gen.alpha                  :as gen]
            [clojure.spec.alpha                      :as s]
            [cheshire.core                           :as cheshire]
            [com.gfredericks.test.chuck.clojure-test :as test.chuck]))

(defn shadow-generator [gen shadow-values]
  (gen/fmap (fn [a-map] (merge a-map shadow-values))
            gen))


(defn parse-body [body]
  (if body
    (cheshire/parse-string (slurp body) true))) 


(t/deftest ad-management
  ;; given:
  (let [handler       adserver/handler

        example-ad    (-> (gen/generate (shadow-generator (s/gen ::ad/ad)
                                                          {:channel "foo"}))
                          (dissoc :ad-id))
        ;; when:
        not-found     (handler (mock/request :get (str "/api/v1/ad/i-dont-exist")))
        ;; then:
        _             (t/testing "On empty set 404 should be reported"
                        (t/is (= 404 (:status not-found))))

        ;; when:
        bad-request   (handler (-> (mock/request :post "/api/v1/ad")
                                   (mock/json-body {:not :an-ad})))
        ;; then:
        _             (t/testing "On invalid post 400 should be reported"
                        (t/is (= 400 (:status bad-request))))

        ;; when:
        post-response (handler (-> (mock/request :post "/api/v1/ad")
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


(t/deftest ad-matching-by-channel
  ;; given
  (let [handler   adserver/handler
        post-ad   (fn [ad] (let [response (handler (-> (mock/request :post "/api/v1/ad")
                                                       (mock/json-body ad)))]
                             (do
                               (t/testing "Sanity check when posting example")
                               (t/is (= 201 (:status response))))))]
    (test.chuck/checking
     "Checking filter ads by channel"
     10
     [some-ads  (gen/vector (shadow-generator (s/gen ::ad/ad) {:channel "bar"}) 6)
      noise-ads (gen/vector (gen/such-that (fn [{:keys [channel]}] (not= "bar" channel))
                                           (s/gen ::ad/ad)) 10)]
     (reset! sut/ad-map {}) ;; hack! reset ads each run
     (doseq [ad (clojure.set/union some-ads noise-ads)]
       (post-ad ad))

     ;; when:
     (let [response     (handler (-> (mock/request :get "/api/v1/ad?channel=bar")))
           response-ads (parse-body (:body response))]

       ;; then:
       (t/is (= 200 (:status response)))
       (t/is (= 6   (count response-ads)) "only bar channel ads")
       (t/is (empty (remove (fn [{:keys [channel]}] (= "bar" channel)) response-ads)))))))


(t/deftest ad-matching-by-locale
  ;; given
  (let [handler   adserver/handler
        post-ad   (fn [ad] (let [response (handler (-> (mock/request :post "/api/v1/ad")
                                                       (mock/json-body ad)))]
                             (do
                               (t/testing "Sanity check when posting example")
                               (t/is (= 201 (:status response))))))]
    (test.chuck/checking
     "Checking filter ads by locale"
     10
     [some-ads  (gen/vector (shadow-generator (s/gen ::ad/ad) {:locale "pl_PL"}) 3)
      noise-ads (gen/vector (gen/such-that (fn [{:keys [locale]}] (not= "pl_PL" locale))
                                           (s/gen ::ad/ad)) 10)]
     (reset! sut/ad-map {}) ;; hack! reset ads each run
     (doseq [ad (clojure.set/union some-ads noise-ads)]
       (post-ad ad))

     ;; when:
     (let [response     (handler (-> (mock/request :get "/api/v1/ad?locale=pl_PL")))
           response-ads (parse-body (:body response))]

       ;; then:
       (t/is (= 200 (:status response)))
       (t/is (= 3   (count response-ads)) "only pl_PL locale ads")
       (t/is (empty (remove (fn [{:keys [locale]}] (= "pl_PL" locale)) response-ads)))))))


(t/deftest ad-limit-of-views
  ;; given
  (let [handler   adserver/handler
        post-ad   (fn [ad] (let [response (handler (-> (mock/request :post "/api/v1/ad")
                                                       (mock/json-body ad)))]
                             (do
                               (t/testing "Sanity check when posting example")
                               (t/is (= 201 (:status response)))
                               (get-in response [:headers "Location"]))))]
    (test.chuck/checking
     "Checking filter ads by limit-of-viewes"
     10
     [some-ads  (gen/vector (shadow-generator (s/gen ::ad/ad) {:lov 1}) 4)
      noise-ads (gen/vector (shadow-generator (s/gen ::ad/ad) {:lov 1000}) 20)]
     (reset! sut/ad-map {}) ;; hack! reset ads each run

     (let [ad-responses (doall (map post-ad (clojure.set/union some-ads noise-ads)))]

       ;; when:
       ;; view all ads, once
       (doseq [ad-post-response-path ad-responses]
         (let [ad-resp  (handler (-> (mock/request :get (str "/api/v1" ad-post-response-path))))
               ad       (parse-body (:body ad-resp))
               id       (nth (clojure.string/split ad-post-response-path #"/") 2) ;; /ad/id
               channel  (:channel ad)
               response (handler (-> (mock/request :post "/api/v1/ad-view")
                                     (mock/json-body {:ad-id   id
                                                      :channel channel})))]
           (t/is (= 201 (:status response))))))

     ;; and when:
     (let [response     (handler (-> (mock/request :get "/api/v1/ad?available=1")))
           response-ads (parse-body (:body response))]

       ;; then:
       (t/is (= 200 (:status response)))
       (t/is (= 20  (count response-ads)) "only available ads")))))


(t/deftest ad-just-one
  ;; given
  (let [handler   adserver/handler
        post-ad   (fn [ad] (let [response (handler (-> (mock/request :post "/api/v1/ad")
                                                       (mock/json-body ad)))]
                             (do
                               (t/testing "Sanity check when posting example")
                               (t/is (= 201 (:status response)))
                               (get-in response [:headers "Location"]))))]
    (test.chuck/checking
     "Checking filter ads by limit-of-viewes"
     10
     [some-ads  (gen/vector (s/gen ::ad/ad) 4)]
     (reset! sut/ad-map {}) ;; hack! reset ads each run
     (doseq [ad some-ads]
       (post-ad ad))

     ;; and when:
     (let [response     (handler (-> (mock/request :get "/api/v1/ad?limit=1")))
           response-ads (parse-body (:body response))]

       ;; then:
       (t/is (= 200 (:status response)))
       (t/is (= 1   (count response-ads)) "only one, any")))))


(comment
  ;; Failed test case:
  (let [handler   adserver/handler
        post-ad   (fn [ad] (let [response (handler (-> (mock/request :post "/api/v1/ad")
                                                       (mock/json-body ad)))]
                             (do
                               (t/testing "Sanity check when posting example")
                               (t/is (= 201 (:status response))))))]
    (let [some-ads [{:channel "", :locale "pl_PL", :country "", :device "", :interests []}
                    {:channel "", :locale "pl_PL", :country "", :device "", :interests []}
                    {:channel "", :locale "pl_PL", :country "", :device "", :interests []}]
        noise-ads [{:channel "", :locale "", :country "", :device "", :interests []} {:channel "", :locale "", :country "", :device "", :interests []} {:channel "", :locale "", :country "", :device "", :interests []} {:channel "", :locale "", :country "", :device "", :interests []} {:channel "", :locale "", :country "", :device "", :interests []} {:channel "", :locale "", :country "", :device "", :interests []} {:channel "", :locale "", :country "", :device "", :interests []} {:channel "", :locale "", :country "", :device "", :interests []} {:channel "", :locale "", :country "", :device "", :interests []} {:channel "", :locale "", :country "", :device "", :interests []}]]

    (reset! sut/ad-map {}) ;; hack! reset ads each run
    #_(doseq [ad (clojure.set/union some-ads noise-ads)]
      (post-ad ad))
    #_(post-ad {:channel "", :locale "pl_PL", :country "", :device "", :interests []}) 

    (let [ad       {:channel "", :locale "pl_PL", :country "", :device "", :interests []}
          response (handler (-> (mock/request :post "/api/v1/ad")
                                (mock/json-body ad)))]
      (parse-body (:body response))) 


    ;; when:
    #_(let [response     (handler (-> (mock/request :get "/api/v1/ad")))
          response-ads (parse-body (:body response))]

      ;; then:
      @sut/ad-map 
      #_response
      #_(t/is (= 200 (:status response)))
      #_(t/is (= 3   (count response-ads)) "only pl_PL locale ads")
      #_(t/is (empty (remove (fn [{:keys [channel]}] (= "pl_PL" channel)) response-ads))))

    ))


  ;; Reduced to:
  (let [ads [{:locale "", :channel "", :country ""} {:locale "", :channel "", :country ""} {:locale "", :channel "", :country ""} {:locale "pl_PL", :channel "", :country ""} {:locale "pl_PL", :channel "", :country ""} {:locale "", :channel "", :country ""} {:locale "", :channel "", :country ""} {:locale "", :channel "", :country ""} {:locale "", :channel "", :country ""} {:locale "", :channel "", :country ""} {:locale "", :channel "", :country ""} {:locale "", :channel "", :country ""} {:locale "pl_PL", :channel "", :country ""}]
        channel ""
        locale  "pl_PL"]


    (into []
          (filter (fn [{a-channel :channel
                        a-locale  :locale
                        :as ad}]
                    (and
                     (if (nil? channel) true (= a-channel channel))
                     (if (nil? locale)  true (= a-locale  locale)))))
          ads)) 
  ) 


(comment
  ;; Generator helper, that would generate specific ads
  (gen/sample (s/gen ::ad/ad)) 
  (gen/sample (shadow-generator (s/gen ::ad/request) {:channel "bar" :locale "pl"})) 

  (not-empty (filter (fn [{:keys [channel]}] (= "bar" channel)) [{:channel "foo"} {:channel "foo"}])) 

  (->>
   (gen/generate (gen/vector (shadow-generator (s/gen ::ad/request) {:channel "bar"}) 10)) 
   (clojure.set/union
    (gen/generate (gen/vector (gen/such-that (fn [{:keys [channel]}] (not= "bar" channel))
                                                 (s/gen ::ad/request)))) ) 
   (map :channel)
   ) 




  )

