(ns app.media.media-db.impl-db.impl
  (:require
   [app.media.media-db.inter :as media-db]
   [clojure.core.async :refer [go <!]]
   [clojure.set]
   [lib.db.impl]
   [lib.db.inter :as db]
   [app.media.media-db.impl-db.migrations :as migrations]))


(defn- media->row [media]
  (clojure.set/rename-keys media
                           {:media/id :id
                            :media/title :title
                            :media/release-date :release_date
                            :media/overview :overview
                            :media/poster-path :poster_path
                            :media/vote-average :vote_average
                            :media/vote-count :vote_count
                            :media/popularity :popularity
                            :media/poster-url :poster_url
                            :media/backdrop-url :backdrop_url}))

(defn- row->media [row]
  (clojure.set/rename-keys row
                           {:id :media/id
                            :title :media/title
                            :release_date :media/release-date
                            :overview :media/overview
                            :poster_path :media/poster-path
                            :vote_average :media/vote-average
                            :vote_count :media/vote-count
                            :popularity :media/popularity
                            :poster_url :media/poster-url
                            :backdrop_url :media/backdrop-url}))

(defn- run-migrations! [config]
  (go
    (doseq [migration migrations/migrations]
      (<! (db/query-chan! (merge config {:db/query migration}))))))

(defmethod media-db/upsert-chan! :media-db/impl-db
  [{:keys [media/entity] :as config}]
  (go
    (<! (run-migrations! config))
    (let [row (media->row entity)
          _result (<! (db/query-chan!
                       (merge config
                              {:db/query {:insert-into :media
                                          :columns (keys row)
                                          :values [(vals row)]}})))]
      {:media-db/impl :media-db/impl-db
       :result/type :result/ok})))

(defmethod media-db/query-result-chan! :media-db/impl-db
  [{:keys [query/where query/limit query/offset query/select query/order] :as config}]
  (go
    (<! (run-migrations! config))
    (let [limit (or limit 25)
          offset (or offset 0)
          count-result (<! (db/query-chan!
                            (merge config
                                   {:db/query {:select [[:%count.* :total]]
                                               :from [:media]
                                               :where (or where [])}})))
          total (get-in count-result [:db/rows 0 :total])
          result (<! (db/query-chan!
                      (merge config
                             {:db/query {:select (or select [:*])
                                         :from [:media]
                                         :where (or where [])
                                         :order-by (or order [[:media/id :desc]])
                                         :limit limit
                                         :offset offset}})))
          rows (map row->media (:db/rows result))
          query-result {:queried/query config
                        :queried/rows rows
                        :query-result/limit limit
                        :query-result/offset offset
                        :query-result/total total
                        :query-result/primary-key :media/id}]
      query-result)))
