;; https://developer.themoviedb.org/reference/discover-movie
(ns core.tmdb-api.discover-movie
  (:require [clojure.core.async :refer [go <!]]
            [core.http-client :as http-client]
            [core.tmdb-api.shared :as shared]))

(defn fetch-chan! [params]
  (go
    (let [request (shared/build-request "/discover/movie" params)
          response (<! (http-client/fetch-chan! request))
          mapped-response (shared/map-response response)]
      mapped-response)))
