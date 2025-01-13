(ns linkpage.rpc.backend
  (:require [clojure.core.async :refer [go <!]]
            [linkpage.backend.request-handler :refer [request-handler!]]
            [linkpage.core.http-server.http-request :as http-request]
            [linkpage.core.http-server.http-response :as http-response]))

(defmulti rpc! first)

(defmethod rpc! :default [req]
  (go
    (println "rpc! :default " req)
    [:result/err {:error/message "Unknown rpc method"
                  :error/data {:rpc/req req}}]))

(defmethod request-handler! "/rpc" [req res]
  (go
    (let [body-edn (<! (http-request/body-edn-chan req))
          rpc-res (<! (rpc! body-edn))
          rpc-res-text-plain (pr-str rpc-res)]
      (println "body-edn " body-edn)
      (println "rpc-res " rpc-res)
      (println "rpc-res-text-plain " rpc-res-text-plain)
      (http-response/set-header! res "Content-Type" "text/plain")
      (http-response/end! res rpc-res-text-plain))))