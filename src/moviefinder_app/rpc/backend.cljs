(ns moviefinder-app.rpc.backend
  (:require [clojure.core.async :refer [go <!]]
            [moviefinder-app.backend.request-handler :refer [request-handler!]]
            [moviefinder-app.backend.ctx :refer [ctx]]
            [core.http-server.http-request :as http-request]
            [core.http-server.http-response :as http-response]))

(defmulti rpc! first)

(defmethod rpc! :default [req]
  (go
    (println "rpc! :default " req)
    {:result/type :result/err
     :error/message "Unknown rpc method"
     :rpc/req req}))


(defmethod request-handler! "/rpc" [req res]
  (go
    (let [rpc-req (<! (http-request/body-edn-chan req))
          rpc-req-name (first rpc-req)
          rpc-req-payload (or (second rpc-req) {})
          rpc-req-input-payload (merge ctx rpc-req-payload)
          rpc-req-input [rpc-req-name rpc-req-input-payload]
          rpc-res (<! (rpc! rpc-req-input))]
      (println
       "\nrpc:" rpc-req-name
       "\nreq:" rpc-req
       "\nres:" rpc-res
       "\n")
      (http-response/allow-cors! res)
      (http-response/set-header! res "Content-Type" "text/plain")
      (http-response/end! res (pr-str rpc-res)))))

