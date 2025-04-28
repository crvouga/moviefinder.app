(ns app.rpc.frontend
  (:require
   [app.frontend.ctx :refer [ctx]]
   [app.frontend.mod :as mod]
   [clojure.core.async :refer [<! go]]
   [clojure.edn :as edn]
   [lib.http-client :as http-client]
   [lib.program :as p]
   [app.rpc.shared :as shared]))

(defn- rpc-fetch! [req]
  (http-client/fetch-chan!
   {:http-request/url (str (:wire/backend-url ctx) shared/endpoint "?req=" (-> req first pr-str))
    :http-request/method :http-method/post
    :http-request/headers {"Content-Type" "text/plain"}
    :http-request/body (pr-str req)}))

(defn rpc-res-chan! [req]
  (println "rpc-chan! " req)
  (go
    (let [res (<! (rpc-fetch! req))
          body (-> res :http-response/body)
          body-edn (-> body edn/read-string)
          ok? (-> res :http-response/ok?)]
      (if ok?
        body-edn
        (merge body-edn {:result/type :result/ok
                         :error/message "Errored while requesting from backend"})))))

(defn- logic [i]
  (p/reg-eff i :rpc/send! (fn [[_ req]] (rpc-res-chan! req))))

(mod/reg
 {:mod/name :mod/rpc
  :mod/logic-fn logic})