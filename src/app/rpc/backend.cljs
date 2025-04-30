(ns app.rpc.backend
  (:require [clojure.core.async :as a]
            [app.backend.http-respond :refer [http-respond!]]
            [app.backend.ctx :as ctx]
            [lib.http-server.http-request :as http-request]
            [lib.http-server.http-response :as http-response]
            [clojure.pprint :refer [pprint]]
            [app.rpc.shared :as shared]))


(def rpc-fns! (atom {}))

(defn reg [rpc-name rpc-fn]
  (swap! rpc-fns! assoc rpc-name rpc-fn))

(defn- rpc! [rpc-name rpc-body]
  (let [rpc-fn (get @rpc-fns! rpc-name identity)]
    (rpc-fn rpc-body)))

(defn handle-rpc-request! [rpc-req]
  (a/go
    (let [rpc-body (-> rpc-req second (or {}) (ctx/assoc-ctx))
          rpc-res (a/<! (rpc! (first rpc-req) rpc-body))
          rpc-res (ctx/dissoc-ctx rpc-res)]
      (pprint {:rpc (first rpc-req)
               :req rpc-req
               :res rpc-res})
      rpc-res)))

(defmethod http-respond! shared/endpoint [req res]
  (a/go
    (let [rpc-req (a/<! (http-request/body-edn-chan req))
          rpc-res (a/<! (handle-rpc-request! rpc-req))]
      (http-response/allow-cors! res)
      (http-response/set-header! res "Content-Type" "text/plain")
      (http-response/end! res (pr-str rpc-res)))))
