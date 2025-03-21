(ns app.media.media-db.impl-tmdb-api.mapping
  (:require
   [core.tmdb-api.configuration]
   [core.tmdb-api.discover-movie]
   [core.tmdb-api.movie-details]
   [clojure.set :refer [rename-keys]]))

(defn- tmdb-item->media [movie]
  (rename-keys movie {:tmdb/id :media/id
                      :tmdb/title :media/title
                      :tmdb/release-date :media/release-date
                      :tmdb/overview :media/overview
                      :tmdb/poster-path :media/poster-path
                      :tmdb/backdrop-path :media/backdrop-path
                      :tmdb/vote-average :media/vote-average
                      :tmdb/vote-count :media/vote-count
                      :tmdb/popularity :media/popularity}))

(defn- assoc-image-urls [config movie]
  (assoc movie
         :media/poster-url (core.tmdb-api.configuration/to-poster-url config (:media/poster-path movie))
         :media/backdrop-url (core.tmdb-api.configuration/to-backdrop-url config (:media/backdrop-path movie))))

(defn tmdb-result->query-result [input]
  (let [total (:tmdb/total-results input)
        limit (-> input :query/limit (or 25))
        offset (-> input :query/offset (or 0))
        items (->> (:tmdb/results input)
                   (map tmdb-item->media)
                   (map #(assoc-image-urls input %))
                   (drop offset)
                   (take limit))]
    {:query-result/query (select-keys input [:query/where :query/limit :query/offset :query/order :query/select])
     :query-result/limit limit
     :query-result/offset offset
     :query-result/total total
     :query-result/primary-key :media/id
     :query-result/rows items}))


