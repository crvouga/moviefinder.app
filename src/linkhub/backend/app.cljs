(ns linkhub.backend.app
  (:require ["http" :as http]
            [linkhub.backend.serve-single-page-app :as serve-single-page-app]))

(defn request-handler [req res]
  (serve-single-page-app/request-handler! req res))

(defn on-start []
  (println "Server is running on http://localhost:3000"))

(defn start-server []
  (let [http-server! (.createServer http request-handler)]
    (.listen http-server! 3000 on-start)))

(defn -main []
  (println "Starting server...")
  (start-server))