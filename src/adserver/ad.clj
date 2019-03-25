(ns adserver.ad
  (:require [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as gen]))

(s/def ::channel string?)
(s/def ::ad-id string?)
(s/def ::ad-ids (s/coll-of ::ad-id :kind vector?))
(s/def ::locale (s/with-gen
                 (s/and string? (fn [x] (= 5 (count x))))
                 #(gen/fmap (fn [chars] (apply str chars))
                            (gen/vector (gen/char) 5))))
(s/def ::country (s/with-gen
                   (s/and string? (fn [x] (= 2 (count x))))
                   #(gen/fmap (fn [chars] (apply str chars))
                                (gen/vector (gen/char) 2))))
(s/def ::device string?)
(s/def ::interest (s/with-gen
                    (s/and string? (fn [x] (re-matches #"IAB.*" x)))
                    #(gen/fmap (fn [x] (str "IAB" x))
                               (gen/string))))
(s/def ::interests (s/coll-of ::interest :kind vector?))


;; Needs a better domain name, it could've been named `bannana` for now
(s/def ::request (s/keys :req-un [::channel ::locale ::country ::device ::interests]
                         :opt-un [::ad-ids]))

(s/def ::ad-content-url string?)

(s/def ::ad-creative (s/keys :req-un [::ad-id
                                      ::ad-content-url]))

;; Figure out a better name
(s/def ::response (s/coll-of ::ad-creative :kind vector?))
