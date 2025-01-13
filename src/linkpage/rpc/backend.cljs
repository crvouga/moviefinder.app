(ns linkpage.rpc.backend
  (:require [clojure.core.async :refer [go <!]]
            [linkpage.backend.request-handler :refer [request-handler!]]
            [linkpage.core.http-server.http-request :as http-request]
            [linkpage.core.http-server.http-response :as http-response]))

(defmulti rpc! first)

(defmethod request-handler! "/rpc" [req res]
  (println "/rpc time" req)
  (go
    (let [body-text (<! (http-request/body-text-chan req))
          #_body-edn #_(<! (http-request/body-edn-chan req))]
      (println "/rpc body-text" body-text)
      #_(println "/rpc body-edn" body-edn)
      (http-response/end! res))))