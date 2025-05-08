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
(s/def :query-result/limit number?)
(s/def :query-result/offset number?)
(s/def :query-result/total number?)
(s/def :query-result/primary-key keyword?)
(s/def :query-result/rows (s/coll-of map?))

(s/def :query-result/query-result
  (s/keys :req [:queried/query
                :query-result/limit
                :query-result/offset
                :query-result/total
                :query-result/primary-key
                :query-result/rows]))

(def empty-query-result
  {:query-result/limit 25
   :query-result/offset 0
   :query-result/total 0
   :query-result/primary-key :media/id
   :query-result/rows []})



(defmulti new!
  "Used for creating a new media db implementation"
  :media-db/impl)

(defmethod new! :default [q]
  q)

(defmulti query!
  "Used for querying media data from the database using query spec"
  :media-db/impl)

(defmethod query! :default [q]
  (go
    (assoc empty-query-result
           :error/message "Media db implementation not found"
           :error/data q
           :queried/query q
           :query-result/rows [])))

(defmulti upsert-chan!
  "Used for inserting media data into the database"
  :media-db/impl)

(defmethod upsert-chan! :default [q]
  (go
    (assoc q
           :result/type :result/err
           :error/message "Media db implementation not found"
           :error/data q)))

