(ns moviefinder-app.media.media-db.impl-tmdb-api.query-plan-item.movie-details
  (:require [clojure.core.async :refer [go <!]]
            [core.tmdb-api.configuration]
            [core.tmdb-api.movie-details]
            [moviefinder-app.media.media-db.impl-tmdb-api.mapping :as mapping]
            [moviefinder-app.media.media-db.impl-tmdb-api.query-plan-item :as query-plan-item]))

(defmethod query-plan-item/query-result-chan! :tmdb-query-plan-item/movie-details [query-plan-item]
  (go
    (let [q (second query-plan-item)
          where (:query/where q)
          movie-id (get where 2) ; Get the third element which is the ID value
          params (merge q {:tmdb/language "en-US"})
          configuration-response (<! (core.tmdb-api.configuration/fetch-chan! params))
          movie-details-response (<! (core.tmdb-api.movie-details/fetch-chan! movie-id params))
          response (merge params
                          movie-details-response
                          configuration-response
                          {:tmdb/results [movie-details-response]
                           :tmdb/total-results 1})
          query-result (mapping/tmdb-result->query-result response)]
      query-result)))
