(ns adserver.ad-handler
  (:require [compojure.api.sweet     :as api :refer [POST context defroutes]]
            [clojure.tools.logging   :as log]
            [ring.util.http-response :as http-response :refer [created]]
            [adserver.ad             :as ad]
            [spec-tools.core         :as st]))


(defn new-ad [ad]
  (let [ad-id "1"]
    (log/trace "Creating ad"
               {:ad ad})
    {:ad-id ad-id}))


(defroutes routes
  (context "/ad" []
    :tags     ["ad"]
    :coercion :spec

    (POST "/" []
      :body   [ad ::ad/request]
      :return ::ad/response
      (created (new-ad ad)))))
