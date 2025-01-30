;; https://developer.themoviedb.org/reference/movie-details
(ns core.tmdb-api.movie-details-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [clojure.core.async :refer [go <!]]
            [core.tmdb-api.movie-details :as movie-details]
            [moviefinder-app.backend.config :as config]
            [cljs.spec.alpha :as s]))

(deftest fetch-movie-details-test
  (testing "fetching movie details from TMDB API"
    (async
     done
     (go
       (let [movie-id 550  ; Fight Club movie ID
             params (merge config/config
                          {:tmdb/language "en-US"})
             result (<! (movie-details/fetch-chan! movie-id params))]

         (is (s/valid? :tmdb/movie result)
             (s/explain-str :tmdb/movie result))

         (is (= (:tmdb/id result) movie-id)
             "Response should contain the requested movie ID")

         (is (string? (:tmdb/title result))
             "Response should contain a movie title")

         (done))))))