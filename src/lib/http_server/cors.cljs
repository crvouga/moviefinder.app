(ns lib.http-server.cors
  (:require
   [lib.http-server.http-res :refer [set-header!]]
   [lib.http-server.http-req :as http-req]))

(defn allow! [req res]
  (set-header! res "Access-Control-Allow-Origin" (http-req/get-header req "origin"))
  (set-header! res "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS")
  (set-header! res "Access-Control-Allow-Headers" "Content-Type, Authorization")
  (set-header! res "Access-Control-Allow-Credentials" "true"))