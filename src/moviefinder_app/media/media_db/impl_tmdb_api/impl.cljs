(ns moviefinder-app.media.media-db.impl-tmdb-api.impl
  (:require [clojure.core.async :refer [go <!]]
            [moviefinder-app.media.media-db.interface :as media-db]
            [core.tmdb-api.discover-movie :as discover-movie]
            [clojure.set :refer [rename-keys]]))

(defn- tmdb-movie->media [movie]
  (rename-keys movie {:tmdb/id :media/id
                      :tmdb/title :media/title
                      :tmdb/release-date :media/release-date
                      :tmdb/overview :media/overview
                      :tmdb/poster-path :media/poster-path
                      :tmdb/vote-average :media/vote-average
                      :tmdb/vote-count :media/vote-count
                      :tmdb/popularity :media/popularity}))

(defn to-query-result [q response]
  (let [total (:tmdb/total-results response)
        limit (-> q :query/limit (or 25))
        offset (-> q :query/offset (or 0))
        items (->> (:tmdb/results response)
                   (map tmdb-movie->media))]
    (-> q
        (merge {:query-result/query q
                :query-result/limit limit
                :query-result/offset offset
                :query-result/total total
                :query-result/primary-key :media/id
                :query-result/rows items}))))

(defmethod media-db/query-result-chan! :media-db-impl/tmdb-api [q]
  (go
    (let [params {:tmdb/api-key (:tmdb/api-key q)
                  :tmdb/language "en-US"
                  :tmdb/sort-by "popularity.desc"
                  :tmdb/include-adult false
                  :tmdb/include-video false
                  :tmdb/page 1}
          response (<! (discover-movie/fetch-chan! params))
          query-result (to-query-result q response)]
      query-result)))
