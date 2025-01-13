(ns linkpage.rpc.frontend
  (:require
   [linkpage.frontend.store :as store]
   [linkpage.core.http-client :as http-client]
   [clojure.core.async :refer [<! go]]))

(defn rpc! [msg]
  (http-client/send!
   {:http-request/method :http-method/post
    :http-request/url "/rpc"
    :http-request/headers {"Content-Type" "text/plain"}
    :http-request/body (pr-str msg)}))

(defmethod store/eff! :rpc/send! [i]
  (go
    (let [eff-payload (store/eff-payload i)
          msg (-> eff-payload :rpc/req)
          map-res (-> eff-payload :rpc/res)
          res (<! (rpc! msg))
          mapped-res (map-res res)]
      (println "mapped-res" mapped-res)
      (store/dispatch! i mapped-res))))