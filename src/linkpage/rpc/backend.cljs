(ns linkpage.rpc.backend
  (:require [clojure.core.async :refer [go <!]]
            [linkpage.backend.request-handler :refer [request-handler!]]
            [linkpage.core.http-server :as http-server]
            [linkpage.core.http-server.http-request :as http-request]))

(defmulti rpc! first)

(defmethod request-handler! "/rpc" [req res]
  (println "/rpc time" req)
  (go
    (let [body-text (<! (http-request/body-text-chan req))
          body-edn (<! (http-request/body-edn-chan req))]
      (println "/rpc body-text" body-text)
      (println "/rpc body-edn" body-edn)
      (http-server/end! res))))