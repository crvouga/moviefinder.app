(ns moviefinder-app.backend
  (:require [clojure.core.async :refer [go <!]]
            [moviefinder-app.backend.request-handler :refer [request-handler!]]
            [moviefinder-app.backend.serve-single-page-app]
            [moviefinder-app.rpc.backend]
            [moviefinder-app.auth.backend]
            [moviefinder-app.core.http-server :as http-server]
            [moviefinder-app.backend.env :as env]))

(defn request-handler-root! [req res]
  (request-handler! req res))

(defn start-http-server! []
  (go
    (let [http-server! (http-server/new! request-handler-root!)]
      (<! (http-server/listen! http-server! env/port))
      (println (str "Server is running... http://localhost:" env/port)))))

(defn -main []
  (go
    (<! (start-http-server!))))
  