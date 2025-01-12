(ns linkhub.rpc.frontend 
  (:require
    [linkhub.frontend.store :as store]
    [clojure.core.async :as async]))

(defn rpc! [request]
  (println request))

(defmethod store/effect! :rpc/send! [i]
  (async/go
    (let [effect-payload (-> i :store/effect second)
          req (-> effect-payload :rpc/req)
          map-response (-> effect-payload :rpc/msg)
          res (async/<! (rpc! req))
          mapped-res (map-response res)]
      (store/dispatch! i mapped-res))))