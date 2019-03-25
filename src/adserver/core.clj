(ns adserver.core
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [mount.core            :as mount :refer [defstate]]
            [org.httpkit.server    :as httpkit]
            [adserver.ad-handler   :as ad-handler]
            [compojure.api.sweet   :as api :refer [api context]]))


(def handler
  (api
   {:swagger
    {:ui   "/"
     :spec "/swagger.json"
     :data {:info {:title "Ad server api"
                   :description "Ad server api"}
            :tags [{:name "api", :description "ad apis"}]}}}
   (context "/api/v1" []
                :tags ["api"]
                :coercion :spec
                ad-handler/routes)))


(defn start-webservice []
  (let [port 8080] ;; TODO: configure from env
    (log/info (str "Starting web, port :" port))
    (httpkit/run-server handler {:port port})))


(defstate webservice
  :start (start-webservice)
  ;; (start-webservice) is expected to return a function to close webservice
  :stop (webservice))


(defn -main [& args]
  (mount/start
   #'adserver.core/webservice))
