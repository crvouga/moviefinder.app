(ns app.backend
  (:require [clojure.core.async :refer [go <!]]
            [app.backend.http-respond :refer [http-respond!]]
            [app.backend.ctx :as config]
            [app.backend.serve-single-page-app]
            [app.rpc.backend]
            [app.auth.backend]
            [app.media.backend]
            [lib.http-server :as http-server]))

(defn request-handler-root! [req res]
  (http-respond! req res))

(defn start-http-server! []
  (go
    (let [http-server! (http-server/new! request-handler-root!)
          port (config/ctx :http-server/port)]
      (<! (http-server/listen! http-server! port))
      (println (str "Server is running... http://localhost:" port)))))

(defn -main []
  (go
    (<! (start-http-server!))))
  