(ns app.backend.http-respond
  (:require [lib.http-server.http-req :as http-req]))

(defmulti http-respond!
  (fn [req _res] (http-req/url req)))