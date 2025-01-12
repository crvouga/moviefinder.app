(ns linkhub.backend.main
  (:require ["http" :as http]
            [linkhub.backend.serve-single-page-app]
            [linkhub.rpc.backend]
            [linkhub.backend.request-handler :refer [request-handler!]]
            [linkhub.backend.env :as env]))

(defn on-start []
  (println (str "Server is running on http://localhost:" env/port)))

(defn start-server []
  (let [http-server! (.createServer http request-handler!)]
    (.listen http-server! env/port on-start)))

(defn -main []
  (println "Starting server...")
  (start-server))