(ns app.rpc.backend
  (:require
   [app.backend.ctx :as ctx]
   [app.backend.http-respond :refer [http-respond!]]
   [app.rpc.shared :as shared]
   [clojure.core.async :refer [go <!]]
   [clojure.pprint :refer [pprint]]
   [lib.http-server.cors :as cors]
   [lib.http-server.http-req :as http-req]
   [lib.http-server.http-res :as http-res]
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

(defn handle-rpc-request! [rpc-req session-id]
  (go
    (let [rpc-body (-> rpc-req
                       second
                       (or {})
                       (ctx/assoc-ctx) (assoc :session/session-id session-id))
          rpc-res (<! (rpc! (first rpc-req) rpc-body))
          rpc-res (-> rpc-res ctx/dissoc-ctx (dissoc :session/session-id))]
      (pprint {:rpc (first rpc-req)
               :req rpc-req
               :res rpc-res})
      rpc-res)))

(defmethod http-respond! shared/endpoint [req res]
  (go
    (let [rpc-req (<! (http-req/read-body-edn! req))
          session-id (session-id-cookie/read req)
          rpc-res (<! (handle-rpc-request! rpc-req session-id))]
      (cors/allow! req res)
      (http-res/set-header! res "Content-Type" "text/plain")
      (http-res/end! res (pretty/str-edn rpc-res)))))
