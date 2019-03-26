(ns adserver.ad-handler
  (:require [compojure.api.sweet     :as api :refer [POST GET context defroutes]]
            [clojure.tools.logging   :as log]
            [ring.util.http-response :as http-response :refer [created ok not-found]]
            [adserver.ad             :as ad]
            [spec-tools.core         :as st]
            [clojure.spec.alpha      :as s]))

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


(defn find-all-ads-by [{:keys [channel] :as params}]
  (into []
        (filter (fn [{a-channel :channel :as ad}] (= a-channel channel)) (vals @ad-map))))


(defroutes routes
  (context "/ad" []
    :tags     ["ad"]
    :coercion :spec

    (POST "/" []
      :body   [ad ::ad/ad]
      (created (new-ad ad)))

    (GET "/:id" [id]
      :return ::ad/ad
      (if-let [an-ad (find-ad-by-id id)]
        (ok an-ad)
        (not-found)))

    (GET "/" []
      :query-params [channel :- ::ad/channel]
      :return       (s/coll-of ::ad/ad)
      (if-let [ads (find-all-ads-by {:channel channel})]
        (ok ads)
        (not-found)))))


(comment
  (uuid)
  (count @ad-map) 
  (reset! ad-map {}) 
  )
