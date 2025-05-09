(ns lib.kv.impl-http-client
  (:require
   [lib.http-client :as http-client]))



(defmethod kv/get :http-client [config key]
  (http-client/fetch! {:http/method :http/get
                       :http/url ()}))

(defmethod kv/put :http-client [config key value]
  (http-client/put config key value))