(ns app.rpc.backend
  (:require [clojure.core.async :refer [go <!]]
            [app.backend.http-respond :refer [http-respond!]]
            [app.backend.ctx :as ctx]
            [lib.http-server.http-request :as http-request]
            [lib.http-server.http-response :as http-response]
            [clojure.pprint :refer [pprint]]
            [app.rpc.shared :as shared]))

(defmulti rpc! first)

(def rpc-fns! (atom {}))

(defn reg-rpc [rpc-name rpc-fn]
  (swap! rpc-fns! assoc rpc-name rpc-fn))



(defn handle-rpc-request! [rpc-req]
  (go
    (let [rpc-req-name (first rpc-req)
          rpc-req-payload (or (second rpc-req) {})
          rpc-req-input-payload (ctx/assoc-ctx rpc-req-payload)
          rpc-req-input [rpc-req-name rpc-req-input-payload]
          rpc-res-unsafe (<! (rpc! rpc-req-input))
          rpc-res (ctx/dissoc-ctx rpc-res-unsafe)]
      (pprint {:rpc rpc-req-name
               :req rpc-req
               :res rpc-res})
      rpc-res)))

(defmethod http-respond! shared/endpoint [req res]
  (go
    (let [rpc-req (<! (http-request/body-edn-chan req))
          rpc-res (<! (handle-rpc-request! rpc-req))]
      (http-response/allow-cors! res)
      (http-response/set-header! res "Content-Type" "text/plain")
      (http-response/end! res (pr-str rpc-res)))))
