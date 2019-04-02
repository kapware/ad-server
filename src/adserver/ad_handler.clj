(ns adserver.ad-handler
  (:require [compojure.api.sweet     :as api :refer [context defroutes resource]]
            [clojure.tools.logging   :as log]
            [ring.util.http-response :as http-response :refer [created ok not-found]]
            [adserver.ad             :as ad]
            [spec-tools.core         :as st]
            [clojure.spec.alpha      :as s]
            [spec-tools.data-spec    :as ds]))

(defn uuid [] (.toString (java.util.UUID/randomUUID)))

(def ad-map (atom {}))

(defn new-ad [ad]
  (let [ad-id (uuid)]
    (log/trace "Creating ad" {:ad ad})
    (swap! ad-map assoc ad-id ad)
    (str "/ad/" ad-id)))


(defn find-ad-by-id [id]
  (let [ad (get @ad-map id)]
    (log/trace "Retrieving ad " {:id id :ad ad})
    ad))


(defn find-all-ads-by [{:keys [channel locale] :as params}]
  (if (and (not channel) (not locale))
    (vals @ad-map) ;; just return it all, without even bothering to filter
    (into []
          (filter (fn [{a-channel :channel
                        a-locale  :locale
                        :as ad}]
                    (and
                     (if (nil? channel) true (= a-channel channel))
                     (if (nil? locale)  true (= a-locale  locale)))))
          (vals @ad-map))))


(defroutes routes
  (context "/ad" []
    :tags     ["ad"]
    :coercion :spec

    (context "/:id" [id]
      (resource
      {:get
       {:responses {200 {:schema ::ad/ad}}
        :handler   (fn [{:as req}]
                     (if-let [an-ad (find-ad-by-id id)]
                       (ok an-ad)
                       (not-found)))}}))

    (context "/" []
      (resource
       {:post
        {:parameters {:body-params ::ad/ad}
         :handler    (fn [{ad :body-params :as req}]
                       (created (new-ad ad)))}
        :get
        {:parameters {:query-params (s/keys :opt-un [::ad/channel
                                                     ::ad/locale])}
         :responses  {200 {:schema (s/coll-of ::ad/ad)}}
         :handler    (fn [{{:keys [channel locale]} :query-params}]
                       (if-let [ads (find-all-ads-by {:channel channel
                                                      :locale  locale})]
                         (ok ads)
                         (not-found)))}}))))

(comment
  (uuid)
  (count @ad-map)
  (reset! ad-map {}) 
  )
