(ns app.rpc.backend
  (:require [clojure.core.async :refer [go <!]]
            [app.backend.request-handler :refer [request-handler!]]
            [app.backend.config :as config]
            [core.http-server.http-request :as http-request]
            [core.http-server.http-response :as http-response]
            [clojure.pprint :refer [pprint]]
            [app.rpc.shared :as shared]))

(defmulti rpc! first)

(defmethod rpc! :default [req]
  (go
    (pprint {:msg "rpc! :default" :req req})
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
      (pprint {:rpc rpc-req-name
               :req rpc-req
               :res rpc-res})
      rpc-res)))

(defmethod request-handler! shared/endpoint [req res]
  (go
    (let [rpc-req (<! (http-request/body-edn-chan req))
          rpc-res (<! (handle-rpc-request! rpc-req))]
      (http-response/allow-cors! res)
      (http-response/set-header! res "Content-Type" "text/plain")
      (http-response/end! res (pr-str rpc-res)))))
