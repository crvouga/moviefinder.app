(ns app.backend
  (:require [clojure.core.async :refer [go <!]]
            [app.backend.request-handler :refer [request-handler!]]
            [app.backend.config :as config]
            [app.backend.serve-single-page-app]
            [app.rpc.backend]
            [app.auth.backend]
            [app.media.backend]
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
  