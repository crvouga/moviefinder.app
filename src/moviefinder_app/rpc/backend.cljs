(ns moviefinder-app.rpc.backend
  (:require [clojure.core.async :refer [go <!]]
            [moviefinder-app.backend.request-handler :refer [request-handler!]]
            [moviefinder-app.backend.config :as config]
            [core.http-server.http-request :as http-request]
            [core.http-server.http-response :as http-response]))

(defmulti rpc! first)

(defmethod rpc! :default [req]
  (go
    (println "rpc! :default " req)
    {:result/type :result/err
     :error/message "Unknown rpc method"
     :rpc/req req}))


(defn handle-rpc-request! [rpc-req]
  (go
    (let [rpc-req-name (first rpc-req)
          rpc-req-payload (or (second rpc-req) {})
          rpc-req-input-payload (config/assoc-config rpc-req-payload)
          rpc-req-input [rpc-req-name rpc-req-input-payload]
          rpc-res-unsafe (<! (rpc! rpc-req-input))
          rpc-res (config/dissoc-config rpc-res-unsafe)]
      (println
       "\nrpc:" rpc-req-name
       "\nreq:" rpc-req
       "\nres:" rpc-res
       "\n")
      rpc-res)))

(defmethod request-handler! "/rpc" [req res]
  (go
    (let [rpc-req (<! (http-request/body-edn-chan req))
          rpc-res (<! (handle-rpc-request! rpc-req))]
      (http-response/allow-cors! res)
      (http-response/set-header! res "Content-Type" "text/plain")
      (http-response/end! res (pr-str rpc-res)))))
