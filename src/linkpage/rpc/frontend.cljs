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

(defmethod store/effect! :rpc/send! [i]
  (go
    (let [effect-payload (-> i :store/effect second)
          msg (-> effect-payload :rpc/msg)
          map-response (-> effect-payload :rpc/dispatch!)
          res (<! (rpc! msg))
          mapped-res (map-response res)]
      (println "mapped-res" mapped-res)
      (store/dispatch! i mapped-res))))