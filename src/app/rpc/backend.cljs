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
   [lib.pretty :as pretty]
   [lib.session-id-cookie :as session-id-cookie]))


(def rpc-fns! (atom {}))

(defn reg-fn [rpc-name rpc-fn]
  (swap! rpc-fns! assoc rpc-name rpc-fn))

(defn err-not-found [rpc-name]
  {:result/type :result/err
   :err/err :rpc-error/rpc-fn-not-found
   :err/data {:rpc-name rpc-name}})

(defn- call! [ctx rpc-fn-name rpc-fn-args]
  (go
    (let [rpc-fn (get @rpc-fns! rpc-fn-name)]
      (if (fn? rpc-fn)
        (<! (apply rpc-fn ctx rpc-fn-args))
        (err-not-found rpc-fn-name)))))

(defn- log [rpc-fn-name rpc-fn-args rpc-res]
  (binding [*print-level* 6]
    (pprint {:rpc rpc-fn-name :args rpc-fn-args :res rpc-res})))

(defn- dissoc-ctx [input]
  (apply dissoc input (keys ctx/ctx)))

(defn handle-rpc-req! [session-id [rpc-fn-name & rpc-fn-args]]
  (go
    (let [rpc-ctx (-> ctx/ctx (assoc :session/session-id session-id))
          rpc-res (<! (call! rpc-ctx rpc-fn-name rpc-fn-args))
          rpc-res (dissoc-ctx rpc-res)]
      (log rpc-fn-name rpc-fn-args rpc-res)
      rpc-res)))

(defmethod http-respond! shared/endpoint [req res]
  (go
    (let [rpc-req (<! (http-req/read-body-edn! req))
          session-id (session-id-cookie/read req)
          rpc-res (<! (handle-rpc-req! session-id rpc-req))]
      (cors/allow! req res)
      (http-res/set-header! res "Content-Type" "text/plain")
      (http-res/end! res (pretty/str-edn rpc-res)))))
