(ns adserver.ad-request
  (:require [clojure.spec.alpha :as s]))

(s/def ::channel string?)
(s/def ::ad-id string?)
(s/def ::ad-ids (s/coll-of ::ad-id :kind vector?))
(s/def ::locale (s/and string? (fn [x] (= 5 (count x)))))
(s/def ::country (s/and string? (fn [x] (= 2 (count x)))))
(s/def ::device string?)
(s/def ::interest (s/and string? (fn [x] (re-matches #"IAB.*" x))))
(s/def ::interests (s/coll-of ::interest :kind vector?))
(s/def ::ad-request (s/keys :req-un [::channel ::locale ::country ::device ::interests]
                            :opt-un [::ad-ids]))

