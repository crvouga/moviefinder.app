(ns lib.http-server.cors
  (:require
   [lib.http-server.http-res :refer [set-header!]]))

(defn allow!
  ([res]
   (allow! res "http://localhost:8080"))

  ([res origin]
   (set-header! res "Access-Control-Allow-Origin" origin)
   (set-header! res "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS")
   (set-header! res "Access-Control-Allow-Headers" "Content-Type, Authorization")
   (set-header! res "Access-Control-Allow-Credentials" "true")))