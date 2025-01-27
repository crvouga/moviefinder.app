(ns moviefinder-app.media.media-db.interface
  (:require [cljs.spec.alpha :as s]
            [clojure.core.async :refer [go]]
            [moviefinder-app.media.media]))


(s/def :query/where vector?)
(s/def :query/limit number?)
(s/def :query/offset number?)
(s/def :query/select (s/coll-of keyword?))
(s/def :query/order vector?)

(s/def :query-result/query (s/keys :opt [:query/select :query/where :query/order :query/limit :query/offset]))
(s/def :query-result/limit number?)
(s/def :query-result/offset number?)
(s/def :query-result/total number?)
(s/def :query-result/primary-key keyword?)
(s/def :query-result/rows (s/coll-of :media/entity))

(s/def :query-result/query-result
  (s/keys :req [:query-result/query
                :query-result/limit
                :query-result/offset
                :query-result/total
                :query-result/primary-key
                :query-result/rows]))

(defmulti query-result-chan! :media-db/impl)


(def empty-query-result
  {:query-result/limit 25
   :query-result/offset 0
   :query-result/total 0
   :query-result/primary-key :media/id
   :query-result/rows []})

(defmethod query-result-chan! :default [q]
  (go
    (assoc empty-query-result
           :error/message "Media db implementation not found"
           :error/data q
           :query-result/query q
           :query-result/rows [])))