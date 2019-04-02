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


(defn available? [{:keys [lov views]:as ad}]
  (< (or views 0) (or lov 0)))


(defn find-all-ads-by [{:keys [channel locale available limit] :as params}]
  (if (and (not channel) (not locale) (not available) (not limit))
    (vals @ad-map) ;; just return it all, without even bothering to filter
    (into []
          (comp
           (filter (fn [{a-channel :channel
                        a-locale  :locale
                        :as ad}]
                    (and
                     (if (false? available) true (available? ad))
                     (if (nil? channel)     true (= a-channel channel))
                     (if (nil? locale)      true (= a-locale  locale)))))
           (if (nil? limit) identity (take limit)))
          (vals @ad-map))))


(defn new-ad-view [{:keys [ad-id] :as ad-view}]
  (let [ad-view-id (uuid)]
    ;; Store views-events separately? For now just modify the common aggregate
    (swap! ad-map update-in [ad-id :views] (fnil inc 0))
    (str "/ad-view/" ad-view-id)))

(defn parse-int [s]
  (try
    (Integer/parseInt s)
    (catch Exception e
        nil)))

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
                                                     ::ad/locale
                                                     ::ad/available
                                                     ::ad/limit])}
         :responses  {200 {:schema (s/coll-of ::ad/ad)}}
         :handler    (fn [{{:keys [channel locale available limit]} :query-params}]
                       (if-let [ads (find-all-ads-by {:channel   channel
                                                      :locale    locale
                                                      :available (= "1" available)
                                                      :limit     (parse-int limit)})]
                         (ok ads)
                         (not-found)))}})))
  (context "/ad-view" []
    :tags     ["ad-view"]
    :coercion :spec

    (context "/" []
      (resource
       {:post
        {:parameters {:body-params ::ad/view}
         :handler    (fn [{ad-view :body-params :as req}]
                       (created (new-ad-view ad-view)))}}))))

(comment
  (uuid)
  (count @ad-map)
  (reset! ad-map {}) 
  )
