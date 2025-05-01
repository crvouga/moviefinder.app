(ns app.rpc.frontend
  (:require
   [app.frontend.ctx :refer [ctx]]
   [app.frontend.mod :as mod]
   [app.rpc.shared :as shared]
   [clojure.core.async :as a]
   [clojure.edn :as edn]
   [lib.err :as err]
   [lib.http-client :as http-client]
   [lib.program :as p]
   [lib.serialize :as serialize]
   [lib.pretty :as pretty]))

(defn to-url [req]
  (str (:wire/backend-url ctx) shared/endpoint "?req=" (pr-str (first req))))

(defn- rpc-fetch! [req]
  (serialize/assert-serializable req)
  (http-client/fetch!
   {:http/url (to-url req)
    :http/method :http/post
    :http/headers {"Content-Type" "text/plain"}
    :http/body (pretty/str-edn req)}))

(defn rpc-res-chan! [req]
  (a/go
    (let [{:keys [http/ok? http/body]} (a/<! (rpc-fetch! req))
          edn (edn/read-string body)]
      (if ok?
        edn
        (merge edn {:result/type :result/err
                    :err/err :err/rpc-error})))))

(defmethod err/message :err/rpc-error []
  "Errored while requesting from backend")

(defn- logic [i]
  (p/reg-eff
   i :rpc/send!
   (fn [[_ req]]
     (a/go
       (try
         (a/<! (rpc-res-chan! req))
         (catch js/Error e
           {:result/type :result/err
            :err/err :err/rpc-error
            :err/data e}))))))

(mod/reg
 {:mod/name :mod/rpc
  :mod/logic-fn logic})