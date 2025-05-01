(ns app.backend
  (:require
   [app.auth.backend]
   [app.backend.ctx :as config]
   [app.backend.http-respond :refer [http-respond!]]
   [app.backend.serve-single-page-app]
   [app.media.backend]
   [app.rpc.backend]
   [clojure.core.async :refer [<! go]]
   [lib.http-server :as http-server]
   [lib.session-id-cookie :as session-id-cookie]))

(defn request-handler-root! [req res]
  ((session-id-cookie/with-cookie http-respond!) req res))

(defn start-http-server! []
  (go
    (let [http-server! (http-server/new! request-handler-root!)
          port (config/ctx :http-server/port)]
      (<! (http-server/listen! http-server! port))
      (println (str "Server is running... http://localhost:" port)))))

(defn -main []
  (go
    (<! (start-http-server!))))
  