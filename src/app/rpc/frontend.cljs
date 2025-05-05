(ns app.rpc.frontend
  (:require
   [app.frontend.ctx :as ctx]
   [app.frontend.mod :as mod]
   [app.rpc.shared :as shared]
   [clojure.core.async :as a]
   [clojure.edn :as edn]
   [lib.err :as err]
   [lib.http-client :as http-client]
   [lib.pretty :as pretty]
   [lib.program :as p]
   [lib.serialize :as serialize]
   [clojure.pprint :as pprint]))

(defn to-url [req]
  (str (-> ctx/ctx :rpc/backend-url) shared/endpoint
       "?req=" (pr-str (first req))))

(defn- rpc-fetch! [req]
  (serialize/assert-serializable req)

  (http-client/fetch!
   {:http/url (to-url req)
    :http/method :http/post
    :http/credentials :include
    :http/headers {"Content-Type" "text/plain"}
    :http/body (pretty/str-edn req)}))

(defn rpc-res-chan! [req]
  (a/go
    (let [{:keys [http/ok? http/body]} (a/<! (rpc-fetch! req))
          edn (edn/read-string body)]
      (if ok?
        edn
        (merge edn {:result/type :result/err
                    :err/err :rpc/rpc-exception})))))

(defmethod err/message :rpc/rpc-exception []
  "Errored while requesting from backend")

(defmethod err/message :rpc-error/rpc-fn-not-found [_]
  "Feature is not implemented yet")

(defn- logic [i]
  (p/reg-eff
   i :rpc/send!
   (fn [[_ req]]
     (a/go
       (try
         (pprint/pprint {:rpc/send! req})
         (let [res (a/<! (rpc-res-chan! req))]
           (pprint/pprint {:rpc/res res})
           res)
         (catch js/Error e
           {:result/type :result/err
            :err/err :rpc/rpc-exception
            :err/data e}))))))

(mod/reg
 {:mod/name ::mod
  :mod/logic logic})