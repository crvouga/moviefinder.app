(ns core.tmdb-api.discover-movie-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [clojure.core.async :refer [go <!]]
            [core.tmdb-api.discover-movie :as discover-movie]
            [moviefinder-app.backend.ctx :as ctx]
            [cljs.spec.alpha :as s]))


(deftest fetch-movies-test
  (testing "fetching movies from TMDB API"
    (async
     done
     (go
       (let [params (merge ctx/secrets
                           {:tmdb/language "en-US"
                            :tmdb/sort-by "popularity.desc"
                            :tmdb/include-adult false
                            :tmdb/include-video false
                            :tmdb/page 1})
             result (<! (discover-movie/fetch-chan! params))]

         (is (s/valid? :tmdb/response result)
             (s/explain-str :tmdb/response result))

         (is (seq (:tmdb/results result))
             "Response should contain movie results")

         (is (pos? (:tmdb/total-results result))
             "Total results should be positive")

         (done))))))