(ns moviefinder-app.rpc.backend
  (:require [clojure.core.async :refer [go <!]]
            [moviefinder-app.backend.request-handler :refer [request-handler!]]
            [moviefinder-app.frontend.ctx :refer [ctx]]
            [core.http-server.http-request :as http-request]
            [core.http-server.http-response :as http-response]))

(defmulti rpc! (fn [_ctx req] (first req)))

(defmethod rpc! :default [_ctx req]
  (go
    (println "rpc! :default " req)
    [:result/err {:error/message "Unknown rpc method"
                  :error/data {:rpc/req req}}]))


(defmethod request-handler! "/rpc" [req res]
  (go
    (let [body-edn (<! (http-request/body-edn-chan req))
          rpc-res (<! (rpc! ctx body-edn))
          rpc-res-text-plain (pr-str rpc-res)]
      (http-response/allow-cors! res)
      (http-response/set-header! res "Content-Type" "text/plain")
      (http-response/end! res rpc-res-text-plain))))