(ns core.tmdb-api.discover.movie
  (:require [clojure.core.async :refer [go <!]]
            [core.http-client :as http-client]))


(defn map-response [response]
  (-> response
      :http-response/body
      (js->clj :keyword-key-mapper :keyword)
      (get "results")))

(defn fetch [_]
  (go
    (let [response (<! (http-client/fetch! {:http-request/method :http-method/get
                                            :http-request/url "https://api.themoviedb.org/3/discover/movie"
                                            :http-request/headers {:api-key "1234567890"}}))]
      (response))))

