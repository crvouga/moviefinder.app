(ns moviefinder-app.rpc.frontend
  (:require
   [moviefinder-app.frontend.store :as store]
   [moviefinder-app.core.http-client :as http-client]
   [clojure.edn :as edn]
   [clojure.core.async :refer [<! go]]))

(defn- rpc-fetch! [msg]
  (http-client/fetch!
   {:http-request/url "/rpc"
    :http-request/method :http-method/post
    :http-request/headers {"Content-Type" "text/plain"}
    :http-request/body (pr-str msg)}))

(defn rpc! [msg]
  (go
    (let [res (<! (rpc-fetch! msg))
          body (-> res :http-response/body edn/read-string)]
      body)))

(defmethod store/eff! :rpc/send! [i]
  (go
    (let [eff-payload (store/eff-payload i)
          msg (-> eff-payload :rpc/req)
          map-res (-> eff-payload :rpc/res)
          res (<! (rpc! msg))
          mapped-res (map-res res)]
      (store/put! i mapped-res))))