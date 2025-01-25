(ns core.tmdb-api.discover.movie
  (:require [clojure.core.async :refer [go <!]]
            [core.http-client :as http-client]))


(defn map-response [response]
  (when (:http-response/ok? response)
    (try
      (-> response
          :http-response/body
          js/JSON.parse
          (js->clj :keywordize-keys true)
          :results)
      (catch :default e
        (println "Error parsing response:" e)
        nil))))

(defn fetch-chan [_]
  (go
    (println "fetching")
    (let [request {:http-request/method :http-method/get
                   :http-request/url "https://api.themoviedb.org/3/discover/movie"
                   :http-request/headers {"Authorization" "Bearer 1234567890"}}
          _ (println "Sending request:" request)
          response (<! (http-client/fetch-chan! request))
          _ (println "Raw response:" response)
          mapped-response (map-response response)]
      (println "mapped-response" mapped-response)
      mapped-response)))


(comment
  (go
    (println "fetching\n\n\n")
    (let [medias (<! (fetch-chan {}))]
      (println "medias" medias)
      (println medias))))