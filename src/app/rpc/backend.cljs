(ns app.rpc.backend
  (:require
   [app.backend.ctx :as ctx]
   [app.backend.http-respond :refer [http-respond!]]
   [app.rpc.shared :as shared]
   [clojure.core.async :refer [<! go]]
   [clojure.pprint :refer [pprint]]
   [lib.http-server.cors :as cors]
   [lib.http-server.http-req :as http-req]
   [lib.http-server.http-res :as http-res]
   [lib.map-ext :as map-ext]
   [lib.pretty :as pretty]
   [lib.session-id-cookie :as session-id-cookie]))


(def rpc-fns! (atom {}))

(defn reg [rpc-name rpc-fn]
  (swap! rpc-fns! assoc rpc-name rpc-fn))

(defn- rpc! [rpc-name rpc-body]
  (go
    (let [rpc-fn (get @rpc-fns! rpc-name)]
      (if (fn? rpc-fn)
        (<! (rpc-fn rpc-body))
        (merge rpc-body {:result/type :result/err
                         :err/err :rpc-error/rpc-fn-not-found
                         :err/data {:rpc-name rpc-name}})))))


(defn to-rpc-input [rpc-req session-id]
  (-> rpc-req
      second
      map-ext/ensure
      ctx/assoc-ctx
      (assoc :session/session-id session-id)))

(defn to-rpc-output [rpc-res]
  (-> rpc-res
      ctx/dissoc-ctx
      (dissoc :session/session-id)))


(defn handle-rpc-request! [rpc-req session-id]
  (go
    (let [rpc-input (to-rpc-input rpc-req session-id)
          rpc-res (<! (rpc! (first rpc-req) rpc-input))
          rpc-output (to-rpc-output rpc-res)]
      (binding [*print-level* 6]
        (pprint {:rpc (first rpc-req) :req rpc-req :res rpc-output}))
      rpc-output)))

(defmethod http-respond! shared/endpoint [req res]
  (go
    (let [rpc-req (<! (http-req/read-body-edn! req))
          session-id (session-id-cookie/read req)
          rpc-res (<! (handle-rpc-request! rpc-req session-id))]
      (cors/allow! req res)
      (http-res/set-header! res "Content-Type" "text/plain")
      (http-res/end! res (pretty/str-edn rpc-res)))))
