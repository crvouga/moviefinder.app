(ns moviefinder-app.media.media-db.impl-tmdb-api.impl
  (:require [clojure.core.async :refer [go <!]]
            [core.tmdb-api.configuration]
            [core.tmdb-api.discover-movie]
            [moviefinder-app.media.media-db.interface :as media-db]
            [clojure.set :refer [rename-keys]]))

(defn- tmdb-movie->media [movie]
  (rename-keys movie {:tmdb/id :media/id
                      :tmdb/title :media/title
                      :tmdb/release-date :media/release-date
                      :tmdb/overview :media/overview
                      :tmdb/poster-path :media/poster-path
                      :tmdb/backdrop-path :media/backdrop-path
                      :tmdb/vote-average :media/vote-average
                      :tmdb/vote-count :media/vote-count
                      :tmdb/popularity :media/popularity}))

(defn assoc-image-urls [config movie]
  (assoc movie
         :media/poster-url (core.tmdb-api.configuration/to-poster-url config (:media/poster-path movie))
         :media/backdrop-url (core.tmdb-api.configuration/to-backdrop-url config (:media/backdrop-path movie))))

(defn to-query-result [input]
  (let [total (:tmdb/total-results input)
        limit (-> input :query/limit (or 25))
        offset (-> input :query/offset (or 0))
        items (->> (:tmdb/results input)
                   (map tmdb-movie->media)
                   (map #(assoc-image-urls input %)))]
    {:query-result/query input
     :query-result/limit limit
     :query-result/offset offset
     :query-result/total total
     :query-result/primary-key :media/id
     :query-result/rows items}))

(defmethod media-db/query-result-chan! :media-db-impl/tmdb-api [q]
  (go
    (let [params (merge q {:tmdb/language "en-US"
                           :tmdb/sort-by "popularity.desc"
                           :tmdb/include-adult false
                           :tmdb/include-video false
                           :tmdb/page 1})
          configuration-response (<! (core.tmdb-api.configuration/fetch-chan! q))
          discover-movie-response (<! (core.tmdb-api.discover-movie/fetch-chan! params))
          response (merge params discover-movie-response configuration-response)
          query-result (to-query-result response)]
      query-result)))
