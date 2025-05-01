(ns app.rpc.backend
  (:require
   [app.backend.ctx :as ctx]
   [app.backend.http-respond :refer [http-respond!]]
   [app.rpc.shared :as shared]
   [clojure.core.async :as a]
   [clojure.pprint :refer [pprint]]
   [lib.http-server.http-req :as http-req]
   [lib.http-server.http-res :as http-res]
   [lib.pretty :as pretty]))


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
    (let [rpc-req (a/<! (http-req/body-edn-chan req))
          rpc-res (a/<! (handle-rpc-request! rpc-req))]
      (http-res/allow-cors! res)
      (http-res/set-header! res "Content-Type" "text/plain")
      (http-res/end! res (pretty/str-edn rpc-res)))))
