;; https://developer.themoviedb.org/reference/configuration-details
(ns core.tmdb-api.configuration
  (:require [clojure.core.async :refer [go <!]]
            [core.http-client :as http-client]
            [core.tmdb-api.shared :as shared]
            [cljs.spec.alpha :as s]))

(s/def :tmdb/base-url string?)
(s/def :tmdb/secure-base-url string?)
(s/def :tmdb/backdrop-sizes (s/coll-of string?))
(s/def :tmdb/logo-sizes (s/coll-of string?))
(s/def :tmdb/poster-sizes (s/coll-of string?))
(s/def :tmdb/profile-sizes (s/coll-of string?))
(s/def :tmdb/still-sizes (s/coll-of string?))

(s/def :tmdb/images
  (s/keys :req [:tmdb/base-url
                :tmdb/secure-base-url
                :tmdb/backdrop-sizes
                :tmdb/logo-sizes
                :tmdb/poster-sizes
                :tmdb/profile-sizes
                :tmdb/still-sizes]))

(s/def :tmdb/change-keys (s/coll-of string?))

(s/def :tmdb.configuration/response
  (s/keys :req [:tmdb/images
                :tmdb/change-keys]))

(def configuration-cache (atom {}))

(defn to-poster-url [configuration poster-path]
  (when (and configuration poster-path)
    (let [images (:tmdb/images configuration)
          base-url (:tmdb/secure-base-url images)
          sizes (:tmdb/poster-sizes images)
          largest-size (last sizes)]
      (str base-url largest-size poster-path))))

(defn to-backdrop-url [configuration backdrop-path]
  (when (and configuration backdrop-path)
    (let [images (:tmdb/images configuration)
          base-url (:tmdb/secure-base-url images)
          sizes (:tmdb/backdrop-sizes images)
          largest-size (last sizes)]
      (str base-url largest-size backdrop-path))))

(defn fetch-chan! [input]
  (go
    (if-let [cached (@configuration-cache input)]
      cached
      (let [request (shared/build-request input "/configuration")
            response (<! (http-client/fetch-chan! request))
            mapped-response (shared/map-response response)]
        (swap! configuration-cache assoc input mapped-response)
        mapped-response))))
