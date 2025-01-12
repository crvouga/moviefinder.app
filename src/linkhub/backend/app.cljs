(ns linkhub.backend.app
  (:require ["http" :as http]
            [linkhub.backend.serve-single-page-app :as serve-single-page-app]
            [linkhub.backend.env :as env]))

(defn request-handler [req res]
  (serve-single-page-app/request-handler! req res))

(defn on-start []
  (println (str "Server is running on http://localhost:" env/port)))

(defn start-server []
  (let [http-server! (.createServer http request-handler)]
    (.listen http-server! env/port on-start)))

(defn -main []
  (println "Starting server...")
  (start-server))