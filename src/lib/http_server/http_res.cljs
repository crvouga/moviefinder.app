(ns lib.http-server.http-res
  (:require
   [lib.http-cookie :as http-cookie]))

(defn set-header! [^js res key value]
  (.setHeader res key value))
(defn allow-cors!
  ([^js res] (allow-cors! res "http://localhost:8080"))
  ([^js res origin]
   (set-header! res "Access-Control-Allow-Origin" origin)
   (set-header! res "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS")
   (set-header! res "Access-Control-Allow-Headers" "Content-Type, Authorization")
   (set-header! res "Access-Control-Allow-Credentials" "true")))

(defn end!
  ([^js res] (.end res))
  ([^js res body] (.end res body)))

(defn set-cookie! [^js res cookie]
  (set-header! res "Set-Cookie" (http-cookie/to-header-value cookie)))
