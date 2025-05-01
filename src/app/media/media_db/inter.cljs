(ns app.media.media-db.inter
  (:require [cljs.spec.alpha :as s]
            [clojure.core.async :refer [go]]
            [app.media.entity]))





(s/def :query/where vector?)
(s/def :query/limit number?)
(s/def :query/offset number?)
(s/def :query/select (s/coll-of keyword?))
(s/def :query/order vector?)

(s/def :queried/query (s/keys :opt [:query/select :query/where :query/order :query/limit :query/offset]))
(s/def :queried/limit number?)
(s/def :queried/offset number?)
(s/def :queried/total number?)
(s/def :queried/primary-key keyword?)
(s/def :queried/rows (s/coll-of :media/entity))

(s/def :query-result/query-result
  (s/keys :req [:queried/query
                :queried/limit
                :queried/offset
                :queried/total
                :queried/primary-key
                :queried/rows]))

(def empty-query-result
  {:queried/limit 25
   :queried/offset 0
   :queried/total 0
   :queried/primary-key :media/id
   :queried/rows []})



(defmulti new!
  "Used for creating a new media db implementation"
  :media-db/impl)

(defmethod new! :default [q]
  q)

(defmulti query-result-chan!
  "Used for querying media data from the database using query spec"
  :media-db/impl)

(defmethod query-result-chan! :default [q]
  (go
    (assoc empty-query-result
           :error/message "Media db implementation not found"
           :error/data q
           :queried/query q
           :queried/rows [])))

(defmulti upsert-chan!
  "Used for inserting media data into the database"
  :media-db/impl)

(defmethod upsert-chan! :default [q]
  (go
    (assoc q
           :result/type :result/err
           :error/message "Media db implementation not found"
           :error/data q)))

