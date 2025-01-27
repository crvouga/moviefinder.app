(ns moviefinder-app.backend
  (:require [clojure.core.async :refer [go <!]]
            [moviefinder-app.backend.request-handler :refer [request-handler!]]
            [moviefinder-app.backend.config :as config]
            [moviefinder-app.backend.serve-single-page-app]
            [moviefinder-app.rpc.backend]
            [moviefinder-app.auth.backend]
            [moviefinder-app.media.backend]
            [core.http-server :as http-server]))

(defn request-handler-root! [req res]
  (request-handler! req res))

(defn start-http-server! []
  (go
    (let [http-server! (http-server/new! request-handler-root!)
          port (config/config :http-server/port)]
      (<! (http-server/listen! http-server! port))
      (println (str "Server is running... http://localhost:" port)))))

(defn -main []
  (go
    (<! (start-http-server!))))
  