(ns linkpage.backend
  (:require [clojure.core.async :refer [go <!]]
            [linkpage.backend.request-handler :refer [request-handler!]]
            [linkpage.backend.serve-single-page-app]
            [linkpage.rpc.backend]
            [linkpage.auth.login.backend]
            [linkpage.auth.current-user.backend]
            [linkpage.core.http-server :as http-server]
            [linkpage.backend.env :as env]))

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
  