(ns adserver.ad
  (:require [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as gen]
            [spec-tools.core        :as st]))


;; TODO: a macro, or move it to coercion code?
(defn with-example [spec]
  (st/spec {:spec spec
            :json-schema/example (gen/generate (s/gen spec))}))


(s/def ::channel (with-example string?))
(s/def ::ad-id   (with-example string?))
(s/def ::ad-ids  (with-example (s/coll-of ::ad-id :kind vector?)))
(s/def ::locale  (with-example (s/with-gen
                                 (s/and string? (fn [x] (= 5 (count x))))
                                 #(gen/fmap (fn [chars] (apply str chars))
                                            (gen/vector (gen/char) 5)))))
(s/def ::country (with-example
                   (s/with-gen
                     (s/and string? (fn [x] (= 2 (count x))))
                     #(gen/fmap (fn [chars] (apply str chars))
                                (gen/vector (gen/char) 2)))))
(s/def ::device   (with-example string?))
(s/def ::interest (with-example
                    (s/with-gen
                      (s/and string? (fn [x] (re-matches #"IAB.*" x)))
                      #(gen/fmap (fn [x] (str "IAB" x))
                                 (gen/string)))))
(s/def ::interests (with-example
                     (s/coll-of ::interest :kind vector?)))


;; Needs a better domain name, it could've been named `bannana` for now
(s/def ::request (s/keys :req-un [::channel ::locale ::country ::device ::interests]
                         :opt-un [::ad-ids]))

(s/def ::ad      (s/keys :req-un [::channel ::locale ::country]
                         :opt-un [::ad-id]))

(s/def ::ad-content-url (with-example string?))

(s/def ::ad-creative (s/keys :req-un [::ad-id
                                      ::ad-content-url]))

;; Figure out a better name
(s/def ::response (with-example
                    (s/coll-of ::ad-creative :kind vector?)))
