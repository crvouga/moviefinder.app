(ns app.media.media-db.impl
  (:require
   [app.media.media-db.impl-dual-source.impl]
   [app.media.media-db.impl-fake.impl]
   [app.media.media-db.impl-tmdb-api.impl]
   [clojure.core.async :refer [go]]
   [app.media.media-db.inter :as media-db]
   [lib.query-result :as query-result]))

(defmethod media-db/init :default [_]
  _)

(defmethod media-db/query! :default [inst q]
  (go
    (-> query-result/init
        (assoc
         :query-result/primary-key :media/id
         :result/type :result/err
         :err/err :media-db/impl-not-found
         :err/data (:media-db/impl inst)
         :query-result/query q))))

(defmethod media-db/put! :default [_ media]
  (go
    (-> {}
        (assoc :result/type :result/err
               :err/err :media-db/impl-not-found
               :err/data media))))



