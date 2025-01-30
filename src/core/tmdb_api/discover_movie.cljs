;; https://developer.themoviedb.org/reference/discover-movie
(ns core.tmdb-api.discover-movie
  (:require [clojure.core.async :refer [go <!]]
            [core.http-client :as http-client]
            [core.tmdb-api.shared :as shared]
            [core.tmdb-api.shared-spec]))

(defn fetch-chan!
  "Returns a channel containing a :tmdb/response"
  [params]
  (go
    (->> params
         (shared/build-request "/discover/movie") 
         http-client/fetch-chan! 
         <!
         shared/map-response)))