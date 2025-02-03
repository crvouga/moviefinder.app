(ns core.tmdb-api.configuration-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [clojure.core.async :refer [go <!]]
            [core.tmdb-api.configuration :as configuration]
            [cljs.spec.alpha :as s]
            [app.backend.config :as config]))

(deftest fetch-configuration-test
  (testing "fetching configuration from TMDB API"
    (async
     done
     (go
       (let [result (<! (configuration/fetch-chan! config/config))]
         (is (s/valid? :tmdb.configuration/response result)
             (s/explain-str :tmdb.configuration/response result))
         (done))))))
