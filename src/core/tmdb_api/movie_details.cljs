;; https://developer.themoviedb.org/reference/movie-details
(ns core.tmdb-api.movie-details
  (:require [clojure.core.async :refer [go <!]]
            [core.http-client :as http-client]
            [core.tmdb-api.shared :as shared]
            [core.tmdb-api.shared-spec]))

(defn fetch-chan!
  "Returns a channel containing a :tmdb/response"
  [movie-id params]
  (go
    (-> params
         (shared/build-request (str "/movie/" movie-id))
         http-client/fetch-chan!
         <!
         shared/map-response)))