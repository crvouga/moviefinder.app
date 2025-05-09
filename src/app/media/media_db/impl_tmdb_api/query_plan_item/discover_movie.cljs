(ns app.media.media-db.impl-tmdb-api.query-plan-item.discover-movie
  (:require
   [app.media.media-db.impl-tmdb-api.mapping :as mapping]
   [app.media.media-db.impl-tmdb-api.query-plan-item :as query-plan-item]
   [clojure.core.async :refer [<! go]]
   [lib.result :as result]
   [lib.tmdb-api.configuration]
   [lib.tmdb-api.discover-movie]
   [lib.tmdb-api.movie-details]
   [lib.tmdb-api.shared]))


(defn- to-tmdb-params [page ctx]
  (merge ctx
         {:tmdb/language "en-US"
          :tmdb/sort-by "popularity.desc"
          :tmdb/include-adult false
          :tmdb/include-video false
          :tmdb/page (-> page :page/number)}))

(defmethod query-plan-item/result-chan! :tmdb-query-plan-item/discover-movie
  [[_ page] ctx]
  (go
    (let [tmdb-params (to-tmdb-params page ctx)
          tmdb-config (<! (lib.tmdb-api.configuration/fetch! tmdb-params))
          tmdb-discover-movie (<! (lib.tmdb-api.discover-movie/fetch! tmdb-params))
          tmdb-result (merge tmdb-params tmdb-discover-movie tmdb-config page)
          query-result (mapping/tmdb-result->query-result tmdb-result)
          result (merge result/ok query-result)]
      result)))
