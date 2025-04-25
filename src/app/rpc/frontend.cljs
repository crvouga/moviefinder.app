(ns app.rpc.frontend
  (:require
   [app.frontend.config :refer [config]]
   [app.frontend.mod :as mod]
   [clojure.core.async :refer [<! go]]
   [clojure.edn :as edn]
   [core.http-client :as http-client]
   [core.program :as p]))

(defn- rpc-fetch! [req]
  (http-client/fetch-chan!
   {:http-request/url (str (:wire/backend-url config) "/rpc")
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