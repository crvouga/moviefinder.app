(ns app.media.media-db.impl-db-conn.impl
  (:require
   [app.media.media-db.interface :as media-db]
   [cljs.core.async :refer [go <!]]
   [clojure.set]
   [core.db-conn.impl]
   [core.db-conn.interface :as db-conn]
   [app.media.media-db.impl-db-conn.migrations :as migrations]))


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
      (<! (db-conn/query-chan! (merge config {:db-conn/query migration}))))))

(defmethod media-db/put-chan! :media-db-impl/db-conn
  [{:keys [media/entity] :as config}]
  (go
    (<! (run-migrations! config))
    (let [row (media->row entity)
          _result (<! (db-conn/query-chan!
                       (merge config
                              {:db-conn/query {:insert-into :media
                                               :columns (keys row)
                                               :values [(vals row)]}})))]
      {:media-db/impl :media-db-impl/db-conn
       :result/type :result/ok})))

(defmethod media-db/query-result-chan! :media-db-impl/db-conn
  [{:keys [query/where query/limit query/offset query/select query/order] :as config}]
  (go
    (<! (run-migrations! config))
    (let [limit (or limit 25)
          offset (or offset 0)
          count-result (<! (db-conn/query-chan!
                            (merge config
                                   {:db-conn/query {:select [[:%count.* :total]]
                                                    :from [:media]
                                                    :where (or where [])}})))
          total (get-in count-result [:db-conn/rows 0 :total])
          result (<! (db-conn/query-chan!
                      (merge config
                             {:db-conn/query {:select (or select [:*])
                                              :from [:media]
                                              :where (or where [])
                                              :order-by (or order [[:media/id :desc]])
                                              :limit limit
                                              :offset offset}})))
          rows (map row->media (:db-conn/rows result))
          query-result {:query-result/query config
                        :query-result/rows rows
                        :query-result/limit limit
                        :query-result/offset offset
                        :query-result/total total
                        :query-result/primary-key :media/id}]
      query-result)))
