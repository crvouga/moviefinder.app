(ns lib.tmdb-api.discover-movie-test
  (:require
   [lib.tmdb-api.fixture :as fixture]
   [cljs.spec.alpha :as s]
   [cljs.test :refer-macros [deftest testing is async]]
   [clojure.core.async :refer [<! go]]
   [lib.tmdb-api.discover-movie :as discover-movie]))


(deftest fetch-movies-test
  (testing "fetching movies from TMDB API"
    (async
     done
     (go
       (let [params (merge fixture/ctx
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