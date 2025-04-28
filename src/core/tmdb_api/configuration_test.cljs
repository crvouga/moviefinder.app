(ns core.tmdb-api.configuration-test
  (:require
   [core.tmdb-api.fixture :as fixture]
   [cljs.spec.alpha :as s]
   [cljs.test :refer-macros [deftest testing is async]]
   [clojure.core.async :refer [<! go]]
   [core.tmdb-api.configuration :as configuration]))

(deftest fetch-configuration-test
  (testing "fetching configuration from TMDB API"
    (async
     done
     (go
       (let [result (<! (configuration/fetch-chan! fixture/ctx))]
         (is (s/valid? :tmdb.configuration/response result)
             (s/explain-str :tmdb.configuration/response result))
         (done))))))
