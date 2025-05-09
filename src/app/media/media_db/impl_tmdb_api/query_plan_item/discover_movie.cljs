(ns app.media.media-db.impl-tmdb-api.query-plan-item.discover-movie
  (:require [clojure.core.async :refer [go <!]]
            [lib.tmdb-api.configuration]
            [lib.tmdb-api.discover-movie]
            [lib.tmdb-api.movie-details]
            [app.media.media-db.impl-tmdb-api.mapping :as mapping]
            [app.media.media-db.impl-tmdb-api.query-plan-item :as query-plan-item]))


(defmethod query-plan-item/query-result-chan! :tmdb-query-plan-item/discover-movie [query-plan-item ctx]
  (go
    (let [q (second query-plan-item)
          _limit (-> q :query/limit (or 25))
          offset (-> q :query/offset (or 0))
          page (inc (quot offset 20)) ; TMDB uses 1-based page numbers, each page has 20 items
          params (merge ctx {:tmdb/language "en-US"
                             :tmdb/sort-by "popularity.desc"
                             :tmdb/include-adult false
                             :tmdb/include-video false
                             :tmdb/page page})
          configuration-response (<! (lib.tmdb-api.configuration/fetch-chan! params))
          discover-movie-response (<! (lib.tmdb-api.discover-movie/fetch-chan! params))
          response (merge params discover-movie-response configuration-response)
          query-result (mapping/tmdb-result->query-result response)]
      query-result)))
