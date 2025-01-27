(ns moviefinder-app.rpc.frontend
  (:require
   [moviefinder-app.frontend.store :as store]
   [core.http-client :as http-client]
   [clojure.edn :as edn]
   [clojure.core.async :refer [<! go]]
   [moviefinder-app.frontend.config :refer [config]]))

(defn- rpc-fetch! [req]
  (http-client/fetch-chan!
   {:http-request/url (str (:wire/backend-url config) "/rpc")
    :http-request/method :http-method/post
    :http-request/headers {"Content-Type" "text/plain"}
    :http-request/body (pr-str req)}))

(defn rpc-chan! [req]
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

(defmethod store/eff! :rpc/send! [i]
  (println "store/eff! :rpc/send! " i)
  (go
    (let [eff-payload (store/eff-payload i)
          req (-> eff-payload :rpc/req)
          map-res (-> eff-payload :rpc/res)
          res (<! (rpc-chan! req))
          mapped-res (map-res res)]
      (store/put! i mapped-res))))