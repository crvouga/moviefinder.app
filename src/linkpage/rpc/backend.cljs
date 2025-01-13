(ns linkpage.rpc.backend
  (:require [linkpage.backend.request-handler :refer [request-handler!]]))

(defmulti rpc! first)

(defmethod request-handler! "/rpc" [req _res]
  (println "/rpc time")
  (let [body (.-body req)
        req (js/JSON.parse body)
        [method & args] req
        res (rpc! method args)]
    #_(set! (.-statusCode res) 200)
    #_(set! (.-header res "Content-Type") "application/json")
    #_(set! (.-body res) (js/JSON.stringify res))
    res))