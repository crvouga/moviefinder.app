(ns app.media.media-db.inter
  (:require
   [app.media.media]
   [cljs.spec.alpha :as s]
   [clojure.core.async :refer [go]]
   [lib.query-result :as query-result]))





(s/def :query/where vector?)
(s/def :query/limit number?)
(s/def :query/offset number?)
(s/def :query/select (s/coll-of keyword?))
(s/def :query/order vector?)





(defmulti init
  "Used for creating a new media db implementation"
  :media-db/impl)

(defmethod init :default [q]
  q)

(defmulti query!
  "Used for querying media data from the database using query spec"
  :media-db/impl)

(defmethod query! :default [q]
  (go
    (assoc query-result/init
           :query-result/primary-key :media/id
           :error/message "Media db implementation not found"
           :error/data q
           :query-result/query q)))

(defmulti upsert-chan!
  "Used for inserting media data into the database"
  :media-db/impl)

(defmethod upsert-chan! :default [q]
  (go
    (assoc q
           :result/type :result/err
           :error/message "Media db implementation not found"
           :error/data q)))

