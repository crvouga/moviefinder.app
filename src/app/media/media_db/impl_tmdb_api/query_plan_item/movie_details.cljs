(ns app.media.media-db.impl-tmdb-api.query-plan-item.movie-details
  (:require
   [app.media.media-db.impl-tmdb-api.mapping :as mapping]
   [app.media.media-db.impl-tmdb-api.query-plan-item :as query-plan-item]
   [clojure.core.async :refer [<! go]]
   [lib.result :as result]
   [lib.tmdb-api.configuration]
   [lib.tmdb-api.movie-details]))

(defmethod query-plan-item/result-chan! :tmdb-query-plan-item/movie-details
  [[_ {:keys [tmdb/id]}] ctx]
  (go
    (let [tmdb-params (merge ctx {:tmdb/language "en-US"})
          tmdb-config (<! (lib.tmdb-api.configuration/fetch! tmdb-params))
          tmdb-movie-details (<! (lib.tmdb-api.movie-details/fetch! id tmdb-params))
          tmdb-result {:tmdb/results [tmdb-movie-details]
                       :tmdb/total-results 1}
          tmdb-res (merge tmdb-params tmdb-result tmdb-config)
          query-result (mapping/tmdb-result->query-result tmdb-res)
          result (merge result/ok query-result)]
      result)))
